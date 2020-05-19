package com.pdfcore.main.processor.impl;

import java.io.File;
import java.util.Properties;

import com.pdfcore.main.constants.Constants;
import com.pdfcore.main.exceptions.ConfigInitException;
import com.pdfcore.main.exceptions.ConfigInitRuntimeException;
import com.pdfcore.main.service.util.PropertyUtils;

/**
 * @author: Rick Chen
 * Date: 1/18/13
 * Time: 12:05 PM
 */
public class PcdJobConf {

	// This should be changed to packagePath as it is not the confPath
    private String confPath = ""; 

    private String jobFileName;

    private String mergedPdfFileName;

    private int index = 0;




    private String pdfTemplateFolder = "pdf";
    
    /**
	 * Load template file into memory can get better performance, but need more
	 * memory.
	 */
	private boolean loadTemplateFileIntoMemory = true;

    private String pdfFolder = "pdf";

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


    public PcdJobConf(String jobFileName) {
        this.jobFileName = jobFileName;
        constructConfigFromFile();

    }
    
    public PcdJobConf(String packagePath, String jobFileName) {
        this.confPath = packagePath;
        this.jobFileName = jobFileName;
        constructConfigFromFile();

    }

    public PcdJobConf(String packagePath, String jobFileName, String mergedPdfFileName) {
        this.confPath = packagePath;
        this.jobFileName = jobFileName;
        this.mergedPdfFileName = mergedPdfFileName;
        constructConfigFromFile();
    }

    public String getMergedPdfFileName() {
        return mergedPdfFileName;
    }

    private void constructConfigFromFile() {
    	
    	String confFile;
    	
    	if (confPath.equalsIgnoreCase("")) {
    		confFile = Constants.CONF_FOLDER + File.separator + Constants.CONF_FILE;
    	} else {
    		confFile = confPath + File.separator +
                    Constants.CONF_FOLDER + File.separator + Constants.CONF_FILE;
    	}

        try {
            //get the properties file
            Properties confProperties = PropertyUtils.getProperties(confFile);
            
            //read the properties
            if (confProperties.containsKey("conf.pdftemplatefolder")) {
                this.pdfTemplateFolder = confProperties.getProperty("conf.pdftemplatefolder");
            }
            if (confProperties.containsKey("conf.loadTemplateFileIntoMemory")) {
				this.loadTemplateFileIntoMemory = Boolean
						.parseBoolean(confProperties
								.getProperty("conf.loadTemplateFileIntoMemory"));
			}
            if (confProperties.containsKey("conf.pdffolder")) {
                this.pdfFolder = confProperties.getProperty("conf.pdffolder");
            }
            if (confProperties.containsKey("conf.mappingsfolder")) {
                this.mappingsFolder = confProperties.getProperty("conf.mappingsfolder");
            }
            if (confProperties.containsKey("conf.outputfolder")) {
                this.outputFolder = confProperties.getProperty("conf.outputfolder");
            }
            if (confProperties.containsKey("conf.csvfolder")) {
                this.csvFolder = confProperties.getProperty("conf.csvfolder");
            }
            if (confProperties.containsKey("conf.csvdatadump")) {
                this.csvDataDump = confProperties.getProperty("conf.csvdatadump");
            }
            if (confProperties.containsKey("conf.pdfextension")) {
                this.pdfExtension = confProperties.getProperty("conf.pdfextension");
            }
            if (confProperties.containsKey("conf.csvextension")) {
                this.csvExtension = confProperties.getProperty("conf.csvextension");
            }
            if (confProperties.containsKey("conf.zipextension")) {
                this.zipExtension = confProperties.getProperty("conf.zipextension");
            }
            if (confProperties.containsKey("conf.zipfileoutputname")) {
                this.zipFileOutputname = confProperties.getProperty("conf.zipfileoutputname");
            }
            if (confProperties.containsKey("conf.generatedcsvfilename")) {
                this.generatedCsvFileName = confProperties.getProperty("conf.generatedcsvfilename");
            }
            if (confProperties.containsKey("db.url")) {
                this.dburl = confProperties.getProperty("db.url");
            }
            if (confProperties.containsKey("db.username")) {
                this.dbusername = confProperties.getProperty("db.username");
            }
            if (confProperties.containsKey("conf.imagesfolder")) {
                this.imageFolder = confProperties.getProperty("conf.imagesfolder");
            }
            if (confProperties.containsKey("conf.inputFolder")) {
                this.inputFolder = confProperties.getProperty("conf.inputFolder");
            }

        }
        catch (ConfigInitException e) {
            throw new ConfigInitRuntimeException("Could not initialize the application config. Severe error", e);
        }


        //construct the forms to be generated

    }



    public String getConfPath() {
        return confPath;
    }


    public String getJobFileName() {
        return jobFileName;
    }



    public String getPdfTemplateFolder() {
        return pdfTemplateFolder;
    }

    public String getPdfFolder() {
        return pdfFolder;
    }

    public String getMappingsFolder() {
        return mappingsFolder;
    }

    public String getOutputFolder() {
        return outputFolder;
    }

    public String getCsvFolder() {
        return csvFolder;
    }

    public String getCsvDataDump() {
        return csvDataDump;
    }

    public String getPdfExtension() {
        return pdfExtension;
    }

    public String getCsvExtension() {
        return csvExtension;
    }

    public String getZipExtension() {
        return zipExtension;
    }

    public String getZipFileOutputname() {
        return zipFileOutputname;
    }

    public String getGeneratedCsvFileName() {
        return generatedCsvFileName;
    }

    public String getImageFolder() {
        return imageFolder;
    }

    public String getInputFolder() {
        return inputFolder;
    }

    public String getDburl() {
        return dburl;
    }

    public String getDbusername() {
        return dbusername;
    }

    public String getDbpassword() {
        return dbpassword;
    }


    public void setMergedPdfFileName(String mergedPdfFileName) {
        this.mergedPdfFileName = mergedPdfFileName;
    }

    public int nextIndex() {
        return index ++;
    }
    
	public boolean isLoadTemplateFileIntoMemory() {
		return loadTemplateFileIntoMemory;
	}

	public void setLoadTemplateFileIntoMemory(boolean loadTemplateFileIntoMemory) {
		this.loadTemplateFileIntoMemory = loadTemplateFileIntoMemory;
	}
}
