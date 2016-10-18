package model.bluemix.servicediscovery;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import util.BaseObject;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "type", "value" })
public class Endpoint extends BaseObject {

	@XmlElement(name = "type")
	public final String type;

	@XmlElement(name = "value")
	public final String value;

	/**
	 * Constructor for instantiation via JAXB
	 */
	@SuppressWarnings("unused")
	private Endpoint() {
		this(null, null);
	}

	public Endpoint(String type, String value) {
		this.type = type;
		this.value = value;
	}

}
