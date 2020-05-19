package com.pdfcore.main.providers.writer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.pdfcore.main.exceptions.ConfigInitException;
import com.pdfcore.main.exceptions.DataReaderException;
import com.pdfcore.main.exceptions.DataWriterException;
import com.pdfcore.main.exceptions.FormMappingException;
import com.pdfcore.main.interfaces.DataProvider;
import com.pdfcore.main.interfaces.WriterProvider;
import com.pdfcore.main.model.BinaryStream;
import com.pdfcore.main.model.ColIndexMapping;
import com.pdfcore.main.model.ConstructedMapping;
import com.pdfcore.main.model.FormMapping;
import com.pdfcore.main.model.GenResult;
import com.pdfcore.main.model.ImageMapping;
import com.pdfcore.main.model.Mapping;
import com.pdfcore.main.model.NormalMapping;
import com.pdfcore.main.model.PreSufMapping;
import com.pdfcore.main.processor.enums.CompleteFlagEnum;
import com.pdfcore.main.processor.impl.LogHandler;
import com.pdfcore.main.processor.impl.PcdJobConf;
import com.pdfcore.main.service.util.DbUtil;

public class DBWriterProvider extends WriterProvider {

	private final boolean shouldOverwrite;
	private String tableName;
	private final String verifyIfExistQuery;
	private final String deleteQuery;

	private final DbUtil dbUtil;

    //crw: extract identical methods in this class and other classes that extends WriterProvider to
    //the abstract class, WriterProvider

    //crw: this constructor is never used, is it necessary? The one that it's below it does the same thing,
    //but does it better, because you want to know if you should overwrite or not, so delete this
	public DBWriterProvider(PcdJobConf pcdJobConf, String pk)
			throws ConfigInitException {
		super(pk);

		dbUtil = DbUtil.getInstance(pcdJobConf);

		shouldOverwrite = false;
		verifyIfExistQuery = "SELECT count(*) AS norec FROM @tablename WHERE "
				+ getPk() + "=?";
		deleteQuery = "DELETE FROM @tablename WHERE " + getPk() + "=?";
	}

	public DBWriterProvider(PcdJobConf pcdJobConf, String pk,
			boolean shouldOverwrite) throws ConfigInitException {
		super(pk);
		dbUtil = DbUtil.getInstance(pcdJobConf);
		this.shouldOverwrite = shouldOverwrite;
		verifyIfExistQuery = "SELECT count(*) AS norec FROM @tablename WHERE "
				+ getPk() + "=?";
		deleteQuery = "DELETE FROM @tablename WHERE " + getPk() + "=?";
	}

