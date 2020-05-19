/**
 * 
 */
package com.pdfcore.main.providers.formmapping;

import com.pdfcore.main.interfaces.FormMappingProvider;
import com.pdfcore.main.model.ColIndexMapping;
import com.pdfcore.main.model.ConstructedMapping;
import com.pdfcore.main.model.ImageMapping;
import com.pdfcore.main.model.Mapping;
import com.pdfcore.main.model.NormalMapping;
import com.pdfcore.main.model.PreSufMapping;
import com.pdfcore.main.processor.impl.LogHandler;

/**
 * @author tqn
 *
 */
public abstract class AbstractFormMappingProvider implements FormMappingProvider {

	/**
	 * @param key
	 * @param value
	 */
	protected Mapping buildMapping(String key, String value) {
		// Determine what type of mapping it is

		if (value.contains("<<<<") && value.contains(">>>>")) { // It is an
																// image
																// mapping
			int beginIndex = value.indexOf("<<<<") + 4;
			int endIndex = value.indexOf(">>>>");
			// Get the image name
			String imageName = value.substring(beginIndex, endIndex);
			// Validate image name
			if (imageName == null || imageName.trim().length() == 0) {
				LogHandler
						.logError("Image mapping found but null or 0 length image name specified for mapping: "
								+ key + ". Skipping");
				return null;
			}
			// Create and add the mapping
			LogHandler.logDebug("Image mapping was added. Image name: "
					+ imageName);
			return new ImageMapping(imageName, key);

		} else if (value.contains("<<<") && value.contains(">>>")) {
			// It's an index mapping
			int beginIndex = value.indexOf("<<<") + 3;
			int endIndex = value.indexOf(">>>");
			String realField = value.substring(beginIndex, endIndex);
			// validate realField
			if (realField == null || realField.trim().length() == 0) {
				LogHandler
						.logError("Index mapping found but null or 0 length field name specified for mapping: "
								+ key + ". Skipping");
				return null;
			}
			// Create and add the mapping
			LogHandler.logDebug("Index mapping was added. Index field name: "
					+ realField);
			return new ColIndexMapping(realField, key);
		}
		if (value.contains("<<") && value.contains(">>")) {
			// It's a constructed mapping
			int firstPos = value.indexOf("<<") + 2;
			int lastPos = value.indexOf(">>");
			String realField = value.substring(firstPos, lastPos);
			// if the string is empty put null
			if (realField != null && realField.trim().length() == 0)
				realField = null;
			LogHandler.logDebug("Constructed mapping was added.");
			return new ConstructedMapping(realField, key);

		} else if (value.contains("<") && value.contains(">")) {
			int firstPos = value.indexOf("<");
			int lastPos = value.indexOf(">");
			String prefix = value.substring(0, firstPos);
			String sufix = value.substring(lastPos + 1, value.length());
			String dataField = value.substring(firstPos + 1, lastPos);
			if ((prefix == null || prefix.length() == 0)
					&& (sufix == null || sufix.length() == 0))
				LogHandler
						.logInfo("Prefix/Suffix mapping was found but no prefix or suffix was provided. Consider using a normal mapping");
			if (dataField == null || dataField.length() == 0) {
				LogHandler
						.logError("Prefix/Suffix mapping was found but null or 0 length field was provided for mapping: "
								+ key + ". Skipping");
				return null;
			}
			LogHandler.logDebug("Prefix/Suffix mapping was added.");
			return new PreSufMapping(dataField, key, prefix, sufix);
		} else {
			// It might be a unclosed < or unopened >
			if (value.contains("<") || value.contains(">")) {
				LogHandler.logError("Incorrect mapping value for key:" + key
						+ "Please verify mapping file.Skipping");
				return null;
			}
			LogHandler.logDebug("Normal mapping was added. Writer field=" + key
					+ "/Reader field=" + value);
			return new NormalMapping(value, key);

		}
	}


}
