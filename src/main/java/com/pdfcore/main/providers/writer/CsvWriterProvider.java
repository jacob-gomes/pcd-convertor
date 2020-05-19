package com.pdfcore.main.providers.writer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.pdfcore.main.exceptions.ConfigInitException;
import com.pdfcore.main.exceptions.DataReaderException;
import com.pdfcore.main.exceptions.DataWriterException;
import com.pdfcore.main.exceptions.FormMappingException;
import com.pdfcore.main.interfaces.DataProvider;
import com.pdfcore.main.interfaces.WriterProvider;
import com.pdfcore.main.model.ColIndexMapping;
import com.pdfcore.main.model.ConstructedMapping;
import com.pdfcore.main.model.FormMapping;
import com.pdfcore.main.model.GenResult;
import com.pdfcore.main.model.Mapping;
import com.pdfcore.main.model.NormalMapping;
import com.pdfcore.main.model.PreSufMapping;
import com.pdfcore.main.processor.enums.CompleteFlagEnum;
import com.pdfcore.main.processor.impl.LogHandler;
import com.pdfcore.main.processor.impl.PcdJobConf;
import com.pdfcore.main.service.util.PropertyUtils;

import au.com.bytecode.opencsv.CSVWriter;

public class CsvWriterProvider extends WriterProvider {

	private final PcdJobConf pcdJobConf;

    //crw: extract identical methods in this class and other classes that extends WriterProvider to
    //the abstract class, WriterProvider
	public CsvWriterProvider(PcdJobConf pcdJobConf, String pk) {
		super(pk);
		this.pcdJobConf = pcdJobConf;
	}

