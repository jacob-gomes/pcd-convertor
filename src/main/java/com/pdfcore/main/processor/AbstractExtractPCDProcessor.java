package com.pdfcore.main.processor;

import java.io.File;

public abstract class AbstractExtractPCDProcessor extends AbstractPCDProcessor {

	@Override
	public void executeProcess() throws Exception {
		if (getProcessingInstructionModel().getPdfForAccroExtraction() == null 
				|| getProcessingInstructionModel().getPdfForAccroExtraction().isEmpty()) {
			throw new Exception("PdfForAccroExtraction cannot be null");
		}

		File inputFile = new File("input/pdf".concat(File.separator)
				.concat(getProcessingInstructionModel().getPdfForAccroExtraction()).concat(".pdf"));
		File outputFile = new File("output/csv/accro_extracted_"
				.concat(getProcessingInstructionModel().getPdfForAccroExtraction()).concat(".csv"));
		
		
		executeExtractProcess(inputFile, outputFile);
	}

	protected abstract void executeExtractProcess(File inputFile, File outputFile) throws Exception;
}
