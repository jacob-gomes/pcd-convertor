package com.pdfcore.main.model;

public class ProcessingInstructionModel {
	String jobName;
	String packagePath;
	String mergedOutputFileName;
	String pdfForAccroExtraction;
	String pdfCoreVendor;
	
	/**
	 * @return the jobName
	 */
	public String getJobName() {
		return jobName;
	}
	/**
	 * @param jobName the jobName to set
	 */
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	/**
	 * @return the packagePath
	 */
	public String getPackagePath() {
		return packagePath;
	}
	/**
	 * @param packagePath the packagePath to set
	 */
	public void setPackagePath(String packagePath) {
		this.packagePath = packagePath;
	}
	/**
	 * @return the mergedOutputFileName
	 */
	public String getMergedOutputFileName() {
		return mergedOutputFileName;
	}
	/**
	 * @param mergedOutputFileName the mergedOutputFileName to set
	 */
	public void setMergedOutputFileName(String mergedOutputFileName) {
		this.mergedOutputFileName = mergedOutputFileName;
	}
	/**
	 * @return the pdfForAccroExtraction
	 */
	public String getPdfForAccroExtraction() {
		return pdfForAccroExtraction;
	}
	/**
	 * @param pdfForAccroExtraction the pdfForAccroExtraction to set
	 */
	public void setPdfForAccroExtraction(String pdfForAccroExtraction) {
		this.pdfForAccroExtraction = pdfForAccroExtraction;
	}
	/**
	 * @return the pdfCoreVendor
	 */
	public String getPdfCoreVendor() {
		return pdfCoreVendor;
	}
	/**
	 * @param pdfCoreVendor the pdfCoreVendor to set
	 */
	public void setPdfCoreVendor(String pdfCoreVendor) {
		this.pdfCoreVendor = pdfCoreVendor;
	}
	
	
}
