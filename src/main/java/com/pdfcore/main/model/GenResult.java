package com.pdfcore.main.model;

import com.pdfcore.main.processor.enums.CompleteFlagEnum;

public class GenResult {

    private String id ;
	private String generateForm = null;
	private CompleteFlagEnum completFlag ;
	private Exception error;

    private String outputFile;
    private String outputAbsoluteFolder;

    public GenResult() {
		super();
	}
	public GenResult(String id, String generateForm,
			CompleteFlagEnum completFlag, Exception error) {
		super();
		this.id = id;
		this.generateForm = generateForm;
		this.completFlag = completFlag;
		this.error = error;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getGenerateForm() {
		return generateForm;
	}
	public void setGenerateForm(String generateForm) {
		this.generateForm = generateForm;
	}
	public CompleteFlagEnum getCompletFlag() {
		return completFlag;
	}
	public void setCompletFlag(CompleteFlagEnum completFlag) {
		this.completFlag = completFlag;
	}
	public Exception getError() {
		return error;
	}
	public void setError(Exception error) {
		this.error = error;
	}

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public void setOutputAbsoluteFolder(String outputAbsoluteFolder) {
        this.outputAbsoluteFolder = outputAbsoluteFolder;
    }

    public String getOutputAbsoluteFolder() {
        return outputAbsoluteFolder;
    }
}
