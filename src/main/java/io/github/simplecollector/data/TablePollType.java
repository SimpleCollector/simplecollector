package io.github.simplecollector.data;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class TablePollType {
	private List<String> columns;
	private String measurementName;
	private String tagName;
	private Integer tagValueColumnIndex; // Starts with 0
    private Integer nonRepeaters = 0;
 
    public TablePollType() {
    	super();
    }
    
	public TablePollType(String measurementName, List<String> columns, Integer nonRepeaters, String tagName, Integer tagValueColumnIndex) {
		this.measurementName = measurementName;
		this.columns = columns;
		this.nonRepeaters = nonRepeaters;
		this.tagName = tagName;
		this.tagValueColumnIndex = tagValueColumnIndex;
	}

	public List<String> getColumns() {
		return columns;
	}

	public void setColumns(List<String> columns) {
		this.columns = columns;
	}

	public String getMeasurementName() {
		return measurementName;
	}

	public void setMeasurementName(String measurementName) {
		this.measurementName = measurementName;
	}

	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public Integer getTagValueColumnIndex() {
		return tagValueColumnIndex;
	}

	public void setTagValueColumnIndex(Integer tagValueColumnIndex) {
		this.tagValueColumnIndex = tagValueColumnIndex;
	}

	public Integer getNonRepeaters() {
		return nonRepeaters;
	}

	public void setNonRepeaters(Integer nonRepeaters) {
		this.nonRepeaters = nonRepeaters;
	}
	
}
