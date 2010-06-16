package com.gooddata.connector.driver;

import com.gooddata.exception.ModelException;
import com.gooddata.integration.model.Column;
import com.gooddata.integration.model.DLIPart;
import com.gooddata.connector.model.PdmColumn;
import com.gooddata.connector.model.PdmSchema;
import com.gooddata.connector.model.PdmTable;
import com.gooddata.naming.N;
import com.gooddata.util.JdbcUtil;
import com.gooddata.util.StringUtil;
import org.apache.log4j.Logger;
import org.gooddata.connector.driver.AbstractSqlDriver;
import org.gooddata.connector.driver.SqlDriver;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * GoodData Derby SQL driver. Generates the DDL (tables and indexes), DML (transformation SQL) and other
 * SQL statements necessary for the data normalization (lookup generation)
 * @author zd <zd@gooddata.com>
 * @version 1.0
 */
public class MySqlDriver extends AbstractSqlDriver implements SqlDriver {
    //TODO: refactor
    private static Logger l = Logger.getLogger(MySqlDriver.class);

 /**
     * Default constructor
     */
    public MySqlDriver() {
        // autoincrement syntax
        SYNTAX_AUTOINCREMENT = "AUTO_INCREMENT";
        SYNTAX_CONCAT_FUNCTION_PREFIX = "CONCAT(";
        SYNTAX_CONCAT_FUNCTION_SUFFIX = ")";
        SYNTAX_CONCAT_OPERATOR = ",'" + HASH_SEPARATOR + "',";
    }
    
    /**
     * Executes the system DDL initialization
     * @param c JDBC connection
     * @throws ModelException if there is a problem with the PDM schema (e.g. multiple source or fact tables)
     * @throws SQLException in case of db problems
     */
    @Override
    public void executeSystemDdlSql(Connection c) throws ModelException, SQLException {
    	super.executeSystemDdlSql(c);
    	createFunctions(c);
    }
    
    /**
     * Executes the Derby SQL that extracts the data from a CSV file to the normalization database
     * @param c JDBC connection
     * @param schema the PDM schema
     * @throws ModelException in case when there is a problem with the PDM model
     * @throws SQLException in case of db problems
     */
    public void executeExtractSql(Connection c, PdmSchema schema, String file) throws ModelException, SQLException {
        PdmTable sourceTable = schema.getSourceTable();
        String source = sourceTable.getName();
        String cols = getNonAutoincrementColumns(sourceTable);
        JdbcUtil.executeUpdate(c,
            "LOAD DATA INFILE '" + file + "' INTO TABLE " + source.toUpperCase() + " CHARACTER SET UTF8 "+
            "COLUMNS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '\"' LINES TERMINATED BY '\\n' (" + cols + ")"
        );
    }

    /**
     * Executes the Derby SQL that unloads the data from the normalization database to a CSV
     * @param c JDBC connection
     * @param part DLI part
     * @param dir target directory
     * @param snapshotIds specific snapshots IDs that will be integrated
     * @throws SQLException in case of db problems
     */
    public void executeLoadSql(Connection c, PdmSchema schema, DLIPart part, String dir, int[] snapshotIds)
            throws ModelException, SQLException {
        String file = dir + System.getProperty("file.separator") + part.getFileName();
        String cols = getLoadColumns(part, schema);
        String whereClause = getLoadWhereClause(part, schema, snapshotIds);
        String dliTable = getTableNameFromPart(part);
        Statement s = null;
        ResultSet rs = null;
        try {
            s = c.createStatement();
            String sql = "SELECT " + cols + " INTO OUTFILE '" + file +
	            "' FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '\"' LINES TERMINATED BY '\\n' FROM " +
	            dliTable.toUpperCase() + whereClause;
            rs = JdbcUtil.executeQuery(s, sql);
        }
        finally {
            if (rs != null && !rs.isClosed())
                rs.close();
            if (s != null && !s.isClosed())
                s.close();
        }
    }
    
    protected String decorateFactColumnForLoad(String cols, Column cl, String table) {
        if (cols.length() > 0)
            cols += ",ATOD(" + table.toUpperCase() + "." +
                    StringUtil.formatShortName(cl.getName())+")";
        else
            cols +=  "ATOD(" + table.toUpperCase() + "." +
                    StringUtil.formatShortName(cl.getName())+")";
        return cols;
    }

    protected void insertFactsToFactTable(Connection c, PdmSchema schema) throws ModelException, SQLException {
        PdmTable factTable = schema.getFactTable();
        PdmTable sourceTable = schema.getSourceTable();
        String fact = factTable.getName();
        String source = sourceTable.getName();
        String factColumns = "";
        String sourceColumns = "";
        for(PdmColumn column : factTable.getFactColumns()) {
            factColumns += "," + column.getName();
            sourceColumns += "," + column.getSourceColumn();
        }

        for(PdmColumn column : factTable.getDateColumns()) {
            factColumns += "," + column.getName();
            sourceColumns += ",IFNULL(DATEDIFF(STR_TO_DATE(" + column.getSourceColumn() + ",'" +
                    StringUtil.convertJavaDateFormatToMySql(column.getFormat())+"'),'1900-01-01'),2147483646)+1";
        }
        JdbcUtil.executeUpdate(c,
            "INSERT INTO "+fact+"("+N.ID+factColumns+") SELECT "+ N.SRC_ID + sourceColumns +
            " FROM " + source + " WHERE "+N.SRC_ID+" > (SELECT MAX(lastid) FROM snapshots WHERE name='"+fact+"')"
        );
    }
    
    private void createFunctions(Connection c) throws SQLException {
    	String sql = "CREATE FUNCTION ATOD(str varchar(255)) RETURNS DECIMAL(15,4) "
			    + "RETURN CASE "
			    + "      WHEN str = '' THEN NULL "
			    + "      ELSE CAST( ";
    	for (final String s : Constants.DISCARD_CHARS) {
    		sql += "REPLACE(";
    	}
    	sql += "str";
    	for (final String s : Constants.DISCARD_CHARS) {
    		sql += ", '" + s + "', '')";
    	}
		sql +=  "           AS DECIMAL(15,4)) "
			  + "   END";

        JdbcUtil.executeUpdate(c, sql);

    }
}
