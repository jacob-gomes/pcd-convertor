package com.pdfcore.main.model;

import com.pdfcore.main.model.enums.MappingType;

public class ImageMapping  extends Mapping{

	public ImageMapping(String readerField, String writerField) {
		super(readerField, writerField);

	}

	@Override
	public MappingType getType() {
		return MappingType.Image;
	}
	
}
