package io.github.simplecollector.engine;

import java.util.List;

import org.soulwing.snmp.Mib;
import org.soulwing.snmp.SnmpFactory;
import org.soulwing.snmp.SnmpOperation;
import org.soulwing.snmp.SnmpTarget;

import io.github.simplecollector.data.ObjectPollType;
import io.github.simplecollector.data.TablePollType;
import io.github.simplecollector.executor.PollTypeExecutor;

/**
 * Holds information about what needs to be polled where and how often. In
 * addition method {@code createSnmpOperation} returns the {@code SnmpOperation}
 * that can be used to do that actual poll.
 * 
 * Note: this is an object for use in the priority queue, it is not a (data) model object 
 * 
 */
public class PollItem implements Comparable<PollItem> {
	private long nextScheduleTime = 0;
	private Integer interval;
	private SnmpTarget target;
	private PollTypeExecutor<?> executor;
	private Mib mibs;
	private int retries;
	private long timeout;
	
	// Specific information for polltype OBJECT 
	private List<String> objects; // TODO Delete this field and keep everything working (wait until it is removed from all databases)
	private ObjectPollType objectType;
	// Specific information for polltype TABLE
	private TablePollType tableType;
	
	// For meaningful logging 
	private String host;
	private String hostgroup;
	private String pollgroup;
	
	// Indicates that this item is no longer actual and was removed during execution. Will prevent a reschedule.
	private boolean isRemoved = false;
	
	public PollItem() {
		super();
	}

	public boolean isRemoved() {
		return isRemoved;
	}

	public void setRemoved(boolean isRemoved) {
		this.isRemoved = isRemoved;
	}

	public SnmpOperation<?> createInitialOperation() {
		return executor.createInitialOperation(this, SnmpFactory.getInstance().newContext(this.target, this.mibs));
	}
	
	/**
	 * Generates the new {@code nextScheduleTime} based on the interval 
	 */
	public void reschedule() {
		// Note: the interval is a fixed time between the processing of response and start of the next poll.
		// This causes items that have the same start time but different execution/wait time to spread out.
		// Spikes of too high load will flatten automatically in time.
		nextScheduleTime =  System.currentTimeMillis() + (interval * 1000L);
	}
	
	/**
	 * @return A string uniquely identifying this poll item.
	 */
	public String getId() {
		return "[" + host + ":" + hostgroup + ":" + pollgroup + "]";
	}
	
	@Override
	public int compareTo(PollItem other) {
		// Truncation is no issue, don't expect schedule times that are more than a year and a half in the future
		int res = (int) (this.nextScheduleTime - other.nextScheduleTime);
		if (res == 0)
			// TODO: Make a distinction on the time an item is added to the queue so that new items are added after existing items
			return res;
		else
			return res;
	}

	public long getNextScheduleTime() {
		return nextScheduleTime;
	}

	public void setNextScheduleTime(long nextScheduleTime) {
		this.nextScheduleTime = nextScheduleTime;
	}

	public Integer getInterval() {
		return interval;
	}

	public void setInterval(Integer interval) {
		this.interval = interval;
	}

	public SnmpTarget getTarget() {
		return target;
	}

	public void setTarget(SnmpTarget target) {
		this.target = target;
	}

	public PollTypeExecutor<?> getExecutor() {
		return executor;
	}

	public void setExecutor(PollTypeExecutor<?> executor) {
		this.executor = executor;
	}

	public List<String> getObjects() {
		return objects;
	}

	public void setObjects(List<String> objects) {
		this.objects = objects;
	}

	public ObjectPollType getObjectType() {
		return objectType;
	}

	public void setObjectType(ObjectPollType objectType) {
		this.objectType = objectType;
	}

	public Mib getMibs() {
		return mibs;
	}

	public void setMibs(Mib mibs) {
		this.mibs = mibs;
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

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getHostgroup() {
		return hostgroup;
	}

	public void setHostgroup(String hostgroup) {
		this.hostgroup = hostgroup;
	}

	public String getPollgroup() {
		return pollgroup;
	}

	public void setPollgroup(String pollgroup) {
		this.pollgroup = pollgroup;
	}

	public TablePollType getTableType() {
		return tableType;
	}

	public void setTableType(TablePollType tableType) {
		this.tableType = tableType;
	}

}
