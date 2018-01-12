package io.github.simplecollector.data;

import org.soulwing.snmp.SnmpV3Target.AuthType;
import org.soulwing.snmp.SnmpV3Target.PrivType;

public class SnmpAuth {
	private String version; // "2c" or "3"
	private String community;
	private String securityName;
	private AuthType authType;
	private String authPassphrase;
	private PrivType privType;
	private String privPassphrase;
	private String scope;

	public SnmpAuth() {
	}

	public String getVersion() {
		return version;
	}


	public void setVersion(String version) {
		this.version = version;
	}


	public String getCommunity() {
		return community;
	}

	public void setCommunity(String community) {
		this.community = community;
	}

	public String getSecurityName() {
		return securityName;
	}

	public void setSecurityName(String securityName) {
		this.securityName = securityName;
	}

	public AuthType getAuthType() {
		return authType;
	}

	public void setAuthType(AuthType authType) {
		this.authType = authType;
	}

	public String getAuthPassphrase() {
		return authPassphrase;
	}

	public void setAuthPassphrase(String authPassphrase) {
		this.authPassphrase = authPassphrase;
	}

	public PrivType getPrivType() {
		return privType;
	}

	public void setPrivType(PrivType privType) {
		this.privType = privType;
	}

	public String getPrivPassphrase() {
		return privPassphrase;
	}

	public void setPrivPassphrase(String privPassphrase) {
		this.privPassphrase = privPassphrase;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}
	
}
