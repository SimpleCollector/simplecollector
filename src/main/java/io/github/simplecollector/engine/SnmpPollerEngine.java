package io.github.simplecollector.engine;

import java.util.concurrent.atomic.AtomicLong;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.soulwing.snmp.SimpleSnmpTargetConfig;
import org.soulwing.snmp.SnmpContext;
import org.soulwing.snmp.SnmpFactory;
import org.soulwing.snmp.SnmpOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.stereotype.Component;


@Component
public class SnmpPollerEngine implements Runnable {
	public static final Logger logger = LoggerFactory.getLogger(SnmpPollerEngine.class);
	
	//Value("${simplecollector.influxdb.db:simplecollector}")
	public static String tsdbName = "simplecollector";
	@Value("${simplecollector.influxdb.url:http://127.0.0.1:8086}")
	private String influxDbUrl;

	@Autowired
	private PollQueueManager queue;
	static final AtomicLong seq = new AtomicLong(0);
	private Thread thread;

	private InfluxDB influxDB;
	
	
	public SnmpPollerEngine() {
	}
			
	private InfluxDB initializeTSDB() {
		// TODO: Connect to InfluxDb with authentication
		influxDB = InfluxDBFactory.connect(influxDbUrl);
		influxDB.createDatabase(tsdbName);
		influxDB.disableBatch();
		
		return influxDB;
	}
	
	public void run() {
		// Store the thread for later interruption
		thread = Thread.currentThread();

		try {
			initializeTSDB();
			logger.info("Storing measurements in an influx database version " + influxDB.version());
		} catch (Exception e) {
			logger.error("Error, could not connect to InfluxDB: " + e.getMessage());
		}
		
		queue.loadQueue();
		logger.info("Populated polling queue from database configuration: " + queue.size() + " items");
		
				
		try {
			while (!thread.isInterrupted()) {
				// Wait until it's time to execute the next event in the queue
				// A simple sleep loop consumes limited cpu and is less error prone than waking up the thread in all situations
				PollItem nextItem;
				long delta = Long.MAX_VALUE;
				do {
					nextItem = queue.peek();
					if (nextItem != null) {
						// Determine how long to sleep until the next poll
						delta = nextItem.getNextScheduleTime() - System.currentTimeMillis();
					}
					if (nextItem == null || delta > 0) {
						// Sleep no more than 1 second
						Thread.sleep(delta < 1000 ? delta : 1000L);
					}
				} while (delta > 0);
				
				// Remove the first item of the queue and execute
				nextItem = queue.take();
				// In case an update started just after the above code determined it is time to start a new poll nextItem will be null
				if (nextItem == null)
					continue;
				long toolate = System.currentTimeMillis() - nextItem.getNextScheduleTime();
				if (toolate > 1000) {
					logger.warn("SNMP operation on " + nextItem.getHost() + " from host group " + nextItem.getHostgroup() +
					    " and poll group " + nextItem.getPollgroup() + " starts " + toolate/1000 + "s too late");
				}
				SimpleSnmpTargetConfig config = new SimpleSnmpTargetConfig();
			    config.setRetries(nextItem.getRetries());
			    config.setTimeout(nextItem.getTimeout() * 1000);
				SnmpContext context = SnmpFactory.getInstance().newContext(nextItem.getTarget(), nextItem.getMibs(), config, null);
				SnmpOperation<?> operation = nextItem.getExecutor().createInitialOperation(nextItem, context);
				// TODO: periodically try to reconnect to influxdb when not connected
				operation.invoke(new PollCallback<>(nextItem, queue, influxDB));
			}
		} catch (InterruptedException e) { 
			logger.info("SNMP poller engine is interrupted: polling stopped");
	    } catch (Exception e) {
			logger.error("SNMP poller engine encountered an exception: polling stopped", e);
		}
		thread = null;
	}

	public InfluxDB getInfluxDB() {
		return influxDB;
	}

	public void setInfluxDB(InfluxDB influxDB) {
		this.influxDB = influxDB;
	}

	/*
	 * Stop the SnmpPoller thread when the application context is closed
	 */
	@EventListener
	public void handleContextClosed(ContextClosedEvent event) {
		logger.info("DETECTED CONTEXT CLOSED; stopping PollinScheduler thread");
		if (thread != null) {
			thread.interrupt();
			thread = null;
		}
	}

	/*
	 * Start SnmpPoller in a separate thread when springboot signals that the application has started
	 */
	@EventListener
	public void handleApplicationReady(ApplicationReadyEvent event) {
		logger.info("Application is ready, starting SNMP poller engine");
		new SimpleAsyncTaskExecutor("SnmpPoller-").execute(this);
	}

}
