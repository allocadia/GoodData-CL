package org.gooddata.connector.backend;

import com.gooddata.connector.model.PdmSchema;
import com.gooddata.exception.InternalErrorException;
import com.gooddata.exception.ModelException;
import com.gooddata.integration.model.DLI;
import com.gooddata.integration.model.DLIPart;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * GoodData connector backend interface.
 * Connector backend handles communication with the specific SQL database. Specifically it handles the DB connection
 * and other communication specifics of the DBMS. It uses the SQL driver that generates appropriate SQL dialect.
 *
 * @author zd <zd@gooddata.com>
 * @version 1.0
*/
public interface ConnectorBackend {


    // Supported connector backends
    public static final int CONNECTOR_BACKEND_DERBY_SQL = 1;
    public static final int CONNECTOR_BACKEND_MYSQL = 2;
    
    /**
     * Connects the database
     * @return JDBC connection
     * @throws java.sql.SQLException
     */
    public Connection connect() throws SQLException;

    /**
     * Perform the data normalization (generate lookups). The database must contain the required schema
     */
    public void transform();

    /**
     * Drops all current snapshots. This is usually achieved by dropping the whole project database.
     */
    public void dropSnapshots();

    /**
     * Lists the current snapshots
     * @return list of snapshots as String
     */
    public String listSnapshots();

    /**
     * Get last snapshot number. Snapshot is each individual lad of data. Snapshots are numbered (0...N).
     * Sometimes when you call this method at the beginning of a process that creates new snapshot, you might want to
     * add one to the snapshot number.
     * @return last snapshot number
     */
    public int getLastSnapshotId();

    /**
     * Initializes the database schema that is going to be used for the data normalization
     */
    public void initialize();
    
    /**
     * Figures out if the connector is initialized
     * @return the initialization status
     */
    public boolean isInitialized();

    /**
     * Load the all normalized data from the SQL to the GoodData data package on the disk
     * @param parts the Data Loading Interface parts
     * @param dir target directory where the data package will be stored
     */
    public void load(List<DLIPart> parts, String dir);

    /**
     * Load the normalized data of selected snapshots from the SQL to the GoodData data package on the disk.
     * @param parts the Data Loading Interface parts
     * @param dir target directory where the data package will be stored
     * @param snapshotIds snapshot ids that are going to be loaded (if NULL, all snapshots are going to be loaded)
     */
    public void loadSnapshot(List<DLIPart> parts, String dir, int[] snapshotIds);


    /**
     * Create the GoodData data package with the ALL snapshots data
     * @param dli the Data Loading Interface that contains the required data structures
     * @param parts the Data Loading Interface parts
     * @param dir target directory where the data package will be stored
     * @param archiveName the name of the target ZIP archive
     * @throws IOException IO issues
     */
    public void deploy(DLI dli, List<DLIPart> parts, String dir, String archiveName) throws IOException;

    /**
     * Create the GoodData data package with the data from specified snapshots
     * @param dli the Data Loading Interface that contains the required data structures
     * @param parts the Data Loading Interface parts
     * @param dir target directory where the data package will be stored
     * @param archiveName the name of the target ZIP archive
     * @param snapshotIds snapshot ids that are going to be loaded (if NULL, all snapshots are going to be loaded)
     * @throws IOException IO issues
     */
    public void deploySnapshot(DLI dli, List<DLIPart> parts, String dir, String archiveName, int[] snapshotIds) throws IOException;

    /**
     * Extracts the source data CSV to the database where it is going to be transformed
     * @param dataFile the data file to extract
     */
    public void extract(File dataFile);

    /**
     * Returns true if the specified table exists in the DB
     * @param tbl table name
     * @return true if the table exists, false otherwise
     */
    public boolean exists(String tbl);

    /**
     * Project id getter
     * @return project id
     */
    public String getProjectId();

    /**
     * Project id setter
     * @param projectId project id
     */
    public void setProjectId(String projectId);

    /**
     * PDM schema getter
     * @return pdm schema
     */
    public PdmSchema getPdm();

    /**
     * PDM schema setter
     * @param schema PDM schema
     */
    public void setPdm(PdmSchema schema);

}