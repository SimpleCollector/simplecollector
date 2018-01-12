package io.github.simplecollector.data.api;

import org.springframework.beans.factory.annotation.Value;

public interface NameKeyValue {
	@Value("#{target.name}")
	public String getKey();
	
	@Value("#{target.name}")
	public String getValue();
}
