package io.github.simplecollector.executor;

import java.util.Arrays;
import java.util.List;
import org.soulwing.snmp.SnmpContext;
import org.soulwing.snmp.SnmpOperation;
import org.soulwing.snmp.VarbindCollection;

import io.github.simplecollector.data.TablePollType;
import io.github.simplecollector.engine.PollItem;

public class InterfaceTableExecutor extends TableExecutor {
	final String measurementName = "interfaces";
	final String[] columns = {
			"sysUpTime",
			"ifName",
			"ifHCInOctets", "ifHCInUcastPkts", "ifHCInMulticastPkts","ifHCInBroadcastPkts",
	        "ifHCOutOctets", "ifHCOutUcastPkts", "ifHCOutMulticastPkts", "ifHCOutBroadcastPkts"
		  };
	// TODO: add ifCounterDiscontinuityTime, ifOutErrors, ifInErrors
	final String tagName = "interface-name";
	final int tagValueColumnIndex = 1; // ifName
	final int nonRepeaters = 1;
	
	@Override
	public SnmpOperation<List<VarbindCollection>> createInitialOperation(PollItem pollItem, SnmpContext context) {
		pollItem.setTableType(new TablePollType(measurementName, Arrays.asList(columns), nonRepeaters, tagName, tagValueColumnIndex));
		return super.createInitialOperation(pollItem, context);
	}


}
