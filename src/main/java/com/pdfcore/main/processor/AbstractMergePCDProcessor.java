package com.pdfcore.main.processor;

public abstract class AbstractMergePCDProcessor extends AbstractPCDProcessor {

	@Override
	public void executeProcess() throws Exception {
		executeMergeProcess();

	}
	
	protected abstract void executeMergeProcess() throws Exception;
}
