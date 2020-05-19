package com.pdfcore.main.service.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import com.pdfcore.main.exceptions.ConfigInitException;
import com.pdfcore.main.processor.impl.LogHandler;
import com.pdfcore.main.processor.impl.PcdJobConf;

/**
 * @author: Rick Chen
 * Date: 1/20/13
 * Time: 10:51 AM
 */
public class PropertyUtils {

             //crw: incomplete javadoc, delete it or complete it correctly
    /**
    	 * @param
    	 * @return
    	 * @throws com.pdfcore.main.exceptions.ConfigInitException
    	 */
    	public static Properties getProperties(String propFilePath) throws ConfigInitException {
    		Properties properties = new Properties();


    		try {
                //crw: use internal class logger
    			LogHandler.logInfo("Trying to load properties file: " + propFilePath);
    			properties.load(new FileInputStream(propFilePath));
                //crw: throw FileNotFoundException
    		} catch (FileNotFoundException e) {
    			throw new ConfigInitException(
    			        "The properties couldn't pe opened. Please verify that it exists on the correct path", e);
                //crw: throw IOException
    		} catch (IOException e) {
    			throw new ConfigInitException("IOException while trying to get properties file", e);
    		}
    		return properties;

    	}


    public static File getFile(PcdJobConf pcdJobConf, String fileName) throws ConfigInitException {
        //crw: use StringBuilder
        String filePath = pcdJobConf.getConfPath() + File.separator + fileName;
        //crw: log with logger, although this is not a log what you print here
        System.out.println("\n" + filePath + "\n");
        return new File(filePath);
    }

}
