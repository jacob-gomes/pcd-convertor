package com.pdfcore.main.model;

import com.pdfcore.main.model.enums.MappingType;

public class ColIndexMapping extends Mapping
{

	public ColIndexMapping(String readerField, String writerField) {
		super(readerField, writerField);
	}

	@Override
	public MappingType getType() {
		return MappingType.ColumnIndex;
	}

}
