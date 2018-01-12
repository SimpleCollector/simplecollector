package io.github.simplecollector.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soulwing.snmp.SimpleSnmpV2cTarget;
import org.soulwing.snmp.SimpleSnmpV3Target;
import org.soulwing.snmp.SnmpTarget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.github.simplecollector.data.HostGroup;
import io.github.simplecollector.data.PollGroup;
import io.github.simplecollector.data.PollGroupRepository;
import io.github.simplecollector.data.SnmpConfigGroup;
import io.github.simplecollector.executor.InterfaceTableExecutor;
import io.github.simplecollector.executor.PollTypeExecutor;
import io.github.simplecollector.executor.SnmpGetExecutor;
import io.github.simplecollector.executor.TableExecutor;

@Component
public class PollQueueManager extends PriorityBlockingQueue<PollItem> {
	private static final long serialVersionUID = -7402792184332152726L;
	private static final Logger logger = LoggerFactory.getLogger(PollQueueManager.class);
	
	private AtomicBoolean isUpdating = new AtomicBoolean(false);
	// FIXME using string as key does not allow the same item id to exist twice
	private final Map<String, PollItem> executing = new ConcurrentHashMap<String, PollItem>();
	private final Map<String, PollItem> addedDuringUpdate = new ConcurrentHashMap<String, PollItem>();

	@Autowired
	private MibManager mibs;
	@Autowired
	private PollGroupRepository pgRepository;

	public PollQueueManager() {
		super();
	}

	public boolean getIsUpdating() {
		return isUpdating.get();
	}

	public void setIsUpdating(boolean isUpdating) {
		this.isUpdating.set(isUpdating);
	}
	
	public void trackExecutionStart (PollItem item) {
		executing.put(item.getId(), item);
	}
	
	public void trackExecutionEnd (PollItem item) {
		executing.remove(item.getId());
	}
		
	public void loadQueue() {
		if (isUpdating.getAndSet(true)) {
			return;
		}
		List<PollItem> items = loadPollItems();
		for (PollItem item : items) {
			// Fill next schedule time and add to polling queue
			item.reschedule();
			if (!super.add(item)) {
				logger.error("Error, failed to add poll item " + item.getId() + " to the queue");
			}
			
		}
		isUpdating.set(false);
		// No need to do this because loadQueue is the initial loading
		addedDuringUpdate.clear();
	}


	@SuppressWarnings("incomplete-switch")
	public List<PollItem> loadPollItems() {
		List<PollItem> items = new ArrayList<PollItem>();
		
		List<PollGroup> pgs = pgRepository.findAll();
		for (PollGroup pg : pgs) {
			if (!mibs.loadMibs(pg.getMibs())) {
				logger.error("Error loading MIBs for poll group " + pg.getName() + " , polling will not start");
				continue;
			}
			
			// Create PollTypeExecutor object to use in multiple poll items
			PollTypeExecutor<?> executor;
			switch (pg.getType()) {
			case OBJECT:
				executor = new SnmpGetExecutor();
				break;
			case TABLE:
				executor = new TableExecutor();
				break;
			case INTERFACES:
				executor = new InterfaceTableExecutor();
				break;
			default:
				logger.error("Error loading poll group " + pg.getName() + " , polling will not start");
				continue;
			}
			
			HostGroup hg = pg.getHosts();
			if (pg.getHost() != null) {
				hg = new HostGroup();
				hg.setHosts(Arrays.asList(pg.getHost()));
			}
			if (hg != null) {
				for  (String host : hg.getHosts()) {
					// In this block the item gets filled
					
					PollItem item = new PollItem();
					item.setHost(host);
					item.setHostgroup(hg.getName());
					item.setPollgroup(pg.getName());
					item.setInterval(pg.getInterval());
					item.setMibs(mibs.getMib());
					item.setExecutor(executor);
					
					SnmpConfigGroup cfg = pg.getConfig();
					item.setRetries(cfg.getComsettings().getRetries());
					item.setTimeout(cfg.getComsettings().getTimeout());
					
					switch (pg.getType()) {
					case OBJECT:
						item.setObjects(pg.getObjects()); // TODO Assign objectType
						break;
					case TABLE:
						item.setTableType(pg.getTableInfo());
						break;
					}

					// Create the SNMP target
					SnmpTarget target;
					switch (cfg.getAuth().getVersion()) {
					case "2c":
						target = new SimpleSnmpV2cTarget();
						((SimpleSnmpV2cTarget) target).setAddress(host);
						((SimpleSnmpV2cTarget) target).setCommunity(cfg.getAuth().getCommunity());
						break;
					case "3":
						target = new SimpleSnmpV3Target();
						((SimpleSnmpV3Target) target).setAddress(host);
						((SimpleSnmpV3Target) target).setAuthType(cfg.getAuth().getAuthType());
						((SimpleSnmpV3Target) target).setAuthPassphrase(cfg.getAuth().getAuthPassphrase());
						((SimpleSnmpV3Target) target).setPrivType(cfg.getAuth().getPrivType());
						((SimpleSnmpV3Target) target).setPrivPassphrase(cfg.getAuth().getPrivPassphrase());
						((SimpleSnmpV3Target) target).setSecurityName(cfg.getAuth().getSecurityName());
						((SimpleSnmpV3Target) target).setScope(cfg.getAuth().getScope());
						break;
					default:
						target = null;
						break;
					}
					if (target == null) {
						logger.error("Error loading poll group " + pg.getName() + ": incorrect SNMP version");
						// continue with next pollgroup
						break;
					}
					item.setTarget(target);
					
					// Fill next schedule time and add to the list
					item.reschedule();
					items.add(item);
				}
			}			
		}
		
		return items;
	}
	
