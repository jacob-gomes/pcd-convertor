package com.pdfcore.main.interfaces;

import java.util.ArrayList;

import com.pdfcore.main.exceptions.ConfigInitException;
import com.pdfcore.main.exceptions.DataReaderException;
import com.pdfcore.main.exceptions.DataWriterException;
import com.pdfcore.main.model.FormMapping;
import com.pdfcore.main.model.GenResult;

/**
 * @author OgrisorJ
 *
 */
public abstract class WriterProvider {

	private final String pk;
	
	public WriterProvider(String pk) {
		this.pk = pk;
	}
	/**
	 * @param dataProvider
	 * @param formMapping
	 * @throws DataWriterException
	 * @throws DataReaderException
	 * @throws ConfigInitException
	 */
	public abstract ArrayList<GenResult> write(DataProvider dataProvider, FormMapping formMapping) throws DataWriterException, DataReaderException, ConfigInitException;
	public String getPk() {
		return pk;
	}

}
