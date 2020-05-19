package com.pdfcore.main.processor;

import com.pdfcore.main.model.ProcessingInstructionModel;

public interface PCDConvertorProcessor {

	void setProcessingInstructionModel(ProcessingInstructionModel processingInstructionModel);
	
	ProcessingInstructionModel getProcessingInstructionModel();
	
	void executeProcess() throws Exception;
}
