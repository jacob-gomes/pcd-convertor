package com.pdfcore.main.providers.data;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.pdfcore.main.exceptions.ConfigInitException;
import com.pdfcore.main.exceptions.DataReaderException;
import com.pdfcore.main.interfaces.DataProvider;
import com.pdfcore.main.interfaces.FieldValueConstructor;
import com.pdfcore.main.model.BinaryStream;
import com.pdfcore.main.model.ColIndexMapping;
import com.pdfcore.main.model.ConstructedMapping;
import com.pdfcore.main.model.FormMapping;
import com.pdfcore.main.model.ImageMapping;
import com.pdfcore.main.model.NormalMapping;
import com.pdfcore.main.model.PreSufMapping;
import com.pdfcore.main.processor.impl.ConfigFormData;
import com.pdfcore.main.processor.impl.LogHandler;
import com.pdfcore.main.processor.impl.PcdJobConf;
import com.pdfcore.main.service.util.DbUtil;
import com.pdfcore.main.service.util.PropertyUtils;

public class DBDataProvider implements DataProvider {
	private static final Logger logger = Logger.getLogger(DBDataProvider.class);

	private final FieldValueConstructor valueConstructor;
	private final ArrayList<String> ids;
	private final String pkInReader;
	private final DbUtil dbUtil;
	private final PcdJobConf pcdJobConf;
	private final ConfigFormData configFormData;

    //crw: place access modifiers on this variables
	Connection dbConnection;
	ResultSet resultSet;
	PreparedStatement stmt;
	Map<String, ColumnData> currentRecord;

	List<String> columnNames;

