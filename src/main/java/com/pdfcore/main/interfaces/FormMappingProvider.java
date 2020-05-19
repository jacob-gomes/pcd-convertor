package com.pdfcore.main.interfaces;

import com.pdfcore.main.exceptions.FormMappingException;
import com.pdfcore.main.model.FormMapping;
import com.pdfcore.main.processor.impl.ConfigFormData;
import com.pdfcore.main.processor.impl.PcdJobConf;

/**
 * @author OgrisorJ
 * 
 */

public interface FormMappingProvider {

	
	/**
	 * Constructs the mapping for a form
	 *
     * @param pcdJobConf@return FormMappings
	 * @throws FormMappingException
	 */
	public FormMapping getFormMapping(PcdJobConf pcdJobConf, ConfigFormData formData) throws FormMappingException;
	
}