	//crw: refactor this method, it's too long
    //crw: name the variables more properly, like dataProvider and formMapping
    //crw: use internal class logger something like
    //private static final Logger logger = Logger.getLogger(DBWriterProvider.class); in every place you have
    //LogHandler
    @Override
	public ArrayList<GenResult> write(DataProvider dp, FormMapping fm)
			throws DataWriterException, DataReaderException {
		if (dp == null) {
			throw new DataWriterException("Null Data provider provided.");
		}
		if (fm == null) {
			throw new DataWriterException("Null Form mapping provided.");
		}
		ArrayList<String> fmErrors = fm.getErrors();
		if (fmErrors != null) {
            //crw: use internal class logger
			LogHandler
					.logError("Form Mapping provided is invalid. It failed validation with these erorr(s):");
			for (int i = 0; i < fmErrors.size(); i++)
				LogHandler.logError(fmErrors.get(i));
            //crw: it's "Stopped" not "Stoped"
			throw new DataWriterException(
					"Form Mapping failed validation. Stoped write operation");
		}

		LogHandler.logInfo("Starting to write to db for form: "
				+ fm.getFormName());

		tableName = fm.getWriteToName();
		// Starting to build insert script
        //crw: move the result declaration at the beginning of the method
		ArrayList<GenResult> result = new ArrayList<GenResult>();

        /*
        crw: ---- extract the updateScript creation to a new method that returns a String ---
         */
		StringBuilder updateScript = new StringBuilder(
				"insert into @tablename(");
		StringBuilder valuesScript = new StringBuilder(") VALUES(");
		int mappingsNumber = fm.getMappingsNumber();

		for (int i = 0; i < mappingsNumber; i++) {
			updateScript.append(fm.getMapping(i).getWriterField());
			valuesScript.append("?");
            //crw: this if will be executed every single time, except for the last value, so remove it
            //and treat the i=mappingsNumber-1 value outside the for, it's useless
			if (i < mappingsNumber - 1) {
				updateScript.append(", ");
				valuesScript.append(", ");
			}
		}
		valuesScript.append(")");
		String updateSql = updateScript.append(valuesScript.toString())
				.toString();
        /*
        crw: ---- end of updateScript method creation ---
         */
        //crw: internal logger
		LogHandler.logInfo("Update sql script generated: " + updateSql);
        //crw:remove the null initialization for updateScript and valuesScript, it's useless, the garbage collector
        //will remove those variables when this function ends, it won't remove them any sooner if you set them to null
		updateScript = null;
		valuesScript = null;
        //crw: delete the = null initialization, it's useless, you will never use this object before it is initialized
		String pkValue = null;
		Object[] objectValues;
		while (dp.hasNext()) {
            //crw: internal logger, log.info()
			LogHandler
					.logDebug("Data source has more records. Getting primary key for next record");
			try {
				pkValue = dp.getValueForPk();
			} catch (DataReaderException e) {
                //crw: internal logger
				LogHandler
						.logError("Unable to get primary key for record. Errors bellow. Continue at next record");
				LogHandler.logError(e);
				continue;
			}
			GenResult genResult = new GenResult();
			genResult.setId(pkValue);
            //crw: internal logger, log.info()
            //crw: it's "verifying" not "verifing"
			LogHandler.logDebug("Got primary key: " + pkValue
					+ "Verifing if it exists in the db.");
			try {
				// verify if record exists in db
				if (recordExists(pkValue)) {
                    //crw: internal logger and log.info
					LogHandler.logDebug("Record with primary key: " + pkValue
							+ " exists in the db.");
					// verify if should overwrite
					if (shouldOverwrite) {
                        //crw: internal logger and log.info
						LogHandler
								.logDebug("Trying to delete record with primary key : "
										+ pkValue);
						try {
							// delete record
							deleteRecord(pkValue);
                            //crw: extract the code inside the catch clauses to a new method, it's identical
						} catch (ConfigInitException e) {
                            //crw: use String.format
							DataWriterException error = new DataWriterException(
									"Unable to delete record with primary key:"
											+ pkValue
											+ " from db.  Skipping to next record",
									e);
							LogHandler.logError(error);
							genResult.setCompletFlag(CompleteFlagEnum.ERRORS);
							genResult.setError(error);
							result.add(genResult);
							continue;
						} catch (SQLException e) {
							DataWriterException error = new DataWriterException(
									"Unable to delete record with primary key:"
											+ pkValue
											+ " from db.  Skipping to next record",
									e);
							LogHandler.logError(error);
							genResult.setCompletFlag(CompleteFlagEnum.ERRORS);
							genResult.setError(error);
							result.add(genResult);
							continue;
						}
                        //crw: correct the typo in "Rrecord" and use String.format and internal logger and info level
						LogHandler.logDebug("Rrecord with primary key : "
								+ pkValue + " deleted");
					} else {
                        //crw: same code as the catch clauses above
						DataWriterException error = new DataWriterException(
								"Record with primary key:"
										+ pkValue
										+ " exist in db and shouldOverwrite flag is false. Skipping to next record");
						LogHandler.logError(error);
						genResult.setCompletFlag(CompleteFlagEnum.ERRORS);
						genResult.setError(error);
						result.add(genResult);
						continue;
					}
				}
			} catch (ConfigInitException e) {
				DataWriterException error = new DataWriterException(
						"Unable to verify if records exist in db. Skipping to next record",
						e);
				LogHandler.logError(error);
				genResult.setCompletFlag(CompleteFlagEnum.ERRORS);
				genResult.setError(error);
				result.add(genResult);
				continue;
			}
            //crw: extract this to new method
            //crw: internal logger and log.info
			LogHandler.logDebug("Preparing to write for record with pk="
					+ pkValue);
            //crw: internal logger and log.info
			LogHandler.logDebug("Constructing values array");
			objectValues = new Object[fm.getMappingsNumber()];
			boolean errors = false;
			for (int i = 0; i < fm.getMappingsNumber() && errors == false; i++) {
				Mapping mapping = fm.getMapping(i);
				try {
					setObjectI(dp, objectValues, i, mapping);
				} catch (DataReaderException e) {
                    //crw: same method as the catch clauses above, same comments
					DataWriterException error = new DataWriterException(
							"Unable to get mapping value for :"
									+ mapping.getWriterField()
									+ ". Skipping to next record", e);
					LogHandler.logError(error);
					genResult.setCompletFlag(CompleteFlagEnum.ERRORS);
					genResult.setError(error);
					result.add(genResult);
					errors = true;
				} catch (FileNotFoundException e) {
                    //crw: same method as the catch clauses above, same comments
					DataWriterException error = new DataWriterException(
							"Unable to get mapping value for :"
									+ mapping.getWriterField()
									+ ". Skipping to next record", e);
					LogHandler.logError(error);
					genResult.setCompletFlag(CompleteFlagEnum.ERRORS);
					genResult.setError(error);
					result.add(genResult);
					errors = true;
				} catch (FormMappingException e) {
                    //crw: same method as the catch clauses above, same comments
					DataWriterException error = new DataWriterException(
							"Unable to get mapping value for :"
									+ mapping.getWriterField()
									+ ". Skipping to next record", e);
					LogHandler.logError(error);
					genResult.setCompletFlag(CompleteFlagEnum.ERRORS);
					genResult.setError(error);
					result.add(genResult);
					errors = true;
				}

			}
            //crw: use if (!errors) and get rid of the continue
			if (errors)
				continue;
            //crw: internal logger, and log.info
			LogHandler
					.logDebug("Finished constructing values array. Executing update");
			try {
				dbUtil.executeUpdate(updateSql, tableName, objectValues);
			} catch (ConfigInitException e) {
                //crw: same method as the catch clauses above, same comments
				DataWriterException error = new DataWriterException(
						"Unable to execute update for pk:" + pkValue + ". ", e);
				LogHandler.logError(error);
				genResult.setCompletFlag(CompleteFlagEnum.ERRORS);
				genResult.setError(error);
				result.add(genResult);
				continue;
			} catch (SQLException e) {
                //crw: same method as the catch clauses above, same comments
				DataWriterException error = new DataWriterException(
						"Unable to execute update for pk:" + pkValue + ". ", e);
				LogHandler.logError(error);
				genResult.setCompletFlag(CompleteFlagEnum.ERRORS);
				genResult.setError(error);
				result.add(genResult);
				continue;
			}

			// All is successful
			LogHandler.logDebug("Generation for record with pk=" + pkValue
					+ " ended successful.");
			genResult.setCompletFlag(CompleteFlagEnum.COMPLETED);
			result.add(genResult);
		}

		return result;
	}

