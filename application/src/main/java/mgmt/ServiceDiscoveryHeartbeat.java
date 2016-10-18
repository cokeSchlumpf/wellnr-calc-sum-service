package mgmt;

import java.io.IOException;
import java.util.Iterator;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

import model.bluemix.servicediscovery.Register;
import util.BaseObject;
import ws.AboutService;

@Startup
@Singleton
public class ServiceDiscoveryHeartbeat extends BaseObject {

	private static final String VCS_SERVICE_DISCOVERY = "service_discovery";
	private static final String VCS_SERVICE_DISCOVERY_CREDENTIALS = "credentials";
	private static final String VCS_SERVICE_DISCOVERY_AUTH_TOKEN = "auth_token";
	private static final String VCS_SERVICE_DISCOVERY_URL = "url";

	private static final String VCA_URIS = "uris";

	@EJB
	private AboutService about;

	private Register registerRequest;

	@PostConstruct
	public void before() {
		this.registerRequest = registerService$createRequest();
		registerService();
	}

	private String getApplicationUri() {
		ObjectMapper m = new ObjectMapper();
		JsonNode vcapApplication;

		try {
			vcapApplication = m.readTree(CONFIG.getString("vcap.application"));
		} catch (IOException e) {
			LOG.warn("Unable to parse VCAP_APPLICATION from configuration variable `vcap.application`.");
			return getApplicationUriFallback();
		}

		if (vcapApplication.get(VCA_URIS) != null) {
			Iterator<JsonNode> uris = vcapApplication.get(VCA_URIS).elements();
			if (uris.hasNext()) {
				return uris.next().asText();
			} else {
				LOG.warn("There is no URI configured in VCAP_APPLICATION, using Fallback.");
				return getApplicationUriFallback();
			}
		} else {
			LOG.warn("No VCAP_APPLICATION defined in configuration variable `vcap.application`.");
			return getApplicationUriFallback();
		}
	}

	private String getApplicationUriFallback() {
		return CONFIG.getString("app.name") + ".mybluemix.net";
	}

	@Schedule(second = "0", minute = "*/1", hour = "*", persistent = false)
	public void heartbeat() {
		registerService();
	}

	private void registerService() {
		ObjectMapper m = new ObjectMapper();
		JsonNode vcapServices;

		try {
			vcapServices = m.readTree(CONFIG.getString("vcap.services"));
		} catch (IOException e) {
			LOG.warn("Unable to parse VCAP_SERVICES from configuration variable `vcap.services`.");
			return;
		}

		if (vcapServices.get(VCS_SERVICE_DISCOVERY) != null) {
			try {
				Iterator<JsonNode> elements = vcapServices.get(VCS_SERVICE_DISCOVERY).elements();

				while (elements.hasNext()) {
					JsonNode serviceDiscovery = elements.next();
					JsonNode credentials = serviceDiscovery.get(VCS_SERVICE_DISCOVERY_CREDENTIALS);
					String authToken = credentials.get(VCS_SERVICE_DISCOVERY_AUTH_TOKEN).asText();
					String url = credentials.get(VCS_SERVICE_DISCOVERY_URL).asText();

					HttpResponse<String> response = Unirest //
							.post(url + "/api/v1/instances") //
							.header("Content-Type", "application/json") //
							.header("Accept", "application/json") //
							.header("Authorization", "Bearer " + authToken) //
							.body(registerRequest.toString()) //
							.asString();

					LOG.debug(response.getBody());
				}
			} catch (Exception e) {
				LOG.warn("Unable to register at service discovery", e.getMessage());
				e.printStackTrace();
			}
		} else {
			LOG.warn("No VCAP_SERVICES defined in variable `vcap.services`.");
		}
	}

	private Register registerService$createRequest() {
		Register register = new Register( //
				CONFIG.getString("app.name"), //
				"http", //
				"http://" + this.getApplicationUri(), "UP", //
				300, //
				about.about());

		LOG.debug("Service information", register);

		return register;
	}
}