	/**
	 * Load a new set of poll items and replace the current queue with it.
	 */
	public void reloadQueue() {
		if (!replaceQueue(loadPollItems()) && logger.isInfoEnabled())
			logger.info("Reloaded queue: " + size() + " items on the queue");
	}

	/**
	 * Replace the current queue with the given items.
	 * 
	 * @param items
	 * @return True if the queue has been replaced. Will be false in case a replace is already taking place.
	 */
	public boolean replaceQueue(List<PollItem> items) {
		if (isUpdating.getAndSet(true)) {
			// An update is already taking place
			return false;
		}
		
		// Remove all items from the queue to prevent a new poll to start
		HashMap<String, PollItem> oldqueue = new HashMap<>();
		for (PollItem item = super.poll(); item != null; item = super.poll()) {
			oldqueue.put(item.getId(), item);
		}
		
		// Modify all item being executed so that they will not be rescheduled
		executing.forEach(new BiConsumer<String, PollItem>() {
			@Override
            public void accept(String id, PollItem item) {
                  item.setRemoved(true);
			}
		});
		
		// Process all newly loaded items and copy the schedule time from the old queue if it was on that queue
		for (PollItem item : items) {
			PollItem old = oldqueue.get(item.getId());
			if (old != null && item.getInterval().equals(old.getInterval())) {
				item.setNextScheduleTime(old.getNextScheduleTime());
			} else {
				old = addedDuringUpdate.get(item.getId());
				if (old != null && item.getInterval().equals(old.getInterval())) {
					// Item was added to the queue during the update (slipped through marking the executing items)
					item.setNextScheduleTime(old.getNextScheduleTime());
				}
			}
			if (!super.add(item)) {
				logger.error("Error, failed to add poll item " + item.getId() + " to the queue");
			}
				
		}
		
		isUpdating.set(false);
		addedDuringUpdate.clear();
		
		return true;
	}
	
	public List<String> listItemIds() {
		List<String> ids = new ArrayList<>();
		
		forEach(new Consumer<PollItem>() {
			@Override
            public void accept(PollItem item) {
                  ids.add(item.getId());
			}
		});
		
		return ids;
	}
	
	@Override
	public boolean add(PollItem item) {
		// Only add the item to the queue when it has not been marked for removal
		if (item.isRemoved()) {
			// Just want to silently remove it without an error message so return true
			return true;
		}
		if (isUpdating.get()) {
			addedDuringUpdate.put(item.getId(), item);
			// The item will be added later from the method performing the update so don't give an error
			return true;
		}
		return super.add(item);
	}
	
	/**
	 * 	Retrieves and removes the head of this queue unless there is an update of the queue is taking place. Then a
	 *  null value is returned.
	 *  If there is no update taking place and the the queue is empty waiting takes place until an element becomes available.
	 */
	@Override
	public PollItem take() throws InterruptedException {
		// If an update is going on just return null, no waiting
		if (isUpdating.get())
			return null;
		
		return super.take();
	}
	
	@Override
	public PollItem peek() {
		// If an update is going on pretend the queue is empty
		if (isUpdating.get())
			return null;
		
		return super.peek();
	}
	
	@Override
	public PollItem poll() {
		// If an update is going on pretend the queue is empty
		if (isUpdating.get())
			return null;
		
		return super.poll();
	}

	@Override
	public PollItem poll(long timeout, TimeUnit unit) throws InterruptedException {
		// If an update is going on just return null, no waiting
		if (isUpdating.get())
			return null;

		return super.poll(timeout, unit);
	}

	@Override
	public int size() {
		// If an update is going on pretend the queue is empty
		if (isUpdating.get())
			return 0;
		
		return super.size();
	}

}
