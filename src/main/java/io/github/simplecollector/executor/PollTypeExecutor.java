package io.github.simplecollector.executor;

import org.influxdb.InfluxDB;
import org.slf4j.Logger;
import org.soulwing.snmp.SnmpContext;
import org.soulwing.snmp.SnmpOperation;
import org.soulwing.snmp.SnmpResponse;

import io.github.simplecollector.engine.PollItem;
import io.github.simplecollector.engine.SnmpPollerEngine;


public interface PollTypeExecutor<V> {
	static final Logger logger = SnmpPollerEngine.logger;
	static final String POLLGROUP_TAG = "pollgroup";
	static final String HOSTGROUP_TAG = "hostgroup";
	static final String HOST_TAG = "host";
	static final String DATABASE = "simplecollector";

	
	/**
	 * {@code createInitialOperation} is called when it is time to execute a poll.
	 * 
	 * @param pollItem Note: the pollItem may not be stored in a class variable because it will create a circular reference
	 *                       that breaks the garbage collection.
	 * @param context
	 * @return
	 */
	public SnmpOperation<V> createInitialOperation(PollItem pollItem, SnmpContext context);
	
	/**
	 * {@code SnmpOperation} processes the SNMP response that has been received.
	 * 
	 * @param pollItem Note: the pollItem may not be stored in a class variable because it will create a circular reference
	 *                       that breaks the garbage collection.
	 * @param snmpContext 
	 * @param response
	 * @param influxDB 
	 * @return if an {@code SnmpOperation} is returned the operation will be executed asynchronously and
	 *         {@code processSnmpResponse} will be called when a response is received.
	 *         When a {@code null} is returned a next poll will be scheduled and placed on the queue.
	 */
	public SnmpOperation<V> processSnmpResponse (PollItem pollItem, SnmpContext snmpContext, SnmpResponse<V> response, InfluxDB influxDB);
}
