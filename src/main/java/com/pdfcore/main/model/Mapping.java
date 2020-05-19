package com.pdfcore.main.model;

import com.pdfcore.main.model.enums.MappingType;

/**
 * Used to store mapping between data source fields and data destination fields
 * If they have the same name use constructor Mapping(String name)
 * key = value in mapping file
 * Represents the mapping between a source field name and a destination field name.
 * @author OgrisorJ
 *
 */
public abstract class Mapping {
	
	/**
	 * Represents the source field name
	 */
	private final String readerField;
	
	/**
	 * Represent the destination field name
	 */
	private final String writerField;
	
	public Mapping(String readerField, String writerField) {
		super();
		this.readerField = readerField;
		this.writerField = writerField;
	}

	public String getReaderField() {
		return readerField;
	}

	public String getWriterField() {
		return writerField;
	}

	
	@Override
	public boolean equals(Object obj) {

		if (obj instanceof Mapping)
		{
			Mapping mapp = (Mapping) obj;
			if (writerField == null || readerField == null)
				return false;
			return writerField.equals(mapp.getWriterField()) && readerField.equals(mapp.getReaderField()) && getType().equals(mapp.getType());
		}
		return false;
	}
	
	public abstract MappingType getType();

    @Override
    public String toString() {
        return "Mapping{" +
                "readerField='" + readerField + '\'' +
                ", writerField='" + writerField + '\'' +
                '}';
    }
}
