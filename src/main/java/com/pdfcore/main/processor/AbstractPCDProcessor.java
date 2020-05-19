package com.pdfcore.main.processor;

import com.pdfcore.main.model.ProcessingInstructionModel;

public abstract class AbstractPCDProcessor implements PCDConvertorProcessor{
	private ProcessingInstructionModel processingInstructionModel;

	@Override
	public ProcessingInstructionModel getProcessingInstructionModel() {
		return processingInstructionModel;
	}

	@Override
	public void setProcessingInstructionModel(ProcessingInstructionModel processingInstructionModel) {
		this.processingInstructionModel = processingInstructionModel;
	}
	
}
