package model.bluemix.servicediscovery;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import model.About;
import util.BaseObject;

/**
 * { "service_name":"Service_Name", "endpoint":{ "type":"type", "value":"value"
 * }, "status":"Instance_Status", "ttl":3000, "metadata":{
 * "name":"Instance_Name" } }
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "service_name", "endpoint", "status", "ttl", "metadata" })
public class Register extends BaseObject {

	@XmlElement(name = "service_name")
	public final String serviceName;

	@XmlElement(name = "endpoint")
	public final Endpoint endpoint;

	@XmlElement(name = "status")
	public final String status;

	@XmlElement(name = "ttl")
	public final long ttl;

	@XmlElement(name = "metadata")
	public final About metadata;

	/**
	 * Constructor for instantiation via JAXB
	 */
	@SuppressWarnings("unused")
	private Register() {
		this(null, null, null, null, 0l, null);
	}

	public Register(String serviceName, String type, String value, String status, long ttl, About metadata) {
		this.serviceName = serviceName;
		this.endpoint = new Endpoint(type, value);
		this.status = status;
		this.ttl = ttl;
		this.metadata = metadata;
	}

}
