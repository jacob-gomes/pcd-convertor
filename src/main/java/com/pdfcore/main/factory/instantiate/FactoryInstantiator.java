package com.pdfcore.main.factory.instantiate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pdfcore.main.constants.Constants;
import com.pdfcore.main.factory.PCDConvertorFactory;
import com.pdfcore.main.factory.impl.ExtractFactoryImpl;
import com.pdfcore.main.factory.impl.MergeFactoryImpl;

@Component
public class FactoryInstantiator {
	
	private ExtractFactoryImpl extractFactoryImpl;
	
	
	private MergeFactoryImpl mergeFactoryImpl;
	
	
	/**
	 * @param extractFactoryImpl
	 * @param nonExtractFactoryImpl
	 */
	@Autowired
	public FactoryInstantiator(ExtractFactoryImpl extractFactoryImpl, MergeFactoryImpl mergeFactoryImpl) {
		super();
		this.extractFactoryImpl = extractFactoryImpl;
		this.mergeFactoryImpl = mergeFactoryImpl;
	}


	public PCDConvertorFactory getFactoryInstance(String jobName) {
		if(Constants.EXTRACT.equals(jobName)) {
			return extractFactoryImpl;
		}
		return mergeFactoryImpl;
	}
}
