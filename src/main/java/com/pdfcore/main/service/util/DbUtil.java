package com.pdfcore.main.service.util;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.apache.log4j.Logger;

import com.pdfcore.main.constants.Constants;
import com.pdfcore.main.exceptions.ConfigInitException;
import com.pdfcore.main.model.BinaryStream;
import com.pdfcore.main.processor.impl.PcdJobConf;

public class DbUtil {

	private static DbUtil instance;
	private BasicDataSource dataSource = null;
    //crw: if the log variable is Logger.getLogger(DBUtil.class) then why isn't it private?
    //also it is a best practice to make it final.
	static Logger log = Logger.getLogger(DbUtil.class);

    //crw: I don't see these properties used somewhere else, so we should make them private.
    //If they are intended to be used from somewhere else too, then we should move them in the Constants class
	public static final String DB_URL = "db.url";
	public static final String DB_USER = "db.username";
	public static final String DB_PASS = "db.password";

    //crw: this variable should be moved in the constructor, because that's the only place where it is used.
	private Properties dbProperties = null;

	private DbUtil(PcdJobConf conf) throws Exception {
        //crw: log.warn is used for warning messages, the fact that we attempt to connect to the database it's not
        //an warning, it's an information, so it should be log.info
		log.warn("Trying to get DB connection");
		String dbConfFile = conf.getConfPath() + File.separator
				+ Constants.CONF_FOLDER + File.separator + Constants.CONF_FILE;
		dbProperties = PropertyUtils.getProperties(dbConfFile);

		validateProperties(dbProperties);

		initConnection();
	}

    //crw: we should take all the property values from a configuration file, not just url, password and username
    //It is not ok to have hardcoded values anywhere in the application. I'm thinking that you don't want to
    //recompile everything because you want to change the maxWait time from 1000 to 1500, let's say.
	private void initConnection() throws Exception {
		Properties p = new Properties();
		p.setProperty("driverClassName", "com.mysql.jdbc.Driver");
		p.setProperty("url", dbProperties.getProperty(DB_URL));
		p.setProperty("password", dbProperties.getProperty(DB_PASS));
		p.setProperty("username", dbProperties.getProperty(DB_USER));
		p.setProperty("maxActive", "30");
		p.setProperty("maxIdle", "10");
		p.setProperty("maxWait", "1000");
		p.setProperty("removeAbandoned", "false");
		p.setProperty("removeAbandonedTimeout", "120");
		p.setProperty("testOnBorrow", "true");
		p.setProperty("logAbandoned", "true");

        //crw: I know that this function throws Exception, but we should catch it here and throw another custom
        //exception, something like CannotCreateDatabaseConnectionException.
        //It is usually not ok to throw Exception, because it will force the function that catches it to make some code
        //that will be executed for absolutely every exception that can cross your mind and it's not treated in a
        // previous catch block
		dataSource = (BasicDataSource) BasicDataSourceFactory
				.createDataSource(p);

	}

    /*
        If you want to make a singleton class you should always consider making it thread safe.
        So, is this method thread safe? No. Let me tell you why.
        Consider the following scenario:
        1)thread A and thread B call this method roughly at the same time
        2)thread A checks if instance==null, it is, it moves on
        3)thread B checks if instance==null, it is (why?) because thread A didn't had the time to make
          a DBUtil object fast enough so that it could initialize the instance variable and stop thread B
        4)thread B overrides the instance variable. (they may very well compete for writing in the instance variable)
        5)...
     */
	public static DbUtil getInstance(PcdJobConf conf)
			throws ConfigInitException {
		if (instance == null) {
			try {
				instance = new DbUtil(conf);
			} catch (Exception e) {
                //crw: Please use the Logger instance to log exceptions.
				e.printStackTrace();
			}
		}
		return instance;
	}

    //TODO: [tudor]I don't like this, but I don't have a better solution right now, I have to think about it.
	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