    //crw: refactor this, split in smaller functions, this method does too many things
    //crw: DataProvider instance and FormMapping instance have a large scope, so name them in a more suggestive manner
    //like dataProvider and formMapping
	@Override
	public ArrayList<GenResult> write(DataProvider dp, FormMapping fm)
			throws DataWriterException, DataReaderException {
		if (dp == null) {
			throw new DataWriterException("Null Data provider provided.");
		}
		if (fm == null) {
			throw new DataWriterException("Null Form mapping provided.");
		}
        //crw: fm.getErrors() should return an empty list, I'll place this comment in FormMapping class too, but
        //be ready for this change
		ArrayList<String> fmErrors = fm.getErrors();
		if (fmErrors != null) {
            //crw: make an internal class logger, something like
            //private static final Logger logger = Logger.getLogger(CsvWriterProvider.class);
			LogHandler
					.logError("Form Mapping provided is invalid. It failed validation with these erorr(s):");
			for (int i = 0; i < fmErrors.size(); i++)
                //crw: use internal logger
				LogHandler.logError(fmErrors.get(i));
            //crw: "Stoped" is actually "Stopped" in english, correct the message
			throw new DataWriterException(
					"Form Mapping failed validation. Stoped write operation");
		}
        //crw: you should declare "global" method variables like result when the function starts
        //in order to have a better picture
		CSVWriter writer;
		ArrayList<GenResult> result = new ArrayList<GenResult>();
        //crw: user StringBuilder
		String fileName = pcdJobConf.getOutputFolder() + File.separator
				+ pcdJobConf.getCsvFolder() + File.separator
				+ fm.getWriteToName() + System.currentTimeMillis()
				+ pcdJobConf.getCsvExtension();
        //crw: use internal logger
		LogHandler.logInfo("Preparing to open output csv file: " + fileName);
		File file;
		try {
			file = PropertyUtils.getFile(pcdJobConf, fileName);
		} catch (ConfigInitException e) {
            //crw: use String.format()
			throw new DataWriterException("Unable to get output file:"
					+ fileName + "See cause for details.", e);
		}
		try {
			writer = new CSVWriter(new FileWriter(file));
		} catch (IOException e) {
            //crw: use String.format()
			throw new DataWriterException("Unable to get output file:"
					+ fileName + "See cause for details.", e);
		}
        /* crw:
         ---------- you can extract the header writing to a new method ------
         */
        //crw: use internal logger
		LogHandler.logInfo("Output csv file opened. Starting to write");
        //crw: use  log level info
		LogHandler.logDebug("Preparing header of the csv file");
		// Prepare csv file header
		int mappingsNumber = fm.getMappingsNumber();

		String[] fields = new String[mappingsNumber];
		for (int i = 0; i < mappingsNumber; i++) {
			Mapping mapping = fm.getMapping(i);
            //crw: use log level info on internal logger
			LogHandler.logDebug("Adding next header: " + mapping.getWriterField());
			fields[i] = mapping.getWriterField();
		}
		writer.writeNext(fields);
        //crw: use log info on internal logger
		LogHandler.logDebug("Done writing header of the csv file");
        /*
        ------ end header writing method ----
         */
		String pkValue;
		while (dp.hasNext()) {
            //crw: internal logger and log level info
            /* crw:
            --- extract this to new method that returns pkValue
             */
			LogHandler
					.logDebug("Data source has more records. Getting primary key for next record");
			try {
				pkValue = dp.getValueForPk();
			} catch (DataReaderException e) {
                //crw: use internal logger
				LogHandler
						.logError("Unable to get primary key for record. Errors bellow. Continue at next record");
				LogHandler.logError(e);
				continue;
			}
            /*
            ---- end method
             */
			GenResult genResult = new GenResult();
			genResult.setId(pkValue);
			String[] objectValues = new String[mappingsNumber];
            //crw: use internal logger
			LogHandler.logDebug("Got primary key: " + pkValue
					+ " Preparing value for insert");
			boolean errors = false;
            //crw: you can use !errors instead of errors == false for clearer code
			for (int i = 0; i < mappingsNumber && errors == false; i++) {
				Mapping mapping = fm.getMapping(i);
				try {
					setObjectI(dp, objectValues, i, mapping);
                    //crw: in both catch clauses there is the same code, extract it to a new method
				} catch (DataReaderException e) {
                    //crw: use String.format(), it's not ok to concatenate Strings with +
					DataWriterException error = new DataWriterException(
							"Unable to get mapping value for :"
									+ mapping.getWriterField()
									+ ". Skipping to next record", e);
                    //crw: internal logger
					LogHandler.logError(error);
					genResult.setCompletFlag(CompleteFlagEnum.ERRORS);
					genResult.setError(error);
					result.add(genResult);
					errors = true;
				} catch (FormMappingException e) {
                    //crw: String.format()
					DataWriterException error = new DataWriterException(
							"Unable to get mapping value for :"
									+ mapping.getWriterField()
									+ ". Skipping to next record", e);
                    //crw: internal logger
					LogHandler.logError(error);
					genResult.setCompletFlag(CompleteFlagEnum.ERRORS);
					genResult.setError(error);
					result.add(genResult);
					errors = true;
				}
			}
            //crw: get rid of this if with continue, make something like if (!errors){ --the code that follows--}
			if (errors)
				continue;
            //crw: internal logger and log level info
			LogHandler
					.logDebug("Finished constructing values array. Executing update");
			writer.writeNext(objectValues);
			// All is successful
            //crw: internal logger and log level info
			LogHandler.logDebug("Generation for record with pk=" + pkValue
					+ " ended successful.");
			genResult.setCompletFlag(CompleteFlagEnum.COMPLETED);
			result.add(genResult);
		}
		try {
			writer.close();
		} catch (IOException e) {
			throw new DataWriterException(
					"Error while trying to close csv file. All written data is corrupted. See cause for details",
					e);
		}

		return result;

	}

    //crw: don't use Objects, make an interface to the Mapping class, and use the interface
	private void setObjectI(DataProvider dp, Object[] objectValues, int i,
			Mapping mapping) throws DataReaderException, FormMappingException {
		switch (mapping.getType()) {

		case Image:
			LogHandler
					.logError("Trying to insert an image to a csv file . Operation not supported . Will insert null");
			objectValues[i] = "";
			break;
		case ColumnIndex:
			objectValues[i] = dp.getValueForMapping((ColIndexMapping) mapping);
			break;
		case Constructed:
			objectValues[i] = dp
					.getValueForMapping((ConstructedMapping) mapping);
			break;
		case Normal:
			objectValues[i] = dp.getValueForMapping((NormalMapping) mapping);
			break;
		case PreSufMapping:
			objectValues[i] = dp.getValueForMapping((PreSufMapping) mapping);
			break;
		default:
			throw new FormMappingException("Unknown mapping provided");
		}
	}
}
