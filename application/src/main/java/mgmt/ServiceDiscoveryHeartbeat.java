package mgmt;

import java.io.IOException;
import java.util.Iterator;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.apache.commons.lang3.tuple.Pair;

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

	private static final String VCA_URIS = "uris";
	private static final String VCS_SERVICE_DISCOVERY = "service_discovery";
	private static final String VCS_SERVICE_DISCOVERY_AUTH_TOKEN = "auth_token";
	private static final String VCS_SERVICE_DISCOVERY_CREDENTIALS = "credentials";

	private static final String VCS_SERVICE_DISCOVERY_URL = "url";

	@EJB
	private AboutService about;

	private Register registerRequest;

	private Optional<Pair<String, String>> serviceDiscoveryCredentials;

	@PostConstruct
	public void before() {
		this.registerRequest = registerService$createRequest();
		this.serviceDiscoveryCredentials = getServiceDiscoveryCredentials();
		registerService();
	}

	@Schedule(second = "0", minute = "*/1", hour = "*", persistent = false)
	public void heartbeat() {
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

	private Optional<Pair<String, String>> getServiceDiscoveryCredentials() {
		ObjectMapper m = new ObjectMapper();
		JsonNode vcapServices = null;

		try {
			vcapServices = m.readTree(CONFIG.getString("vcap.services"));
		} catch (IOException e) {
			LOG.warn("Unable to parse VCAP_SERVICES from configuration variable `vcap.services`.");
		}

		if (vcapServices != null && vcapServices.get(VCS_SERVICE_DISCOVERY) != null) {
			try {
				Iterator<JsonNode> elements = vcapServices.get(VCS_SERVICE_DISCOVERY).elements();

				while (elements.hasNext()) {
					JsonNode serviceDiscovery = elements.next();
					JsonNode credentials = serviceDiscovery.get(VCS_SERVICE_DISCOVERY_CREDENTIALS);
					String authToken = credentials.get(VCS_SERVICE_DISCOVERY_AUTH_TOKEN).asText();
					String url = credentials.get(VCS_SERVICE_DISCOVERY_URL).asText();
					return Optional.of(Pair.of(authToken, url));
				}
			} catch (Exception e) {
				LOG.warn("Unable to register at service discovery", e.getMessage());
			}
		} else {
			LOG.warn("No VCAP_SERVICES defined in variable `vcap.services`.");
		}

		return Optional.empty();
	}

	private void registerService() {
		try {
			if (serviceDiscoveryCredentials.isPresent()) {
				HttpResponse<String> response = Unirest //
						.post(serviceDiscoveryCredentials.get().getRight() + "/api/v1/instances") //
						.header("Content-Type", "application/json") //
						.header("Accept", "application/json") //
						.header("Authorization", "Bearer " + serviceDiscoveryCredentials.get().getLeft()) //
						.body(registerRequest.toString()) //
						.asString();

				LOG.debug(response.getBody());
			}
		} catch (Exception e) {
			LOG.warn("Unable to register at service discovery", e.getMessage());
			e.printStackTrace();
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
