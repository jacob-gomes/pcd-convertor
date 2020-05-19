package com.pdfcore.main.model;

import com.pdfcore.main.model.enums.MappingType;

public class PreSufMapping extends Mapping 
{
	private final String prefix;
	private final String suffix;
	
	public PreSufMapping(String readerField, String writerField, String prefix, String suffix) {
		super(readerField, writerField);
		this.prefix = prefix;
		this.suffix = suffix;
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	public String getSuffix() {
		return suffix;
	}

	@Override
	public MappingType getType() {
		return MappingType.PreSufMapping;
	}

    @Override
    public String toString() {
        return "PreSufMapping{" +
                "prefix='" + prefix + '\'' +
                ", suffix='" + suffix + '\'' +
                ", parent= " + super.toString() + "}";
    }
}
