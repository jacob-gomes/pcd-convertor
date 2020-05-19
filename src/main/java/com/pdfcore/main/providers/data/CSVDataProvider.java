package com.pdfcore.main.providers.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.util.ArrayList;

import com.pdfcore.main.exceptions.ConfigInitException;
import com.pdfcore.main.exceptions.DataReaderException;
import com.pdfcore.main.interfaces.DataProvider;
import com.pdfcore.main.interfaces.FieldValueConstructor;
import com.pdfcore.main.model.ColIndexMapping;
import com.pdfcore.main.model.ConstructedMapping;
import com.pdfcore.main.model.FormMapping;
import com.pdfcore.main.model.ImageMapping;
import com.pdfcore.main.model.NormalMapping;
import com.pdfcore.main.model.PreSufMapping;
import com.pdfcore.main.processor.impl.CsvWraper;
import com.pdfcore.main.processor.impl.LogHandler;
import com.pdfcore.main.processor.impl.PcdJobConf;
import com.pdfcore.main.service.util.PropertyUtils;

import au.com.bytecode.opencsv.CSVReader;

public class CSVDataProvider implements DataProvider {

	private final CsvWraper reader;
	private final FieldValueConstructor valueConstructor;
	private final ArrayList<String> ids;
	private final String pkInReader;
    private final PcdJobConf pcdJobConf;
    private final FormMapping formMapping;

    //crw: useless javadoc, delete it or fill it
    /**
	 * @param formMapping
	 * @param constructor
	 * @param ids
	 * @param pk
	 * @throws DataReaderException
	 */
	public CSVDataProvider(PcdJobConf pcdJobConf, FormMapping formMapping, FieldValueConstructor constructor, ArrayList<String> ids, String pk)
	        throws DataReaderException {
        //crw: use an internal class logger, something like:
        //private static final Logger logger = Logger.getLogger(CSVDataProvider.class);
		LogHandler.logInfo("Constructing CSV Data Source for form: " + formMapping.getFormName());
        //crw: log.info instead of log.debug, we don't use log.debug in production environment
		LogHandler.logDebug("Setting field constructor to: "
		        + (constructor == null ? "null" : constructor.getClass().getCanonicalName()));
		this.valueConstructor = constructor;
        this.pcdJobConf = pcdJobConf;
        this.formMapping = formMapping;

		if (ids == null || ids.size() == 0) {
            //crw:log as warning
			LogHandler.logInfo("There are no ids specified . Generating for all csv file");
			this.ids = null;
		} else {
			this.ids = ids;
		}

		if (pk == null || pk.trim().length() == 0)
            //crw: it's "Invalid" not "Unvalid" in English
			throw new DataReaderException("Unvalid primary key specified . It is null or 0 length");
        //crw: log.info
		LogHandler.logDebug("Setting primary key for data source to: " + pk);
		pkInReader = pk;
		String fileName = pcdJobConf.getConfPath() + File.separator + pcdJobConf.getInputFolder() + File.separator + pcdJobConf.getCsvFolder() + File.separator
		        + formMapping.getReadFromName() + pcdJobConf.getCsvExtension();
		File file;
        //crw: delete the commented code, you have it in svn history if you'll need it in the future
//		try {

			LogHandler.logInfo("CSV data provider trying to read from : " + fileName);
			file = new File(fileName);
//		} catch (ConfigInitException e) {
//			throw new DataReaderException("Was unable to open for read csv file. See cause for more details", e);
//		}
		try {
			reader = new CsvWraper(new CSVReader(new FileReader(file)));
		} catch (FileNotFoundException e) {
			throw new DataReaderException("The file: " + fileName + " was not found. See cause for more details", e);
		} catch (ConfigInitException e) {
            //crw: throw ConfigInitException
			throw new DataReaderException("I/O exception while trying to open file" + fileName
			        + "See cause for more details", e);
		}
		// Validating data source.
		// if constructor is null verify that there are no Constructed Mappings
        //crw: should make the checks before assigning class variables
		if (this.valueConstructor == null && formMapping.hasConstructedMappings()) {
			throw new DataReaderException("Null Value constructor provided but Constructed mapping found.");
		}
		// verify that pk is in data source.
		if (reader.getIndexForColumn(pkInReader) < 0) {
			throw new DataReaderException("The primary key you provided: " + this.pkInReader
			        + " was not found in CSV file");
		}

	}

	@Override
	public String getValueForMapping(NormalMapping mapping) throws DataReaderException {
		String valueForColumn = reader.getValueForColumn(mapping.getReaderField());
		return valueForColumn == null ? "" : valueForColumn;
	}

	@Override
	public String getValueForMapping(PreSufMapping mapping) throws DataReaderException {
		String prefix = mapping.getPrefix() == null ? "" : mapping.getPrefix();
		String valueForColumn = reader.getValueForColumn(mapping.getReaderField());
		if (valueForColumn == null) {
			valueForColumn = "";
		}
		
		String suffix = mapping.getSuffix() == null ? "" : mapping.getSuffix();
		return  prefix + valueForColumn + suffix;
	}

	@Override
	public String getValueForMapping(ConstructedMapping mapping) throws DataReaderException {
		String value = null;
        //crw: move different cases in different functions so that the code could be more readable
		if (mapping.getReaderField() != null && mapping.getReaderField().trim().length() > 0) {
			if (mapping.isMultipleFieldConcat()) {
				String[] values = mapping.getReaderField().split("\\|");
                //crw: you could name the s variable in a more suggestive way
				String s = values[0];
				value = reader.getValueForColumn(s);
				for (int i = 1; i < values.length; i++) {
					s = values[i];
					value = value.concat(", ").concat(reader.getValueForColumn(s));
				}
				return value;
			}
			
			if (mapping.isFormatField()) {
				String readerField = mapping.getReaderField();
				
				String field =readerField.substring(0, readerField.indexOf("["));
				value = reader.getValueForColumn(field);
				String format = readerField.substring(readerField.indexOf("[") + 1, readerField.length() - 1);
				DecimalFormat f = new DecimalFormat(format);
                //crw: why do you transform a String to a Double and then format it again as a String? it is useless
				return f.format(Double.valueOf(value));
			}
			
			value = reader.getValueForColumn(mapping.getReaderField());
			
							
		}
		return valueConstructor.getValueForField(mapping.getWriterField(), value, formMapping.getNumWordCapital());
	}

	@Override
	public File getValueForMapping(ImageMapping mapping) throws DataReaderException {
		String imageName = mapping.getReaderField();
        //crw: use the logger
        System.out.println("csv: image file name is :" + imageName);
        String imagePath = pcdJobConf.getImageFolder() + File.separator + imageName;
		try {
			return PropertyUtils.getFile(pcdJobConf, imagePath);
		} catch (ConfigInitException e) {
			throw new DataReaderException("Unable to get image at path:" + imagePath, e);
		}
	}

	@Override
	public boolean hasNext() {
		while (reader.next()) {
            if (shouldWriteForId(reader.getValueForColumn(pkInReader))) {
                return true;
            }
        }
		return false;
	}

	public boolean shouldWriteForId(String id) {
		if (ids == null)
			return true;
		return ids.contains(id);
	}

	@Override
	public String getValueForPk() throws DataReaderException {
		return reader.getValueForColumn(pkInReader);
	}

	@Override
	public String getValueForMapping(ColIndexMapping mapping) throws DataReaderException {
		return String.valueOf(reader.getIndexForColumn(mapping.getReaderField()));
	}

}
