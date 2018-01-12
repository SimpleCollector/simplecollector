package io.github.simplecollector.executor;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDB.ConsistencyLevel;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.soulwing.snmp.SnmpContext;
import org.soulwing.snmp.SnmpOperation;
import org.soulwing.snmp.SnmpResponse;
import org.soulwing.snmp.Varbind;
import org.soulwing.snmp.VarbindCollection;

import io.github.simplecollector.data.TablePollType;
import io.github.simplecollector.engine.PollItem;

public class TableExecutor implements PollTypeExecutor<List<VarbindCollection>> {
	final int maxRepetitions = 20;
	
	@Override
	public SnmpOperation<List<VarbindCollection>> createInitialOperation(PollItem pollItem, SnmpContext context) {
		TablePollType conf = pollItem.getTableType();
		return context.newGetBulk(conf.getNonRepeaters(), maxRepetitions, conf.getColumns());
	}

	/**
	 * All columns of the table are stored in one measurement that is identified by the measurement name configured in the poll item.
	 * Every row will be identified with a tag, the value of those tags comes from the column values as identified in tagIndex.
	 */
	@Override
	public SnmpOperation<List<VarbindCollection>> processSnmpResponse(PollItem pollItem, SnmpContext context, SnmpResponse<List<VarbindCollection>> response, InfluxDB influxDB) {
		List<VarbindCollection> rows = response.get();
		
		if (rows.isEmpty())
			return null;
		
		org.influxdb.dto.BatchPoints.Builder batchBuilder = BatchPoints
				.database(DATABASE)
				.tag(HOST_TAG, pollItem.getHost())
				.tag(POLLGROUP_TAG, pollItem.getPollgroup())
				.retentionPolicy("autogen")
				.consistency(ConsistencyLevel.ALL);
		if (pollItem.getHostgroup() != null)
			batchBuilder.tag(HOSTGROUP_TAG, pollItem.getHostgroup());
		BatchPoints batchPoints = batchBuilder.build();
		
		TablePollType conf = pollItem.getTableType();
		String[] columns = (String[]) conf.getColumns().toArray();
		
		long timestamp = System.currentTimeMillis();
		
		VarbindCollection lastRow = null;
	    for (VarbindCollection row : rows) {
	    	if (logger.isDebugEnabled())
	    		logger.debug("Response from " + pollItem.getHost() + " row: " + row);
	    	if (row.get(columns[conf.getNonRepeaters()]) == null) {
	    		// The end of the table has been reached
	    		writeToInfluxDB(influxDB, batchPoints);
	    		return null;
	    	}
	    	if (row.size() < columns.length) {
	    		// Truncated row, don't process it and issue the next getbulk if it was not the first row
	    		continue;
	    	}
	    	lastRow = row;
	    	// Every row is an interface that is stored as a separate measurement with the interface name as tag
	    	Builder measurement = Point.measurement(conf.getMeasurementName())
		    		             .time(timestamp, TimeUnit.MILLISECONDS)
		    		             .tag(conf.getTagName(), row.get(columns[conf.getTagValueColumnIndex()]).asString());
	    	for (int i = conf.getNonRepeaters(); i < columns.length; i++) {
	    		if (i == conf.getTagValueColumnIndex())
	    			continue;
	    		String label = columns[i];
				Varbind column = row.get(label);
				// Some agents don't return all requested objects, skip it in that case
				if (column == null) {
					if (logger.isDebugEnabled())
						logger.debug("Response from " + pollItem.getHost() + " is missing object " + label + " for interface " + row.get(columns[conf.getTagValueColumnIndex()]).asString());
					continue;
				}
				measurement.addField(label, column.asLong());
			}
			batchPoints.point(measurement.build());
	    }
	    writeToInfluxDB(influxDB, batchPoints);
	    
	    if (lastRow == null) {
	    	// This is the first row of the result and it is truncated
	    	logger.error("Cannot collect interface information for " + pollItem.getId() + " because the SNMP agent cannot return a single complete row of the interface table");
	    	return null;
	    } 
	    
	    // Return the next objects to retrieve 
	    return context.newGetBulk(conf.getNonRepeaters(), maxRepetitions, lastRow.nextIdentifiers(Arrays.copyOfRange(columns, 0, conf.getNonRepeaters())));
				
	}
	
	private void writeToInfluxDB (InfluxDB influxDB, BatchPoints batchPoints) {
		if (influxDB == null) {
			logger.warn("Not connected to InfluxDB so not storing results: " + batchPoints.toString());
		}
		try {
			influxDB.write(batchPoints);
		} catch (Exception e) {
			logger.error("Error writing to InfluxDB: " + e.getMessage());
			logger.debug("Measurements not stored : " + batchPoints.toString());
		}
	}

}
