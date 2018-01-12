package io.github.simplecollector.engine;



import org.influxdb.InfluxDB;
import org.slf4j.Logger;
import org.soulwing.snmp.SnmpCallback;

import org.soulwing.snmp.SnmpEvent;
import org.soulwing.snmp.SnmpException;
import org.soulwing.snmp.SnmpOperation;
import org.soulwing.snmp.TimeoutException;

import io.github.simplecollector.executor.PollTypeExecutor;

public class PollCallback<V> implements SnmpCallback<V> {
	private final Logger logger = SnmpPollerEngine.logger;
	private PollItem pollItem;
	private PollQueueManager pollQueue;
	private InfluxDB influxDB;

	/**
	 * Constructor of PollCallback for use in an asynchronous SNMP operation.
	 * On creation of the instance the start of the execution is tracked in @param pollQueue via trackExecutionStart.
	 * When the response or timeout is processed the end of execution is recorded via trackExecutionEnd. 
	 * 
	 */
	public PollCallback(PollItem pollItem, PollQueueManager pollQueue, InfluxDB influxDB) {
		super();
		this.pollItem = pollItem;
		this.pollQueue = pollQueue;
		this.influxDB = influxDB;
		pollQueue.trackExecutionStart(pollItem);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onSnmpResponse(SnmpEvent<V> event) {
		SnmpOperation<V> nextOperation = null;
		
		try {
			nextOperation = ((PollTypeExecutor<V>) pollItem.getExecutor()).processSnmpResponse(pollItem, event.getContext(), event.getResponse(), influxDB);
		} catch (TimeoutException exception) {
			logger.error("SNMP timeout for " + pollItem.getId());
			nextOperation = null;
		} catch (SnmpException exception) {
			logger.error("SNMP error ", exception);
			nextOperation = null;
		} finally {
			if (nextOperation != null) {
				nextOperation.invoke((SnmpCallback<V>) this);
			} else {
				pollQueue.trackExecutionEnd(pollItem);
				pollItem.reschedule();
				if (!pollQueue.add(pollItem)) {
					logger.error("Failed to add poll item to the queue");
				}
				// FIXME:Investigate problem creating a new context due to closing it in the callback.
				//event.getContext().close();
	
			}
		}
	}

}
