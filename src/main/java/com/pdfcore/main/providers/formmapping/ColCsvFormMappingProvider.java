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
public class ColCsvFormMappingProvider extends AbstractCsvFormMappingProvider {

	/**
	 * @param form
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

		if (rows == null || rows.isEmpty()) {
			throw new FormMappingException("Properties file is null.");
		}

		String key;
		String value;
		Mapping mapping;
		for (String[] row : rows) {
			if (row.length != 2) {
				LogHandler
						.logError("Null or 0 length key/value was found in mapping file.Skipping");
				continue;
			}
			key = row[0];
			value = row[1];
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
