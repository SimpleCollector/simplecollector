package io.github.simplecollector.engine;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soulwing.snmp.Mib;
import org.soulwing.snmp.MibFactory;
import org.soulwing.snmp.ModuleParseException;
import org.springframework.stereotype.Component;


/**
 * A single component to hold all loaded mibs.
 *
 */
@Component
public class MibManager {
	private static final Logger logger = LoggerFactory.getLogger(MibManager.class);

	private Mib mib;
	
	public Mib getMib() {
		return mib;
	}

	public void setMib(Mib mib) {
		this.mib = mib;
	}

	/**
	 * A single component to hold all loaded mibs.
	 */
	public MibManager() {
		this.mib = MibFactory.getInstance().newMib();
	}
	
	/**
	 * @param mibName The name of the MIB to load
	 * @return true in case the mibs are sucessfully loaded, false in case of error.
	 */
	public boolean loadMib(String mibName) {
			try {
				mib.load(mibName);
			} catch (ModuleParseException e) {
				logger.error("Error parsing mib " + mibName + ": " + e.getMessage());
				return false;
			} catch (IOException e) {
				logger.error("Error loading mib " + mibName + ": " + e.getMessage());
				return false;
			}

		return true;
	}
	
	/**
	 * @param mibs A list with the names of the MIBs to load
	 * @return true in case the mibs are sucessfully loaded, false in case of error.
	 */
	public boolean loadMibs(List<String> mibs) {
		boolean succes = true;
		for (String mibName : mibs) {
			succes = loadMib(mibName);
		}

		return succes;
	}


}
