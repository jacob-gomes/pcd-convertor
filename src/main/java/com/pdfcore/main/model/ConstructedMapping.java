package com.pdfcore.main.model;

import com.pdfcore.main.model.enums.MappingType;

public class ConstructedMapping extends Mapping {

	private boolean isMultipleFieldConcat;
	private boolean formatField;

	public ConstructedMapping(String readerField, String writerField) {
		super(readerField, writerField);
		this.isMultipleFieldConcat = readerField != null && readerField.contains("|");
		this.formatField = readerField != null && readerField.contains("[") && readerField.contains("]");
	}

	/**
     * @return the formatField
     */
    public boolean isFormatField() {
    	return formatField;
    }

	@Override
	public MappingType getType() {
		return MappingType.Constructed;
	}

	/**
	 * @return the isMultipleFieldConcat
	 */
	public boolean isMultipleFieldConcat() {
		return isMultipleFieldConcat;
	}

}
