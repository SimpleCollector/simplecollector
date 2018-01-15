package io.github.simplecollector.data;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;


/**
 * 
 * The {@code PollGroup} defines what needs to be polled (objects), how often (interval) and where (hosts).
 * 
 * @author Johan Boer
 *
 */
@Document
public class PollGroup {
	@Id
	private String id;
	@Indexed(unique = true)
	private String name;
	private PollType type;
	private List<String> objects;
	private List<String> mibs;
	private Integer interval;
	@DBRef(lazy = false)
	private SnmpConfigGroup config;
	
	// The information related to the type (e.g. table, object)
	private TablePollType tableType;
	private ObjectPollType objectType;
	
	/*
	 * Either host or hosts can be used to specify one or more target systems
	 */
	private String host;
	@DBRef
	private HostGroup hosts;
	
	/**
	 * Set object references to null values when they are not relevant.
	 */
	public void cleanUp() {
		if (host != null && !host.isEmpty())
			hosts =null;
		switch (type) {
		case OBJECT:
			tableType = null;
			break;
		case INTERFACES:
			objectType = null;
			break;
		default:
			break;
		}
	}
	
	public PollGroup() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public PollType getType() {
		return type;
	}

	public void setType(PollType type) {
		this.type = type;
	}

	public List<String> getObjects() {
		return objects;
	}

	public void setObjects(List<String> objects) {
		this.objects = objects;
	}

	public List<String> getMibs() {
		return mibs;
	}

	public void setMibs(List<String> mibs) {
		this.mibs = mibs;
	}

	public Integer getInterval() {
		return interval;
	}

	public void setInterval(Integer interval) {
		this.interval = interval;
	}

	public SnmpConfigGroup getConfig() {
		return config;
	}

	public void setConfig(SnmpConfigGroup config) {
		this.config = config;
	}

	public HostGroup getHosts() {
		return hosts;
	}

	public void setHosts(HostGroup hosts) {
		this.hosts = hosts;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public TablePollType getTableInfo() {
		return tableType;
	}

	public void setTableInfo(TablePollType tableInfo) {
		this.tableType = tableInfo;
	}

	public ObjectPollType getObjectType() {
		return objectType;
	}

	public void setObjectType(ObjectPollType objectType) {
		this.objectType = objectType;
	}

}
