package io.github.simplecollector.data.api;

import org.springframework.beans.factory.annotation.Value;

import io.github.simplecollector.data.PollType;

public interface PollGroupSummary {
	public String getId();
	public String getName();

	public PollType getType();
	public Integer getInterval();
		
	@Value("#{target.config != null ? target.config.name : null}")
	public String getSnmpConfig();
	
	public String getHost();
	
	@Value("#{target.hosts != null ? target.hosts.name : null}")
	public String getHostsGroup();

}