    //crw: we should start returning non-database objects, because it's not ok to handle database objects,
    //like ResultSet all over the application.
	public ResultSet executeQuery(String sqlQuery, Object[] paramValues)
			throws ConfigInitException, SQLException {
		Connection connection = dataSource.getConnection();
		try {
			PreparedStatement ps = connection.prepareStatement(sqlQuery);
            //crw: it would be nice if you move the prepared statement construction below in a different function,
            // because all that if-else structure clutters the intent of this function.
            // Also the code below is identical with the statement preparation for the updateQuery function,
            // so extracting it will remove the duplicate code.
			if (paramValues != null) {
				for (int i = 0; i < paramValues.length; i++) {
                    //crw: what if the paramValues[i] is not one of this three types? (!Integer, !String, !BinaryStream)
                    //for example BigDecimal
					if (paramValues[i] == null) {
						ps.setString(i + 1, null);
					} else if (paramValues[i] instanceof Integer) {
						ps.setInt(i + 1, (Integer) paramValues[i]);
					} else if (paramValues[i] instanceof String) {
						ps.setString(i + 1, (String) paramValues[i]);
					} else if (paramValues[i] instanceof BinaryStream) {
						BinaryStream bs = (BinaryStream) paramValues[i];
						ps.setBinaryStream(i + 1, bs.getInput(), bs.getLength());
					}
				}
			}
			return ps.executeQuery();
		} catch (SQLException e) {
			throw new ConfigInitException(e.getMessage(), e);
		} finally {
            //crw: in executeUpdate you also close the statement, why don't you close it here also?
			connection.close();
		}

	}

	public ResultSet executeQuery(String sqlQuery, String tablename,
			Object[] paramValues) throws ConfigInitException, SQLException {
		return executeQuery(returnSQLQuery(tablename, sqlQuery), paramValues);
	}

	public int executeUpdate(String sqlQuery, String tablename,
			Object[] paramValues) throws ConfigInitException, SQLException {
		return executeUpdate(returnSQLQuery(tablename, sqlQuery), paramValues);
	}

    //crw: you return an int, but nowhere in the application you do anything with it, worse, you don't even
    //capture this int and verify if the update was made. If you don't need the count for the rows affected
    //then you should return a boolean, true if some records where affected, false if none.
	public int executeUpdate(String sqlQuery, Object[] paramValues)
			throws ConfigInitException, SQLException {
		PreparedStatement ps = null;
		Connection connection = dataSource.getConnection();
		try {
			ps = connection.prepareStatement(sqlQuery);

			if (paramValues != null) {
				for (int i = 0; i < paramValues.length; i++) {
                    //crw: same as in executeQuery, what happens if paramValue is not one of the instances described
                    //below?
					if (paramValues[i] == null) {
						ps.setString(i + 1, "");
					} else if (paramValues[i] instanceof Integer) {
						ps.setInt(i + 1, (Integer) paramValues[i]);
					} else if (paramValues[i] instanceof String) {
						ps.setString(i + 1, (String) paramValues[i]);
					} else if (paramValues[i] instanceof BinaryStream) {
						BinaryStream bs = (BinaryStream) paramValues[i];
						ps.setBinaryStream(i + 1, bs.getInput(), bs.getLength());
					}
				}
			}
			return ps.executeUpdate();
		} catch (SQLException e) {
			throw new ConfigInitException(e.getMessage(), e);
		} finally {
			closeStatement(ps);
			connection.close();
		}
	}

	private void validateProperties(Properties connProperties)
			throws ConfigInitException {

		if (!connProperties.containsKey(DB_URL)
				|| !connProperties.containsKey(DB_USER)
				|| !connProperties.containsKey(DB_PASS)) {
			throw new ConfigInitException(
					"Properties file doesn't contain all keys required to connect to DB");
		}
        //crw: if the properties file contains all the values, so everything is ok, then it's not an warning
        // (log.warn() logs with warning level) it's at best a log.info(), but in my opinion it's a useless
        // line of log because here, in this case, if the properties file do not contain all the required keys
        //then an exception is thrown and you will have an error in the logs. So move this log to log.error() and log
        // it before throwing the exception.
		log.warn("Properties file seems to contain all values required to connect do DB");
	}

	public void finalize() {
		if (dataSource != null) {
			try {
				dataSource.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private String returnSQLQuery(String sqlTableName, String query) {
		String sqlQuery = query.replace("@tablename", sqlTableName);
        //crw: use the logger
		System.out.println("sqlQuery is : " + sqlQuery);
		return sqlQuery;
	}

	private void closeStatement(Statement stmt) {
		try {
			if (null != stmt) {
				stmt.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
