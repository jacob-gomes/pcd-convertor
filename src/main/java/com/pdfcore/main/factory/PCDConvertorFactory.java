package com.pdfcore.main.factory;

import com.pdfcore.main.model.ProcessingInstructionModel;
import com.pdfcore.main.processor.PCDConvertorProcessor;

public interface PCDConvertorFactory {

	PCDConvertorProcessor getInstance(ProcessingInstructionModel processingInstructionModel);

}
