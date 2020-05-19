package com.pdfcore.main.constants;

public interface Constants {
	String EXTRACT = "extract";

	//configuration entries go here
	String CONF_FOLDER = "conf";

	//configuration properties
	String CONF_FILE = "pdfconf.properties";
	
	//system temp directory
	String TEMP_DIR = System.getProperty("java.io.tmpdir");

	//select
	String SQL_SELECT = "SELECT * FROM @tablename WHERE trace_no=?";
	
	String PDFBOX= "pdfBox";
		

	String SQL_UPDATE = "UPDATE @tablename SET WorkCompleteFlag=?, PDF_Form=?, Error=? WHERE";
	 
	 
	
}
