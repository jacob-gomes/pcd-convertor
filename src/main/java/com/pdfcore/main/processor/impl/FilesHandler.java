package com.pdfcore.main.processor.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.pdfcore.main.exceptions.ConfigInitException;

/**
 * @author cperebiceanu
 * 
 */
public class FilesHandler {
	private static final int BUFFER = 2048;
	private static String rootPath;

    /*
    class wide crw: use internal class logger
    crw: either fill the javadoc or delete it
     */
    /**
    	 * @param fileName
    	 * @return
    	 * @throws ConfigInitException
    	 */
    	public static Properties getProperties(String fileName) throws ConfigInitException {
    		Properties properties = new Properties();

    		String propFilePath = getFilePath() + File.separator + fileName;

    		try {
    			LogHandler.logInfo("Trying to load properties file: " + propFilePath);
    			properties.load(new FileInputStream(propFilePath));
    		} catch (FileNotFoundException e) {
                //crw: throw FileNotFoundException, no need for decoupling
    			throw new ConfigInitException(
    			        "The properties couldn't pe opened. Please verify that it exists on the correct path", e);
                //crw: throw IOException
    		} catch (IOException e) {
    			throw new ConfigInitException("IOException while trying to get properties file", e);
    		}
    		return properties;

    	}


	static String getFilePath() throws ConfigInitException {
		if (rootPath == null) {
			rootPath = System.getProperty("user.dir");
			LogHandler.logInfo("Determined root path for application: " + rootPath);
		}
		return rootPath;

	}

    //crw: this method is never used, do we need it?
	public static InputStream getFileAsInputStream(String fileName) throws ConfigInitException {
		String file = getFilePath() + File.separator + fileName;
		LogHandler.logInfo("Trying to get file:" + file);
		try {
			return new FileInputStream(file);
           //crw: throw FileNotFoundException
		} catch (FileNotFoundException e) {
			throw new ConfigInitException("The file: " + file + "was not found! ");
		}
	}

    //crw: this method is never used, do we need it?
	public static File zipFiles(ArrayList<File> fileList, File zipFile) throws ConfigInitException {
		try {
			BufferedInputStream origin = null;
			FileOutputStream dest = new FileOutputStream(zipFile);
			ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

			byte data[] = new byte[BUFFER];

			for (int i = 0; i < fileList.size(); i++) {
				FileInputStream fi = new FileInputStream(fileList.get(i));
				origin = new BufferedInputStream(fi, BUFFER);
				ZipEntry entry = new ZipEntry(fileList.get(i).getName());
				out.putNextEntry(entry);
				int count;

				while ((count = origin.read(data, 0, BUFFER)) != -1) {
					out.write(data, 0, count);
				}

				origin.close();
			}
			out.close();

			return zipFile;
		} catch (IOException e) {
			throw new ConfigInitException(e.getMessage(), e);
		}
	}
}
