/**
 * 
 */
package com.pdfcore.main.providers.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import com.lowagie.text.pdf.PdfReader;
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
import com.pdfcore.main.processor.impl.LogHandler;
import com.pdfcore.main.processor.impl.PcdJobConf;
import com.pdfcore.main.processor.impl.PdfWraper;
import com.pdfcore.main.service.util.PropertyUtils;

/**
 * @author tqn
 * 
 */
public class PDFDataProvider implements DataProvider {

    private final FormMapping formMapping;
    private FieldValueConstructor valueConstructor;
	private final ArrayList<String> ids;
	private String pkInReader;
	private PdfWraper reader;
    private final PcdJobConf pcdJobConf;

    public PDFDataProvider(PcdJobConf pcdJobConf, FormMapping formMapping,
			FieldValueConstructor constructor, ArrayList<String> ids, String pk, int numberOfRecordPerPages, boolean autoIncreaseFieldIndex, int numberOfPage, boolean autoIncreasePageIndex)
			throws DataReaderException {

        this.pcdJobConf = pcdJobConf;
        this.formMapping = formMapping;

		String formName = formMapping.getFormName();
		String pdfName = formMapping.getReadFromName();

        //crw: use an internal class logger, something like:
        //private static final Logger logger = Logger.getLogger(PDFDataProvider.class);
		LogHandler
				.logInfo("Constructing PDF data source for form: " + formName);
		if (pdfName == null || pdfName.trim().length() == 0) {
			throw new DataReaderException(
					"Invalid reader name provided in config file. It is null or 0 length");
		}
        //crw: internal logger, on log.info
		LogHandler.logDebug("Setting Field Constructor to :"
				+ (constructor == null ? "null" : constructor.getClass()
						.getCanonicalName()));
		this.valueConstructor = constructor;

		if (ids == null || ids.size() == 0) {
            //crw: internal logger
			LogHandler
					.logInfo("There are no ids specified . Generating for all table");
			this.ids = null;
		} else {
			this.ids = ids;
		}

		if (pk == null || pk.trim().length() == 0)
            //crw: In English it is called Invalid, not "Unvalid"
			throw new DataReaderException(
					"Unvalid primary key specified . It is null or 0 length");
        //crw: logger.info
		LogHandler.logDebug("Setting primary key for data source to: " + pk);
		pkInReader = pk;
		String fileName = pcdJobConf.getPdfFolder() + File.separator
				+ formMapping.getReadFromName() + pcdJobConf.getPdfExtension();
		File file;
		try {
            //crw: internal logger
			LogHandler.logInfo("PDF data provider trying to read from : "
					+ fileName);
			file = PropertyUtils.getFile(pcdJobConf, fileName);
		} catch (ConfigInitException e) {
			throw new DataReaderException(
					"Was unable to open for read pdf file. See cause for more details",
					e);
		}
		try {
			reader = new PdfWraper(new PdfReader(new FileInputStream(file)), numberOfRecordPerPages, autoIncreaseFieldIndex, numberOfPage, autoIncreasePageIndex);
		} catch (FileNotFoundException e) {
            //crw: throw FileNotFoundException, it's from java so you don't have to worry about decoupling.
			throw new DataReaderException("The file: " + fileName
					+ "was not found. See cause for more details", e);
		} catch (ConfigInitException e) {
             //crw: throw ConfigInitException
			throw new DataReaderException(
					"I/O exception while trying to open file" + fileName
							+ "See cause for more details", e);
		} catch (IOException e) {
            //crw: log the error with logger
            //crw2: why you throw some of the exceptions and log the others?
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Validating data source.
		// if constructor is null verify that there are no Constructed Mappings
        //crw: make the validations before assigning the class variables
		if (this.valueConstructor == null
				&& formMapping.hasConstructedMappings()) {
			throw new DataReaderException(
					"Null Value constructor provided but Constructed mapping found.");
		}
        //crw: delete this code if it's not necessary anymore. If you'll ever want it again you have it in svn history

		// verify that pk is in data source.
//		if (StringUtils.isBlank(reader.getValueForColumn(pkInReader))) {
//			throw new DataReaderException("The primary key you provided: "
//					+ this.pkInReader + " was not found in CSV file");
//		}

	}

	@Override
	public String getValueForMapping(NormalMapping mapping)
			throws DataReaderException {
		String valueForColumn = reader.getValueForColumn(mapping
				.getReaderField());
		return valueForColumn == null ? "" : valueForColumn;
	}

	@Override
	public String getValueForMapping(PreSufMapping mapping)
			throws DataReaderException {
		String prefix = mapping.getPrefix() == null ? "" : mapping.getPrefix();
		String valueForColumn = reader.getValueForColumn(mapping
				.getReaderField());
		String suffix = mapping.getSuffix() == null ? "" : mapping.getSuffix();
		return  prefix + valueForColumn + suffix;

	}

	@Override
	public String getValueForMapping(ConstructedMapping mapping)
			throws DataReaderException {
		String value = null;
		if (mapping.getReaderField() != null
				&& mapping.getReaderField().trim().length() > 0)
			value = reader.getValueForColumn(mapping.getReaderField());
		return valueConstructor.getValueForField(mapping.getWriterField(), value, formMapping.getNumWordCapital());
	}

	@Override
	public File getValueForMapping(ImageMapping mapping) throws DataReaderException {
		String imageName = mapping.getReaderField();
		String imagePath = pcdJobConf.getImageFolder() + File.separator + imageName;
        //crw: use the logger
        System.out.println("pdf: image file name is : " + imageName);
        try {

			return PropertyUtils.getFile(pcdJobConf, imagePath);
		} catch (ConfigInitException e) {
			throw new DataReaderException("Unable to get image at path:"
					+ imagePath, e);
		}
	}

	@Override
	public boolean hasNext() {
		while (reader.next()) {
			if (shouldWriteForId(reader.getValueForColumn(pkInReader)))
				return true;
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
	public String getValueForMapping(ColIndexMapping mapping)
			throws DataReaderException {
        //crw: this function doesn't do anything
		throw new DataReaderException("unsupported operation for pdf");
	}


}
