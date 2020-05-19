package com.pdfcore.main.model;

import java.util.HashMap;

/**
 * @author OgrisorJ
 * This will hold the data for exactly one record from the data source
 */
public class RecordData {
	
	private Integer recordId;
	private HashMap<Integer,String> values = new HashMap<Integer, String>();
	
	public RecordData()
	{
		
	}
	public void setRecordId(Integer recordId) {
		this.recordId = recordId;
	}
	public Integer getRecordId() {
		return recordId;
	}
	public RecordData(Integer recordId) {
		super();
		this.recordId = recordId;
	}
	public String getValueForMapping(Integer mappingId) {
		if (values.containsKey(mappingId))
			return values.get(mappingId);
		return null;
	}
	public void setValueForMapping(Integer mappingId, String value)
	{
		values.put(mappingId, value);
	}
}
