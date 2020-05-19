package com.pdfcore.main.interfaces;

import java.io.File;

import com.pdfcore.main.exceptions.DataReaderException;
import com.pdfcore.main.model.ColIndexMapping;
import com.pdfcore.main.model.ConstructedMapping;
import com.pdfcore.main.model.ImageMapping;
import com.pdfcore.main.model.NormalMapping;
import com.pdfcore.main.model.PreSufMapping;

/**
 * @author OgrisorJ Interface used to handle read operations
 */
public interface DataProvider {

	/**
	 * Moves to the next record
	 * 
	 * @return true if there is another element , false otherwise
	 */
	public boolean hasNext() throws DataReaderException;

	public String getValueForMapping(NormalMapping mapping)
			throws DataReaderException;

	public String getValueForMapping(PreSufMapping mapping)
			throws DataReaderException;

	public String getValueForMapping(ConstructedMapping mapping)
			throws DataReaderException;

	public String getValueForMapping(ColIndexMapping mapping)
			throws DataReaderException;

	public File getValueForMapping(ImageMapping mapping)
			throws DataReaderException;

	/**
	 * @return the value for the primarykey
	 */
	public String getValueForPk() throws DataReaderException;

}
