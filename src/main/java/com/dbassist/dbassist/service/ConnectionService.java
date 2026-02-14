package com.dbassist.dbassist.service;

import com.dbassist.dbassist.model.DatabaseConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionService {

    /**
     * Test database connection
     */
    public static boolean testConnection(DatabaseConnection dbConnection) {
        Connection conn = null;
        try {
            // Set connection timeout
            DriverManager.setLoginTimeout(10); // 10 seconds timeout

            conn = createConnection(dbConnection);

            if (conn != null && !conn.isClosed()) {
                // Test with a simple query
                return conn.isValid(5); // 5 seconds timeout for validation
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Connection test failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error closing test connection: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Create database connection
     */
    public static Connection createConnection(DatabaseConnection dbConnection) throws SQLException {
        String jdbcUrl = buildJdbcUrl(dbConnection);

        try {
            // Load appropriate JDBC driver
            loadDriver(dbConnection.getDatabaseType());

            return DriverManager.getConnection(
                jdbcUrl,
                dbConnection.getUsername(),
                dbConnection.getPassword()
            );
        } catch (ClassNotFoundException e) {
            throw new SQLException("JDBC Driver not found: " + e.getMessage());
        }
    }

    /**
     * Build JDBC URL based on database type
     */
    private static String buildJdbcUrl(DatabaseConnection dbConnection) {
        String host = dbConnection.getHost();
        String port = dbConnection.getPort();
        String dbName = dbConnection.getDatabaseName();

        switch (dbConnection.getDatabaseType()) {
            case "SQL Server":
                // SQL Server JDBC URL format
                if (dbName != null && !dbName.trim().isEmpty()) {
                    return String.format("jdbc:sqlserver://%s:%s;databaseName=%s;encrypt=true;trustServerCertificate=true",
                                       host, port, dbName);
                } else {
                    return String.format("jdbc:sqlserver://%s:%s;encrypt=true;trustServerCertificate=true",
                                       host, port);
                }

            case "Oracle":
                // Oracle JDBC URL format (SID)
                if (dbName != null && !dbName.trim().isEmpty()) {
                    return String.format("jdbc:oracle:thin:@%s:%s:%s", host, port, dbName);
                } else {
                    return String.format("jdbc:oracle:thin:@%s:%s", host, port);
                }

            default:
                throw new IllegalArgumentException("Unsupported database type: " + dbConnection.getDatabaseType());
        }
    }

    /**
     * Load JDBC driver class
     */
    private static void loadDriver(String databaseType) throws ClassNotFoundException {
        switch (databaseType) {
            case "SQL Server":
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                break;
            case "Oracle":
                Class.forName("oracle.jdbc.driver.OracleDriver");
                break;
            default:
                throw new ClassNotFoundException("No driver for database type: " + databaseType);
        }
    }

    /**
     * Get connection string for display
     */
    public static String getConnectionString(DatabaseConnection dbConnection) {
        return buildJdbcUrl(dbConnection);
    }
}

