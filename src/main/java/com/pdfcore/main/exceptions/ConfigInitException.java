/**
 * 
 */
package com.pdfcore.main.exceptions;

/**
 * @author OgrisorJ
 *
 */
public class ConfigInitException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6356582162041892064L;

	/**
	 * 
	 */
	public ConfigInitException() {
	}

	/**
	 * @param arg0
	 */
	public ConfigInitException(String arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 */
	public ConfigInitException(Throwable arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public ConfigInitException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
