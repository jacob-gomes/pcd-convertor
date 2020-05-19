/**
 * 
 */
package com.pdfcore.main.providers.formmapping;

import com.pdfcore.main.exceptions.FormMappingException;
import com.pdfcore.main.interfaces.FormMappingProvider;
import com.pdfcore.main.model.enums.FileMappingType;

/**
 * @author tqn
 * 
 */
public class FormMappingProviderFactory {
	private static FormMappingProviderFactory instance;

	/**
	 * 
	 */
	private FormMappingProviderFactory() {
	}

	public static FormMappingProviderFactory getInstance() {
		if (instance == null) {
			instance = new FormMappingProviderFactory();
		}
		return instance;
	}

	public FormMappingProvider getFormMapping(String fileType) throws FormMappingException {
		FileMappingType fileMappingType = FileMappingType.parse(fileType);
		switch (fileMappingType) {
		case PROPERTIES:
			return new PropertiesFormMappingProvider();
		case ROW_CSV:
			return new RowCsvFormMappingProvider();
		case COL_CSV:
			return new ColCsvFormMappingProvider();
		default:
			throw new FormMappingException("unsupported file mapping type");
		}
	}
}
