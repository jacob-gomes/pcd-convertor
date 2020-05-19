/**
 * 
 */
package com.pdfcore.main.providers.formmapping;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import au.com.bytecode.opencsv.CSVReader;
import com.pdfcore.main.exceptions.FormMappingException;
import com.pdfcore.main.model.FormMapping;
import com.pdfcore.main.processor.impl.ConfigFormData;
import com.pdfcore.main.processor.impl.LogHandler;
import com.pdfcore.main.processor.impl.PcdJobConf;

/**
 * @author tqn
 * 
 */
public abstract class AbstractCsvFormMappingProvider extends AbstractFormMappingProvider {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.pdfcore.main.interfaces.FormMappingProvider#getFormMapping(com.pdfcore
	 * .main.util.ConfigFormData)
	 */
	@Override
	public FormMapping getFormMapping(PcdJobConf pcdJobConf, ConfigFormData formData)
			throws FormMappingException {
		if (formData == null) {
			throw new FormMappingException(
					"Null form data configuration provided . Unable to construct form mappings");
		}
		String formName = formData.getFormName();
		LogHandler.logInfo("Starting contructing mappings for form: "
				+ formName);

		String readerFromName = formData.getReaderFromName();
		if (readerFromName == null || readerFromName.trim().length() == 0) {
			throw new FormMappingException(
					"Null or 0 length read from name provided . Unable to construct form mappings");
		}
		String writerToName = formData.getWriterToName();
		if (writerToName == null || writerToName.trim().length() == 0) {
			throw new FormMappingException(
					"Null or 0 length write to name provided . Unable to construct form mappings");
		}
		FormMapping form = new FormMapping(formName, readerFromName, writerToName);

		CSVReader prop;
		try {
			String fileName = pcdJobConf.getConfPath() + File.separator + pcdJobConf.getMappingsFolder() + File.separator
					+ formName + ".csv";
			LogHandler.logInfo("Form mappings will be read from the file: "
					+ fileName);
			prop = new CSVReader(
					new FileReader(new File(fileName)));

//		} catch (ConfigInitException e) {
//			throw new FormMappingException(
//					"Could not get the properties mapping file", e);
		} catch (FileNotFoundException e) {
			throw new FormMappingException(
					"Could not get the properties mapping file", e);
		}

		LogHandler.logInfo("Form mapping file found. Start reading mappings");

		

		processRows(form, prop);

		LogHandler.logInfo("Mappings read and created successful.");
		return form;

	}

	/**
	 * @param form
	 * @param prop
	 * @throws FormMappingException
	 */
	abstract protected void processRows(FormMapping form, CSVReader prop)
			throws FormMappingException;
}
