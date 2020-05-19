package com.pdfcore.main.service.util;

import com.pdfcore.main.constants.Constants;
import com.pdfcore.main.model.ProcessingInstructionModel;
import com.pdfcore.main.processor.impl.PcdJobConf;

public interface PCDConvertorUtil {

	static ProcessingInstructionModel populateProcessingInstructionModel(String[] arguments, String pdfCoreVendor ) {
		ProcessingInstructionModel processingInstructionModel = new ProcessingInstructionModel();
		
		if (arguments.length == 1) {

			processingInstructionModel.setJobName(arguments[0]);

		} else if (arguments.length == 2) {

			if (Constants.EXTRACT.equalsIgnoreCase(arguments[0])) {

				processingInstructionModel.setJobName(arguments[0]);
				processingInstructionModel.setPdfForAccroExtraction(arguments[1]);

			} else {

				processingInstructionModel.setPackagePath(arguments[0]);
				processingInstructionModel.setJobName(arguments[1]);

			}
		} else if (arguments.length == 3) {

			processingInstructionModel.setPackagePath(arguments[0]);
			processingInstructionModel.setJobName(arguments[1]);
			processingInstructionModel.setMergedOutputFileName(arguments[2]);
		}
		
		processingInstructionModel.setPdfCoreVendor(pdfCoreVendor);
		
		return processingInstructionModel;
	}

	static PcdJobConf generatePcdJobConf(ProcessingInstructionModel instructionModel) {
		PcdJobConf pcdJobConf;	

		if (instructionModel.getPackagePath() == null) {

			pcdJobConf = new PcdJobConf(System.getProperty("user.dir"), instructionModel.getJobName());

		} else {

			pcdJobConf = new PcdJobConf(instructionModel.getPackagePath(),
					instructionModel.getJobName(),
					instructionModel.getMergedOutputFileName());
		}
		
		return pcdJobConf;
	}
}
