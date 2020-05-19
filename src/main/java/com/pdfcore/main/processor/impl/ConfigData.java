package com.pdfcore.main.processor.impl;

import java.io.File;
import java.util.Properties;

import com.pdfcore.main.constants.Constants;
import com.pdfcore.main.exceptions.ConfigInitException;
import com.pdfcore.main.exceptions.ConfigInitRuntimeException;

/**
 * @author OgrisorJ
 *
 */
public class ConfigData {
	
	private static ConfigData singleton = null;
		
	private String  pdfTemplateFolder = "pdf";
	
	private String  pdfFolder = "pdf";
	
	//mappings location
	private String mappingsFolder = "mappings";
		 
	//report folder to put generated reports
	private String outputFolder = "output";
	
	//csv folder
	private String csvFolder = "csv";
	
	//data csv file name
	private String csvDataDump = "DATA_FEED";
	
	//pdf extension
	private String pdfExtension = ".pdf";
	
	//csv extension
	private String csvExtension = ".csv";
	
	//zip extension
	private String zipExtension = ".zip";
	
	//zip file output name
	private String zipFileOutputname = "FinalOutput_ZIP_";
	
	//generated csv file name
	private String generatedCsvFileName = "Generated_CSV_";
	
	//image folder
	private String imageFolder = "imgs";
	
	//input folder
	private String inputFolder = "input";
	
	//db connection settings
	private String dburl = null;
	private String dbusername = null;
	private String dbpassword = null;
	

	
	public static void initConfigurationFromFile(String configurationFileName) 
	{
		singleton = new ConfigData(configurationFileName);
	}
		
	/**
	 * @param confFileName
	 * Constructor that reads config values from the  properties file
	 * specified in the parameter. If the parameter is null than C
	 * @throws ConfigInitException 
	 */
	private ConfigData(String confFileName)
	{
		constructConfigFromFile(confFileName == null ? Constants.CONF_FILE : confFileName);
	}
	
	private void constructConfigFromFile(String confFile)
	{
		
		try 
		{
			//get the properties file
			Properties confProperties = FilesHandler.getProperties(Constants.CONF_FOLDER+File.separator+confFile);
			//read the properties
			if (confProperties.containsKey("conf.pdftemplatefolder"))
				this.pdfTemplateFolder = confProperties.getProperty("conf.pdftemplatefolder");
			if (confProperties.containsKey("conf.pdffolder"))
				this.pdfFolder = confProperties.getProperty("conf.pdffolder");
			if (confProperties.containsKey("conf.mappingsfolder"))
				this.mappingsFolder = confProperties.getProperty("conf.mappingsfolder");
			if (confProperties.containsKey("conf.outputfolder"))
				this.outputFolder = confProperties.getProperty("conf.outputfolder");
			if (confProperties.containsKey("conf.csvfolder"))
				this.csvFolder = confProperties.getProperty("conf.csvfolder");
			if (confProperties.containsKey("conf.csvdatadump"))
				this.csvDataDump = confProperties.getProperty("conf.csvdatadump");
			if (confProperties.containsKey("conf.pdfextension"))
				this.pdfExtension = confProperties.getProperty("conf.pdfextension");
			if (confProperties.containsKey("conf.csvextension"))
				this.csvExtension = confProperties.getProperty("conf.csvextension");
			if (confProperties.containsKey("conf.zipextension"))
				this.zipExtension = confProperties.getProperty("conf.zipextension");
			if (confProperties.containsKey("conf.zipfileoutputname"))
				this.zipFileOutputname = confProperties.getProperty("conf.zipfileoutputname");
			if (confProperties.containsKey("conf.generatedcsvfilename"))
				this.generatedCsvFileName= confProperties.getProperty("conf.generatedcsvfilename");
			if (confProperties.containsKey("db.url"))
				this.dburl = confProperties.getProperty("db.url");
			if (confProperties.containsKey("db.username"))
				this.dbusername = confProperties.getProperty("db.username");
			if (confProperties.containsKey("conf.imagesfolder"))
				this.imageFolder = confProperties.getProperty("conf.imagesfolder");
			if (confProperties.containsKey("conf.inputFolder"))
				this.inputFolder = confProperties.getProperty("conf.inputFolder");
			
		} 
		catch (ConfigInitException e)
		{
			throw new ConfigInitRuntimeException("Could not initialize the application config. Severe error", e);
		}
		

		
		//construct the forms to be generated
		
	}

	public  static String getPdfTemplateFolder() {
		if (singleton == null)
		{
			singleton = new ConfigData(null);
		}
		return singleton.pdfTemplateFolder;
	}

	/**
	 * @return the pdfFolder
	 */
	public static String getPdfFolder() {
		if (singleton == null)
		{
			singleton = new ConfigData(null);
		}
		return singleton.pdfFolder;
	}

	/**
	 * @param pdfFolder the pdfFolder to set
	 */
	public void setPdfFolder(String pdfFolder) {
		this.pdfFolder = pdfFolder;
	}

	public static String getMappingsFolder() {
		if (singleton == null)
		{
			singleton = new ConfigData(null);
		}
		return singleton.mappingsFolder;
	}

	public static String getOutputFolder() {
		if (singleton == null)
		{
			singleton = new ConfigData(null);
		}
		return singleton.outputFolder;
	}

	public static String getCsvFolder() {
		if (singleton == null)
		{
			singleton = new ConfigData(null);
		}
		return singleton.csvFolder;
	}
	public static String getCsvDataDump() {
		if (singleton == null)
		{
			singleton = new ConfigData(null);
		}
		return singleton.csvDataDump;
	}

	public static String getPdfExtension() {
		if (singleton == null)
		{
			singleton = new ConfigData(null);
		}
		return singleton.pdfExtension;
	}
	public static String getCsvExtension() {
		if (singleton == null)
		{
			singleton = new ConfigData(null);
		}
		return singleton.csvExtension;
	}
	public static String getZipExtension()
	{
		if (singleton == null)
		{
			singleton = new ConfigData(null);
		}
		return singleton.zipExtension;
	}

	public static String getZipFileOutputname() {
		if (singleton == null)
		{
			singleton = new ConfigData(null);
		}
		return singleton.zipFileOutputname;
	}

	public static String getGeneratedCsvFileName() {
		if (singleton == null)
		{
			singleton = new ConfigData(null);
		}
		return singleton.generatedCsvFileName;
	}

	public static String getDburl() {
		if (singleton == null)
		{
			singleton = new ConfigData(null);
		}
		return singleton.dburl;
	}

	
	public static String getDbusername() {
		if (singleton == null)
		{
			singleton = new ConfigData(null);
		}
		return singleton.dbusername;
	}


	public static String getDbpassword() {
		if (singleton == null)
		{
			singleton = new ConfigData(null);
		}
		return singleton.dbpassword;
	}

	
	public static String getImageFolder() {
		if (singleton == null)
		{
			singleton = new ConfigData(null);
		}
		return singleton.imageFolder;
	}

	/**
     * @return the inputFolder
     */
    public static String getInputFolder() {
    	
		if (singleton == null)
		{
			singleton = new ConfigData(null);
		}
		return singleton.inputFolder;
    }

	


}
