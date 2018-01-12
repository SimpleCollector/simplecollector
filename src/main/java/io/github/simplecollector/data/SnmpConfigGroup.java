package io.github.simplecollector.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 
 * The {@code SnmpConfigGroup} defines SNMP authentication and communication properties that can be identified by a name and id.
 * 
 * @author Johan Boer
 *
 */
@Document
public class SnmpConfigGroup {
	@Id
	private String name;
	private SnmpAuth auth;
	private SnmpCommunicationSettings comsettings;

	public SnmpConfigGroup() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public SnmpAuth getAuth() {
		return auth;
	}

	public void setAuth(SnmpAuth auth) {
		this.auth = auth;
	}

	public SnmpCommunicationSettings getComsettings() {
		return comsettings;
	}

	public void setComsettings(SnmpCommunicationSettings comsettings) {
		this.comsettings = comsettings;
	}

}