    /* Overall crw:
       1) make the checks first and then make the assignments to the class variables
       2) move all the checks to a new method, something like validateParameters()
     */
	public DBDataProvider(PcdJobConf pcdJobConf, FormMapping formMapping,
			ConfigFormData form, ArrayList<String> ids, String pk)
			throws DataReaderException, SQLException {

		FieldValueConstructor constructor = form.getValueConstrucor();
		String formName = formMapping.getFormName();
		String tableName = formMapping.getReadFromName();
		this.pcdJobConf = pcdJobConf;
		this.configFormData = form;

        //crw: why do you use two loggers to log messages? Use only the logger in this class
        //crw2: we shouldn't use log.debug in a production environment
		logger.debug("rr: tableName = " + tableName);
		LogHandler.logInfo("Constructing DB data source for form: " + formName);
		if (tableName == null || tableName.trim().length() == 0) {
			throw new DataReaderException(
					"Invalid reader name provided in config file. It is null or 0 length");
		}
        //crw: use the logger from this class
		LogHandler.logDebug("Setting Field Constructor to :"
				+ (constructor == null ? "null" : constructor.getClass()
						.getCanonicalName()));
		this.valueConstructor = constructor;

		if (ids == null || ids.size() == 0) {
            //crw: one of your methods arguments is null, so usually you expect it to be otherwise
            //because of that you should change the log level to warning if you start processing data with
            //a default behaviour
			LogHandler
					.logInfo("There are no ids specified . Generating for all table");
			this.ids = null;
		} else {
			this.ids = ids;
		}

		if (pk == null || pk.trim().length() == 0) {
			throw new DataReaderException(
					"Unvalid primary key specified . It is null or 0 length");
		}
        //crw: we don't use debug level in a production environment, switch it to info
        //crw2: use the class logger
		LogHandler.logDebug("Setting primary key for data source to: " + pk);
		pkInReader = pk;
		// Validating data source.
		// if constructor is null verify that there are no Constructed Mappings
		if (this.valueConstructor == null
				&& formMapping.hasConstructedMappings()) {
			throw new DataReaderException(
					"Null Value constructor provided but Constructed mapping found. ");
		}
		try {
			dbUtil = DbUtil.getInstance(pcdJobConf);
            //crw: why do you throw a different type of exception?
            //It is good to throw a custom exception when you want to uncuple from a library you use, or something
            //that is not in your own project, but in this case, ConfigInitException is our own exception, so
            //creating a new exception is not justified.
		} catch (ConfigInitException e) {
			throw new DataReaderException(
					"Was unable to retrive records from database. See cause for details.",
					e);
		}

		String where = generateWhereCondition();

		if (this.ids == null) {
			try {
                //crw: be consistent, if you use String.format() for tableName, then
                //include the where clause also in the String.format()
				String sql = String.format("SELECT * FROM %s WHERE " + where,
						tableName);
                //crw: logger.info
				logger.debug("sql is: " + sql);
                //crw: don't send null, send an array with 0 objects. See prepareData crw for more details
				prepareData(sql, null);
			} catch (ConfigInitException e) {
                //crw: throw ConfigInitException instead of DataReaderException, see above the explanation
				throw new DataReaderException(
						"Was unable to retrive records from database. See cause for details.",
						e);
			}
		} else {

			// Preparing ids for select
            //crw: if you are using StringBuilder then use it as it was intended, append all that statement in the
            //StringBuilder constructor. For every + between two String objects, a new String object is made
            //in memory, so you make 6 useless String objects in a StringBuilder constructor argument
			StringBuilder sqlQuery = new StringBuilder("SELECT * FROM "
					+ tableName + " WHERE " + "(" + where + ") and (");
			Object[] values = new Object[ids.size()];
			for (int i = 0; i < ids.size(); i++) {
                //crw: you know you make ids.size()-1 useless if's
                //hint 1:extract the i=0 case outside the for and remove the if(i>0)
                //hint 2: transform "OR " into " OR", append it after the element and go to <ids.size()-1
				if (i > 0) {
					sqlQuery.append("OR ");
				}
				sqlQuery.append(pkInReader + "=? ");
				values[i] = ids.get(i);
			}
			sqlQuery.append(" )");
			// Execute query
			try {
				String sql = sqlQuery.toString();
                //crw: logger.info
				logger.debug("sql is: " + sql);
				prepareData(sql, values);
			} catch (ConfigInitException e) {
                //crw" throw ConfigInitException
				throw new DataReaderException(
						"Was unable to retrive records from database. See cause for details.",
						e);
			}

		}
	}
    //crw: make this method private
    //crw: use List instead of Array
	public void prepareData(String sqlQuery, Object[] paramValues)
			throws ConfigInitException, SQLException {
        //crw: don't use assert, assert is for unit testing purposes only
        //for production code, throw exceptions, log errors, but don't use assert
		assert (dbConnection == null);
		assert (stmt == null);
		assert (resultSet == null);

		dbConnection = dbUtil.getConnection();

		stmt = dbConnection.prepareStatement(sqlQuery);
        //crw: remove this if and don't pass null anymore
		if (paramValues != null) {
			for (int i = 0; i < paramValues.length; i++) {
                //crw: what happens when the paramValue is not one of the types you described below?
				if (paramValues[i] == null) {
					stmt.setString(i + 1, null);
				} else if (paramValues[i] instanceof Integer) {
					stmt.setInt(i + 1, (Integer) paramValues[i]);
				} else if (paramValues[i] instanceof String) {
					stmt.setString(i + 1, (String) paramValues[i]);
				} else if (paramValues[i] instanceof BinaryStream) {
					BinaryStream bs = (BinaryStream) paramValues[i];
					stmt.setBinaryStream(i + 1, bs.getInput(), bs.getLength());
				}
			}
		}
		resultSet = stmt.executeQuery();

		ResultSetMetaData meta = resultSet.getMetaData();
		int count = meta.getColumnCount();
		columnNames = new ArrayList<String>();
        //crw: logger.info
		logger.debug("count = " + count);
        //crw: meta.getColumnName counts your columns starting with 1, so start your for from 1 too, read the JavaDoc
		for (int i = 0; i < count;) {
			columnNames.add(meta.getColumnName(++i));
		}

	}

	private String generateWhereCondition() {
        //crw: what is the purpose of this initialization for the StringBuilder? It is useless.
		StringBuilder sb = new StringBuilder(" 1 = 1 ");

		Map<String, String> wcMap = configFormData.getWhereConditionMap();

		for (String key : wcMap.keySet()) {
			String valueStr = wcMap.get(key);
			if (null != valueStr && !"".equals(valueStr.trim())) {
				sb.append(" and (");
				String[] userIds = valueStr.split(",");

				for (String uid : userIds) {
					sb.append(key + " ='" + uid.trim() + "' or ");
				}
                //crw: userIds is an Array, so you can take it's size and iterate before the last element and add
                //the last element without the "' or " so you won't need to set the length of the StringBuilder to
                //sb.length()-4
				sb.setLength(sb.length() - 4);
				sb.append(" ) ");
			}
		}
        //crw: return sb.toString() directly, don't use extra variable, it's useless
		String where = sb.toString();
		return where;
	}

	@Override
	public String getValueForMapping(NormalMapping mapping)
			throws DataReaderException {
		return getStringColumnValue(mapping.getReaderField());
	}

