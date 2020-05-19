package com.pdfcore.main.processor.enums;

public enum CompleteFlagEnum {
	
	PENDINGWEB (1,"Work started by web application but not complete yet."),
	ERRORSWEB (2,"Work started by web application and errors."),
	COMPLETEDWEB (3 , "Work started by web application and is now complete. (That means the pdfGenerator can start working)."),
	STARTED (4, "Work started by pdfGenerator and is not complete yet."),
	COMPLETED (5,"Work started by pdfGenerator and is complete and successfull"),
	ERRORS (6 ,"Work started by pdfGenerator and errors.");
	
	private final int id;
	private final String text;
	
	private CompleteFlagEnum(int id,String text) {
		this.id = id;
		this.text = text;
	}
	
	public int getId()
	{
		return id;
	}
	public String getText()
	{
		return text;
	}
}
