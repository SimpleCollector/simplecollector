package io.github.simplecollector.data;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 
 * The {@code HostGroup} defines a set of hosts that can be identified by a name and id.
 * 
 * @author Johan Boer
 *
 */
@Document
public class HostGroup {
	@Id
	private String name;
	private List<String> hosts;

	public HostGroup() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getHosts() {
		return hosts;
	}

	public void setHosts(List<String> hosts) {
		this.hosts = hosts;
	}

	public void cleanUp() {
		// TODO Auto-generated method stub
		
	}

}
