package com.pdfcore.main.service.impl;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.pdfcore.main.factory.instantiate.FactoryInstantiator;
import com.pdfcore.main.model.ProcessingInstructionModel;
import com.pdfcore.main.processor.PCDConvertorProcessor;
import com.pdfcore.main.service.PCDConvertorService;
import com.pdfcore.main.service.util.PCDConvertorUtil;

@Component
public class PCDConvertorServiceImpl implements PCDConvertorService {

	private static final Logger logger = Logger
			.getLogger(PCDConvertorServiceImpl.class);

	private String pdfCoreVendor;

	private FactoryInstantiator factoryInstantiator; 

	/**
	 * @param pdfCoreVendor
	 * @param factoryInstantiator
	 */
	@Autowired
	public PCDConvertorServiceImpl(@Value("${app.pdfcore.vendor:itext}")String pdfCoreVendor,
			FactoryInstantiator factoryInstantiator) {
		super();
		this.pdfCoreVendor = pdfCoreVendor;
		this.factoryInstantiator = factoryInstantiator;
	}

	@Override
	public void executePCDConvertor(String[] arguments) {	

		ProcessingInstructionModel processingInstructionModel;
		PCDConvertorProcessor processor;
		
		long startTime = System.currentTimeMillis();
		
		try {
			
			if (arguments == null || arguments.length < 1) {
				throw new Exception("Please provide arguments.");
			}
			
			processingInstructionModel =  PCDConvertorUtil.populateProcessingInstructionModel(arguments, this.pdfCoreVendor);

			processor = factoryInstantiator.getFactoryInstance(processingInstructionModel.getJobName())
					.getInstance(processingInstructionModel);
			
			processor.setProcessingInstructionModel(processingInstructionModel);
			
			processor.executeProcess();
			
		} catch (Exception e) {
			logger.error("Failure during processing the request...", e);
		}		
		 
		logger.info(String.format("use %d ms.", System.currentTimeMillis() - startTime));
	}

}
