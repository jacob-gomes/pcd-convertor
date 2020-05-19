package com.pdfcore.main.providers.formmapping;

import java.io.File;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import com.pdfcore.main.exceptions.ConfigInitException;
import com.pdfcore.main.exceptions.FormMappingException;
import com.pdfcore.main.interfaces.FormMappingProvider;
import com.pdfcore.main.model.ColIndexMapping;
import com.pdfcore.main.model.ConstructedMapping;
import com.pdfcore.main.model.FormMapping;
import com.pdfcore.main.model.ImageMapping;
import com.pdfcore.main.model.NormalMapping;
import com.pdfcore.main.model.PreSufMapping;
import com.pdfcore.main.processor.impl.ConfigFormData;
import com.pdfcore.main.processor.impl.LogHandler;
import com.pdfcore.main.processor.impl.PcdJobConf;
import com.pdfcore.main.service.util.PropertyUtils;

public class PropertiesFormMappingProvider implements FormMappingProvider {

	
	/* (non-Javadoc)
	 * @see com.pdfcore.main.interfaces.FormMappingProvider#getFormMapping(java.lang.String)
	 */
	@Override
	public FormMapping getFormMapping(PcdJobConf pcdJobConf, ConfigFormData formData) throws  FormMappingException {
		if (formData == null)
		{
			throw new FormMappingException("Null form data configuration provided . Unable to construct form mappings");
		}
		String formName = formData.getFormName();
		LogHandler.logInfo("Starting contructing mappings for form: "+formName);
		
		String readerFromName = formData.getReaderFromName();
		if (readerFromName==null || readerFromName.trim().length() == 0)
		{
			throw new FormMappingException("Null or 0 length read from name provided . Unable to construct form mappings");
		}
		String writerToName = formData.getWriterToName();
		if (writerToName==null || writerToName.trim().length() == 0)
		{
			throw new FormMappingException("Null or 0 length write to name provided . Unable to construct form mappings");
		}


	    //FormMapping form = new FormMapping(formName, readerFromName, writerToName, formData.getOutputFolderPattern(), formData.getOutputFileNameColumn());
        FormMapping form = new FormMapping(formName, readerFromName, writerToName, formData);

		Properties prop;
        try
        {
        	String fileName;
        	
        	if (pcdJobConf.getConfPath().equalsIgnoreCase("")) {
        		fileName = pcdJobConf.getMappingsFolder() + File.separator + formName + ".properties";
        	} else {
        		fileName = pcdJobConf.getConfPath() + File.separator +
                        pcdJobConf.getMappingsFolder() + File.separator + formName + ".properties";
        	}
            LogHandler.logInfo("Form mappings will be read from the file: "+fileName);
			prop = PropertyUtils.getProperties(fileName);
        } catch (ConfigInitException e)
        {
	        throw new FormMappingException("Could not get the properties mapping file",e);
        }
        LogHandler.logInfo("Form mapping file found. Start reading mappings");
        
        
        if (prop == null)
		{
			throw new FormMappingException("Properties file is null.");
		}
		Set<Object> keySet = prop.keySet();
		if (keySet == null || keySet.size() ==0 )
			throw new FormMappingException("No mappings where found in properties file");
		Iterator<Object> it = keySet.iterator();
		while (it.hasNext())
		{		//Get the key and validate
				String key = (String) it.next();
				if (key == null || key.trim().length()==0)
				{
					LogHandler.logError("Null or 0 length key was found in mapping file.Skipping");
					continue;
				}
				LogHandler.logDebug("Found new mapping with key: "+key+". Determing mapping type");
				
				//Get the value and validate
				String value = prop.getProperty(key);
				
				if (value == null || value.trim().length()==0)
				{
					LogHandler.logError("Null or 0 length value was found for mapping key: "+key+". Skipping");
					continue;
				}
				// Determine what type of mapping it is

				if (value.contains("<<<<") && value.contains(">>>>"))
				{   // It is an image mapping	
					int beginIndex = value.indexOf("<<<<")+4;
					int endIndex = value.indexOf(">>>>");
					//Get the image name
					String imageName = value.substring(beginIndex, endIndex);
					//Validate image name
					if (imageName == null || imageName.trim().length() == 0)
					{
						LogHandler.logError("Image mapping found but null or 0 length image name specified for mapping: "+key+". Skipping");
						continue;
					}
					//Create and add the mapping
					LogHandler.logDebug("Image mapping was added. Image name: "+imageName);
					form.addMappings(new ImageMapping(imageName,key));
					
				}
				else
				if (value.contains("<<<") && value.contains(">>>"))
				{
					//It's an index mapping
					int beginIndex = value.indexOf("<<<")+3;
					int endIndex = value.indexOf(">>>");
					String realField = value.substring(beginIndex, endIndex);
					//validate realField
					if (realField == null || realField.trim().length() == 0)
					{
						LogHandler.logError("Index mapping found but null or 0 length field name specified for mapping: "+key+". Skipping");
						continue;
					}
					//Create and add the mapping
					LogHandler.logDebug("Index mapping was added. Index field name: "+realField);
					form.addMappings(new ColIndexMapping(realField,key));
				}
				else if (value.contains("<<") && value.contains(">>") )
			      {
			    	 //It's a constructed mapping
			    	  int firstPos = value.indexOf("<<")+2;
			    	  int lastPos = value.indexOf(">>");
			    	  String realField = value.substring(firstPos, lastPos);
			    	  //if the string is empty put null
			    	  if (realField!=null && realField.trim().length()==0)
			    		  realField=null;
			    	  LogHandler.logDebug("Constructed mapping was added.");
					  form.addMappings(new ConstructedMapping(realField, key));
			    	  
			      } 
			      else
			    	  if (value.contains("<") && value.contains(">"))
			    	  {
			    		  int firstPos = value.indexOf("<");
			    		  int lastPos = value.indexOf(">");
			    		  String prefix = value.substring(0, firstPos);
			    		  String sufix = value.substring(lastPos+1, value.length());
			    		  String dataField = value.substring(firstPos+1,lastPos);
			    		  if ((prefix == null || prefix.length()==0)&&(sufix == null || sufix.length()==0))
			    			  LogHandler.logInfo("Prefix/Suffix mapping was found but no prefix or suffix was provided. Consider using a normal mapping");
			    		  if (dataField == null || dataField.length() == 0)
			    		  {
			    			  LogHandler.logError("Prefix/Suffix mapping was found but null or 0 length field was provided for mapping: "+key+". Skipping");
			    			  continue;
			    		  }
			    		  LogHandler.logDebug("Prefix/Suffix mapping was added.");
						  form.addMappings(new PreSufMapping(dataField, key,prefix,sufix));
			    	  }
			    	  else
			    	  {
			    		  //It might be a unclosed < or unopened >
			    		  if (value.contains("<")||value.contains(">"))
			    		  {
			    			  LogHandler.logError("Incorrect mapping value for key:"+key+"Please verify mapping file.Skipping");
			    			  continue;
			    		  }
			    		  LogHandler.logDebug("Normal mapping was added. Writer field="+key+"/Reader field="+value);
						  form.addMappings(new NormalMapping(value, key));
			    		  
			    	  }
			      
		    }
			
		
		LogHandler.logInfo("Mappings read and created successful.");
		return form;
	}


}