    //crw: why is this method here? It doesn't do anything related with this class
    //The rule is, if you can take a public function form a class and copy-paste it to another class and it will
    //work flawless then it means it doesn't belong there in the first place (except when it's an utility class, which
    //this one isn't)
	@Override
	public String getValueForMapping(PreSufMapping mapping)
			throws DataReaderException {
        //crw: logger.info
		logger.debug(mapping);
		String prefix = mapping.getPrefix() == null ? "" : mapping.getPrefix();
		String valueForColumn = getStringColumnValue(mapping.getReaderField());
		String suffix = mapping.getSuffix() == null ? "" : mapping.getSuffix();
		return prefix + (valueForColumn == null ? "" : valueForColumn) + suffix;

	}

	@Override
	public String getValueForMapping(ConstructedMapping mapping)
			throws DataReaderException {
        //crw: refactor different cases into different functions so that you can read the code more easily
		String value = null;
		if (mapping.getReaderField() != null
				&& mapping.getReaderField().trim().length() > 0) {
			if (mapping.isMultipleFieldConcat()) {
				String[] values = mapping.getReaderField().split("\\|");
                //crw: you trim the value that you take from values from index 1 onwards, why not for 0?
				String columnName = values[0];

				value = (String) currentRecord.get(columnName).getColumnValue();
				for (int i = 1; i < values.length; i++) {
					columnName = values[i].trim();
					value = value.concat(", ").concat(
							(String) currentRecord.get(columnName)
									.getColumnValue());
				}

				return value;
			}

			if (mapping.isFormatField()) {
				String readerField = mapping.getReaderField();

				String columnName = readerField.substring(0,
						readerField.indexOf("["));
				String format = readerField.substring(
						readerField.indexOf("[") + 1, readerField.length() - 1);
				DecimalFormat f = new DecimalFormat(format);
				value = (String) currentRecord.get(columnName.trim())
						.getColumnValue();

                //crw: why do you convert a String to a Double and then format the Double to a String? It makes no sense.
				return StringUtils.isBlank(value) ? "" : f.format(Double
						.valueOf(value));
			}

			value = (String) currentRecord.get(mapping.getReaderField().trim())
					.getColumnValue();

		}
		return valueConstructor.getValueForField(mapping.getWriterField(),
				value, configFormData.getNumWordCapital());
	}

	@Override
	public File getValueForMapping(ImageMapping mapping)
			throws DataReaderException {
		String imageName = mapping.getReaderField();
		String imagePath = pcdJobConf.getImageFolder() + File.separator
				+ imageName;
        //crw: logger.info
		logger.debug("db: image file name is : " + imageName);
		try {
			return PropertyUtils.getFile(pcdJobConf, imagePath);
		} catch (ConfigInitException e) {
			throw new DataReaderException("Unable to get image at path:"
					+ imagePath, e);
		}
	}

	@Override
	public String getValueForPk() throws DataReaderException {

		return getStringColumnValue(pkInReader);

	}

	private String getStringColumnValue(String columnName)
			throws DataReaderException {

		Object result = currentRecord.get(columnName.trim()).getColumnValue();
        //crw: try returning empty string instead of null
		return result == null ? null : result.toString();
	}

	@Override
	public boolean hasNext() throws DataReaderException {
		try {
			if (resultSet.next()) {
				currentRecord = new HashMap<String, ColumnData>();
				int idx = 1;
				for (String column : columnNames) {
					ColumnData cv = new ColumnData(idx,
							resultSet.getObject(column));
					currentRecord.put(column, cv);
					idx++;
				}
				return true;
			} else {
				dbConnection.close();
				stmt.close();
				resultSet.close();
				return false;
			}
		} catch (SQLException e) {
            //crw: fix the typo in exception message
			throw new DataReaderException("Unabel to move to next record.", e);
		}
	}

	@Override
	public String getValueForMapping(ColIndexMapping mapping)
			throws DataReaderException {
		return String.valueOf(currentRecord.get(mapping.getReaderField())
				.getColumnIndex());
	}

    //crw: why is there a main method in this class?!?
    //crw2: and what is with it's useless content?
    //crw3: delete it!
	public static void main(String[] args) {
		StringBuilder sb = new StringBuilder("12345 and ");
		sb.setLength(sb.length() - 5);
		logger.info(sb.toString());
	}

    /*
        crw: this inner class is useless, it adds no value whatsoever, and you use it in only one function
        you must get rid of it.
     */
	private class ColumnData {
		private int columnIndex;

		private Object columnValue;

		ColumnData(int columnIndex, Object columnValue) {
			this.columnIndex = columnIndex;
			this.columnValue = columnValue;
		}

		int getColumnIndex() {
			return columnIndex;
		}

		Object getColumnValue() {
			return columnValue;
		}

	}

}
