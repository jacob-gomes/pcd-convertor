package com.pdfcore.main.factory.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pdfcore.main.constants.Constants;
import com.pdfcore.main.factory.PCDConvertorFactory;
import com.pdfcore.main.model.ProcessingInstructionModel;
import com.pdfcore.main.processor.PCDConvertorProcessor;
import com.pdfcore.main.processor.impl.AccroFieldExtractor;
import com.pdfcore.main.processor.impl.AccroFieldExtractorPDFBoxImpl;

@Component
public class ExtractFactoryImpl implements PCDConvertorFactory {

	private AccroFieldExtractor accroFieldExtractor;
	
	private AccroFieldExtractorPDFBoxImpl accroFieldExtractorPDFBoxImpl;
	/**
	 * @param accroFieldExtractor
	 */
	@Autowired
	public ExtractFactoryImpl(AccroFieldExtractor accroFieldExtractor,
			AccroFieldExtractorPDFBoxImpl accroFieldExtractorPDFBoxImpl) {
		super();
		this.accroFieldExtractor = accroFieldExtractor;
		this.accroFieldExtractorPDFBoxImpl = accroFieldExtractorPDFBoxImpl;
	}


	@Override
	public PCDConvertorProcessor getInstance(ProcessingInstructionModel processingInstructionModel) {
		if(Constants.PDFBOX.equals(processingInstructionModel.getPdfCoreVendor())) {
			return accroFieldExtractorPDFBoxImpl;
		}
		return accroFieldExtractor;
	}

}
