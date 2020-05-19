package com.pdfcore.main.model;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;

import com.pdfcore.main.processor.impl.ConfigFormData;


/**
 * @author OgrisorJ
 * This represents the mapping between the data source and data destination 
 * form. It may be db to pdf , cvs to pdf, pdf to cvs.
 *
 */
public class FormMapping {
	
	private final String formName;
	
	/**
	 * The name of the table or file to which the data will be written.
	 * In case of a file the name should containt the extension too.
	 */
	private final String writeToName;
	
	/**
	 * The name of the table or file from which the data will be read
	 * In case of a file the name should containt the extension too.
	 */
	private final String readFromName;

    private final String outputFolderPattern;

    private final String outputFileNameColumn;

	/**
	 * A list of mappings between from fields and to fields.
	 * @see Mapping
	 */
	private final ArrayList<Mapping> mappings = new ArrayList<Mapping>();

    private ConfigFormData configFormData;


    public FormMapping(String formName,String readFromName,String writeToName) {
        this(formName, readFromName, writeToName, null, null);
    }

	public FormMapping(String formName, String readFromName,
                       String writeToName, ConfigFormData configFormData) {
	
		this.formName = formName;
		this.readFromName = readFromName;
		this.writeToName = writeToName;
        this.outputFolderPattern = configFormData.getOutputFolderPattern();
        this.outputFileNameColumn = configFormData.getOutputFileNameColumn();
        this.configFormData = configFormData;

	}


    public FormMapping(String formName, String readFromName,
                           String writeToName, String outputFolderPattern,
                           String outputFileNameColumn) {

    		this.formName = formName;
    		this.readFromName = readFromName;
    		this.writeToName = writeToName;
            this.outputFolderPattern = outputFolderPattern;
            this.outputFileNameColumn = outputFileNameColumn;
    	}



    public String getOutputFileNameColumn() {
        return outputFileNameColumn;
    }

    public String getOutputFolderPattern() {
        return outputFolderPattern;
    }

    public Mapping getMapping(int i) {
		return mappings.get(i);
	}
	
	public Integer getIdForMapping(Mapping mapping)
	{
		return mappings.indexOf(mapping);
	}
	public void addMappings(Mapping mapping) {
		this.mappings.add(mapping);
	}
	
	public int getMappingsNumber() {
		return mappings.size();
	}
	
	
	public String getReadFromName() {
		return readFromName;
	}
	
	public String getWriteToName() {
		return writeToName;
	}
	

	public String getFormName() {
		return formName;
	}
	
	/**
	 * Verifies if the form mapping is valid.
	 * That means:
	 * 1) formName is not null and length >0
	 * 2) writeToName is not null and length >0
	 * 3) readFromName is not null and length >0
	 * 4) mappings is not null and size() > 0
	 * @return a list of errors messages or null if it is valid
	 */
	public ArrayList<String>  getErrors()
	{
		ArrayList<String> errorMessages = new ArrayList<String>();
		if (formName == null || formName.trim().length() == 0)
			errorMessages.add("Form name is null or length 0");
		if (writeToName == null || writeToName.trim().length() == 0)
			errorMessages.add("Write to name is null or length 0");
		if (readFromName == null || readFromName.trim().length() == 0)
			errorMessages.add("Read from name is null or length 0");
		if (mappings == null || mappings.size() == 0)
			errorMessages.add("Form  doesn't contain any mappings");
		if (errorMessages.size()==0)
			return null;
		return errorMessages;
	}

	/**
	 * Verifies if the form has constructed mappings
	 * @return true if it has , false otherwise
	 */
	public boolean hasConstructedMappings()
	{
		for (int i = 0 ; i < mappings.size() ; i++)
		{
			if (mappings.get(i) instanceof ConstructedMapping)
				return true;

		}
		return false;
	}

    public String getNumWordCapital() {
        String cap = "all";
        if (StringUtils.isNotBlank(configFormData.getNumWordCapital())) {
            cap = configFormData.getNumWordCapital();
        }
        return cap.trim();
    }

}
