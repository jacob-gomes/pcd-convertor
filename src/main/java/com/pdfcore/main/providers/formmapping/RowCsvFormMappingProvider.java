/**
 * 
 */
package com.pdfcore.main.providers.formmapping;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import au.com.bytecode.opencsv.CSVReader;

import com.pdfcore.main.exceptions.FormMappingException;
import com.pdfcore.main.model.FormMapping;
import com.pdfcore.main.model.Mapping;
import com.pdfcore.main.processor.impl.LogHandler;

/**
 * @author tqn
 * 
 */
public class RowCsvFormMappingProvider extends AbstractCsvFormMappingProvider {

	/**
	 * @param form
	 * @param prop 
	 * @param rows
	 * @throws FormMappingException
	 */
	protected void processRows(FormMapping form, CSVReader prop)
			throws FormMappingException {
		List<String[]> rows;
		try {
			rows = prop.readAll();
		} catch (IOException e) {
			LogHandler.logError(e.getMessage());
			throw new FormMappingException("Properties file is null.");
		}
		
		String[] row1 = rows.get(0);
		String[] row2 = rows.get(1);

		if (row1 == null || row1.length == 0 || row2 == null
				|| row2.length == 0) {
			throw new FormMappingException(
					"No mappings where found in properties file");
		}

		String key;
		String value;
		Mapping mapping;
		for (int col = 0; col < row1.length; col++) {
			key = row1[col];
			value = row2[col];
			if (StringUtils.isBlank(key)) {
				LogHandler
						.logError("Null or 0 length key was found in mapping file.Skipping");
				continue;
			}

			LogHandler.logDebug("Found new mapping with key: " + key
					+ ". Determing mapping type");

			if (StringUtils.isBlank(value)) {
				LogHandler
						.logError("Null or 0 length value was found for mapping key: "
								+ key + ". Skipping");
				continue;
			}

			mapping = buildMapping(key, value);
			if (mapping != null) {
				form.addMappings(mapping);
			}
		}
	}

}
