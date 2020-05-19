package com.pdfcore.main.processor.impl;

import java.util.ArrayList;

import com.pdfcore.main.processor.enums.MergePdfConfig;
import com.pdfcore.main.processor.enums.OutputDestination;
import com.pdfcore.main.processor.enums.OutputLevel;
import com.pdfcore.main.processor.enums.ReadFromEnum;
import com.pdfcore.main.processor.enums.WriteToEnum;

public enum BatchConfigFromFile {
    INSTANCE;

	private ReadFromEnum readFrom;
	private WriteToEnum writeTo;
	private String pkReader = null;
	private String pkWriter = null;
	private MergePdfConfig merge = MergePdfConfig.BOTH;
	private OutputDestination output = OutputDestination.FILE;
	private OutputLevel level;
	private boolean shouldOverwrite = false;
	private ArrayList<String> ids = null;
	//private static BatchConfigFromFile instance = new BatchConfigFromFile();
	
	private ArrayList<ConfigFormData> formsToGenerate = new ArrayList<ConfigFormData>();


    private String mergedFileName;




	/**
	 * 
	 */
	private BatchConfigFromFile() {
	}

	/**
	 * @return the instance
	 */
	public static BatchConfigFromFile getInstance() {

        if (INSTANCE.ids == null) {
            INSTANCE.ids = new ArrayList<String>();
        } else {
            INSTANCE.ids.clear();
        }

        //instance.formsToGenerate.clear();
		return INSTANCE;
	}
	
	public void setReadFrom(ReadFromEnum readFrom) {
		this.readFrom = readFrom;
	}
	public ReadFromEnum getReadFrom() {
		return readFrom;
	}
	public void setWriteTo(WriteToEnum writeTo) {
		this.writeTo = writeTo;
	}
	public WriteToEnum getWriteTo() {
		return writeTo;
	}
	public void addFormToGenerate(ConfigFormData form)
	{
		formsToGenerate.add(form);
	}
	public ArrayList<ConfigFormData> getFormsToGenerate()
	{
		return formsToGenerate;
	}
	public void setPkReader(String pkReader) {
		this.pkReader = pkReader;
	}
	public String getPkReader() {
		return pkReader;
	}
	public void setPkWriter(String pkWriter) {
		this.pkWriter = pkWriter;
	}
	public String getPkWriter() {
		return pkWriter;
	}
	public void setMerge(MergePdfConfig merge) {
		this.merge = merge;
	}
	public MergePdfConfig getMerge() {
		return merge;
	}
	public void setOutput(OutputDestination output) {
		this.output = output;
	}
	public OutputDestination getOutput() {
		return output;
	}
	public void setLevel(OutputLevel level) {
		this.level = level;
	}
	public OutputLevel getLevel() {
		return level;
	}
	public void setShouldOverwrite(boolean shouldOverwrite) {
		this.shouldOverwrite = shouldOverwrite;
	}
	public boolean isShouldOverwrite() {
		return shouldOverwrite;
	}
	public void addId(String id) {
		if (this.ids == null)
			this.ids = new ArrayList<String>();
		this.ids.add(id);
	}
	public ArrayList<String> getIds() {
		return ids;
	}


    public String getMergedFileName() {
        return mergedFileName;
    }

    public void setMergedFileName(String mergedFileName) {
        this.mergedFileName = mergedFileName;
    }


}