    //crw: don't use Objects, make an interface to the Mapping class (something like IMapping), make the Mapping class
    // extend that interface and use the interface instead of Object
	private void setObjectI(DataProvider dp, Object[] objectValues, int i,
			Mapping mapping) throws DataReaderException, FileNotFoundException,
			FormMappingException {
		switch (mapping.getType()) {

		case Image:
			File valueForMapping = dp
					.getValueForMapping((ImageMapping) mapping);
			if (valueForMapping == null)
				objectValues[i] = null;
            //crw: not ok to convert long to int using cast, long it's bigger and can cause errors
            //find another way to convert a long to an int or use something else
			BinaryStream bs = new BinaryStream(new FileInputStream(
					valueForMapping), (int) valueForMapping.length());
			objectValues[i] = bs;
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

	private boolean recordExists(String pkValue) throws ConfigInitException {
		try {
			ResultSet rs = dbUtil.executeQuery(verifyIfExistQuery, tableName,
					new Object[] { pkValue });
			if (rs.next()) {
				int norec = rs.getInt("norec");
				if (norec > 0)
					return true;
			}
		} catch (SQLException e) {
			throw new ConfigInitException(e.getMessage(), e);
		}
		return false;
	}

	private void deleteRecord(String pkValue) throws ConfigInitException,
			SQLException {
		dbUtil.executeUpdate(deleteQuery, tableName, new Object[] { pkValue });
	}

}
