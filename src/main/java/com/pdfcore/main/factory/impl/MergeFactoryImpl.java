package com.pdfcore.main.factory.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pdfcore.main.constants.Constants;
import com.pdfcore.main.factory.PCDConvertorFactory;
import com.pdfcore.main.model.ProcessingInstructionModel;
import com.pdfcore.main.processor.PCDConvertorProcessor;
import com.pdfcore.main.processor.impl.JobBatch;

@Component
public class MergeFactoryImpl implements PCDConvertorFactory {

	private JobBatch jobBatch;
	
	/**
	 * @param jobBatch
	 */
	@Autowired
	public MergeFactoryImpl(JobBatch jobBatch) {
		super();
		this.jobBatch = jobBatch;
	}



	@Override
	public PCDConvertorProcessor getInstance(ProcessingInstructionModel processingInstructionModel) {
		if(Constants.PDFBOX.equals(processingInstructionModel.getPdfCoreVendor())) {
			return null;
		}
		return jobBatch;
	}

}
