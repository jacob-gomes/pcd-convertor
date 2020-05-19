package com.pdfcore.main.service.util;

import java.io.File;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;

import com.pdfcore.main.constants.Constants;
import com.pdfcore.main.exceptions.ConfigInitException;
import com.pdfcore.main.processor.impl.LogHandler;
import com.pdfcore.main.processor.impl.PcdJobConf;

/**
 * Singleton pattern used to get configuration related stuff
 * @author calin.perebiceanu
 *
 */
public class ConfigUtil {

	static String logConfFile = Constants.CONF_FOLDER + File.separator + "log4j.properties";
	
	public static void initLogger(String packagePath) throws ConfigInitException
	{
		if (packagePath != null) {
			logConfFile = packagePath + File.separator + logConfFile;
		}
		
		BasicConfigurator.configure();        

        PropertyConfigurator.configure(PropertyUtils.getProperties(logConfFile));
    }
	
	public static void initLogger(PcdJobConf conf) throws ConfigInitException
	{
		BasicConfigurator.configure();

        //crw: use internal class logger to log the errors.
		LogHandler.logInfo("Trying to init the Configuration");
		//PropertyConfigurator.configure(FilesHandler.getProperties(Constants.CONF_FOLDER+File.separator+"log4j.properties"));
        logConfFile = conf.getConfPath() + File.separator + logConfFile;
        //System.out.println(logConfFile);
        PropertyConfigurator.configure(PropertyUtils.getProperties(logConfFile));
    }
	
}
