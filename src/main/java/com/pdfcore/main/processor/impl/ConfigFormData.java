package com.pdfcore.main.processor.impl;

import java.util.HashMap;
import java.util.Map;

import com.pdfcore.main.interfaces.FieldValueConstructor;
import com.pdfcore.main.model.enums.FileMappingType;
import com.pdfcore.main.processor.enums.PageSize;

/**
 * @author OgrisorJ Used to store information about the forms that you will
 *         generate, values read from configuration file
 */
public class ConfigFormData {

	private String formName;
	private int norpp = 1;
	private int numberOfPage = 1;
	private int numberOfReplicateRecord = 1;
	private PageSize pageSize = PageSize.LETTER;
	private String readerFromName;
	private String writerToName;
	private FieldValueConstructor valueConstrucor = null;
	private String fileMappingType = FileMappingType.PROPERTIES.toString();
	private boolean autoIncreaseFieldIndex = false;
	private boolean autoIncreasePageIndex = false;

    private String outputFolderPattern;

    private Map<String, String> wcMap = new HashMap<String, String>();
    private String outputFileNameColumn;
    private String numWordCapital;


    public ConfigFormData(String formName) {
		this.setFormName(this.setReaderFromName(this.setWriterToName(formName)));
	}

    public String getOutputFolderPattern() {
        return outputFolderPattern;
    }

    public void setOutputFolderPattern(String outputFolderPattern) {
        this.outputFolderPattern = outputFolderPattern;
    }

    public void setFormName(String formName) {
		this.formName = formName;
	}

	public String getFormName() {
		return formName;
	}

	public void setNorpp(int norpp) {
		this.norpp = norpp;
	}

	public int getNorpp() {
		return norpp;
	}

	public void setPageSize(PageSize pageSize) {
		this.pageSize = pageSize;
	}

	public PageSize getPageSize() {
		return pageSize;
	}

	public String setReaderFromName(String readerFromName) {
		this.readerFromName = readerFromName;
		return readerFromName;
	}

	public String getReaderFromName() {
		return readerFromName;
	}

	public String setWriterToName(String writerToName) {
		this.writerToName = writerToName;
		return writerToName;
	}

	public String getWriterToName() {
		return writerToName;
	}

	public void setValueConstrucor(FieldValueConstructor valueConstrucor) {
		this.valueConstrucor = valueConstrucor;
	}

	public FieldValueConstructor getValueConstrucor() {
		return valueConstrucor;
	}

	/**
	 * @return the fileMappingType
	 */
	public String getFileMappingType() {
		return fileMappingType;
	}

	/**
	 * @param fileMappingType
	 *            the fileMappingType to set
	 */
	public void setFileMappingType(String fileMappingType) {
		this.fileMappingType = fileMappingType;
	}

	/**
	 * @return the autoIncreaseFieldIndex
	 */
	public boolean isAutoIncreaseFieldIndex() {
		return autoIncreaseFieldIndex;
	}

	/**
	 * @param autoIncreaseFieldIndex
	 *            the autoIncreaseFieldIndex to set
	 */
	public void setAutoIncreaseFieldIndex(boolean autoIncreaseFieldIndex) {
		this.autoIncreaseFieldIndex = autoIncreaseFieldIndex;
	}

	/**
	 * @param string
	 */
	public void setAutoIncreaseFieldIndex(String val) {
		this.autoIncreaseFieldIndex = Boolean.parseBoolean(val);
	}

	/**
	 * @return the autoIncreasePageIndex
	 */
	public boolean isAutoIncreasePageIndex() {
		return autoIncreasePageIndex;
	}

	/**
	 * @param autoIncreasePageIndex
	 *            the autoIncreasePageIndex to set
	 */
	public void setAutoIncreasePageIndex(boolean autoIncreasePageIndex) {
		this.autoIncreasePageIndex = autoIncreasePageIndex;
	}

	/**
	 * @param val
	 */
	public void setAutoIncreasePageIndex(String val) {
		this.autoIncreasePageIndex = Boolean.parseBoolean(val);
	}

	/**
     * @return the numberOfPage
     */
    public int getNumberOfPage() {
    	return numberOfPage;
    }

	/**
     * @return the numberOfReplicateRecord
     */
    public int getNumberOfReplicateRecord() {
    	return numberOfReplicateRecord;
    }

	/**
     * @param numberOfReplicateRecord the numberOfReplicateRecord to set
     */
    public void setNumberOfReplicateRecord(int numberOfReplicateRecord) {
    	this.numberOfReplicateRecord = numberOfReplicateRecord;
    }

	/**
     * @param numberOfPage the numberOfPage to set
     */
    public void setNumberOfPage(int numberOfPage) {
    	this.numberOfPage = numberOfPage;
    }


    public void addWhereCondition(String prop, String val) {
        wcMap.put(prop, val);
    }


    public Map<String, String>  getWhereConditionMap() {
        return wcMap;

    }

    public void setOutputFileNameColumn(String outputFileNameColumn) {
        this.outputFileNameColumn = outputFileNameColumn;
    }

    public String getOutputFileNameColumn() {
        return outputFileNameColumn;
    }

    public void setNumWordCapital(String numWordCapital) {
        this.numWordCapital = numWordCapital;
    }

    public String getNumWordCapital() {
        return numWordCapital;
    }
}
