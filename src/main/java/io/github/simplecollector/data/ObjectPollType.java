package io.github.simplecollector.data;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class ObjectPollType {
	private List<String> objects;
	private String measurementName;
 
    public ObjectPollType() {
    	super();
    }
    
	public ObjectPollType(String measurementName, List<String> objects) {
		this.measurementName = measurementName;
		this.objects = objects;
	}

	public List<String> getObjects() {
		return objects;
	}

	public void setObjects(List<String> objects) {
		this.objects = objects;
	}

	public String getMeasurementName() {
		return measurementName;
	}

	public void setMeasurementName(String measurementName) {
		this.measurementName = measurementName;
	}
	
}
