package com.pdfcore.main.model;

import com.pdfcore.main.model.enums.MappingType;

public class NormalMapping extends Mapping {

	public NormalMapping(String readerField, String writerField) {
		super(readerField, writerField);
	}

	@Override
	public MappingType getType() {
		return MappingType.Normal;
	}

    @Override
    public String toString() {
        return "Normal" + super.toString();
    }
}
