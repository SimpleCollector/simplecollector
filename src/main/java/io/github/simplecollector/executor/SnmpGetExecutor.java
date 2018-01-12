package io.github.simplecollector.executor;

import org.influxdb.InfluxDB;
import org.soulwing.snmp.SnmpContext;
import org.soulwing.snmp.SnmpOperation;
import org.soulwing.snmp.SnmpResponse;
import org.soulwing.snmp.VarbindCollection;

import io.github.simplecollector.engine.PollItem;

public class SnmpGetExecutor implements PollTypeExecutor<VarbindCollection> {
	// TODO Use objectType from pollItem and write results to the database
	
	@Override
	public SnmpOperation<VarbindCollection> createInitialOperation(PollItem pollItem, SnmpContext context) {
		if (logger.isDebugEnabled()) {
			logger.info("Creating initial snmp operations for " + pollItem.getHost() + " from host group " + pollItem.getHostgroup() +
			             " and poll group " + pollItem.getPollgroup());
		}
		return context.newGetNext(pollItem.getObjects());
	}

	@Override
	public SnmpOperation<VarbindCollection> processSnmpResponse(PollItem pollItem, SnmpContext snmpContext, SnmpResponse<VarbindCollection> response, InfluxDB influxDB) {
		if (logger.isDebugEnabled()) {
			logger.info("Processing response from " + pollItem.getHost() + " part of host group " + pollItem.getHostgroup() +
				    	" and for poll group " + pollItem.getPollgroup());
		}
		VarbindCollection result = response.get();
		logger.info("Result for " + pollItem.getId() + " = " + result.toString());

		return null;
	}

}
