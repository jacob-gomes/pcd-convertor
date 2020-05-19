package com.pdfcore.main.processor.impl;

import org.apache.log4j.Logger;

//crw: this class can be deleted because we will use internal class loggers
public class LogHandler {
	private static Logger log = Logger.getLogger(LogHandler.class);
	
	public static void logError(Throwable e)
	{
		log.error("ERROR OCCURED:",e);
	}
	public static void logError(String e)
	{
		log.error("ERROR OCCURED: "+e);
	}
	public static void logFatal(Throwable e)
	{
		log.fatal("FATAL ERROR OCCURED",e);
	
	}
	public static void logInfo(String message)
	{
		log.info("INFO: "+message);
	}
	public static void logDebug(String message)
	{
		log.debug("DEBUG: "+message);
	}
}
