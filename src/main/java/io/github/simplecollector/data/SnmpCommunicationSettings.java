package io.github.simplecollector.data;

import org.soulwing.snmp.SnmpDefaults;

public class SnmpCommunicationSettings {
	private int retries = SnmpDefaults.RETRIES;
	private long timeout = SnmpDefaults.TIMEOUT;

	public SnmpCommunicationSettings() {
	}

	public int getRetries() {
		return retries;
	}

	public void setRetries(int retries) {
		this.retries = retries;
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

}
