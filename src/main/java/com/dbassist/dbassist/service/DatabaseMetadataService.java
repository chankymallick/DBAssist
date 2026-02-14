package com.dbassist.dbassist.service;

import com.dbassist.dbassist.model.DatabaseConnection;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service to fetch database metadata (tables, views, procedures, etc.)
 */
public class DatabaseMetadataService {

    /**
     * Get list of all tables in the database
     */
    public static List<String> getTables(DatabaseConnection dbConnection) {
        List<String> tables = new ArrayList<>();

        try (Connection conn = ConnectionService.createConnection(dbConnection)) {
            DatabaseMetaData metaData = conn.getMetaData();

            // Get tables for the specific database
            String catalog = dbConnection.getDatabaseName();
            String schemaPattern = null;

            // For SQL Server, use schema
            if ("SQL Server".equals(dbConnection.getDatabaseType())) {
                schemaPattern = "dbo"; // Default schema
            }
            // For Oracle, use username as schema
            else if ("Oracle".equals(dbConnection.getDatabaseType())) {
                schemaPattern = dbConnection.getUsername().toUpperCase();
            }

            ResultSet rs = metaData.getTables(catalog, schemaPattern, "%", new String[]{"TABLE"});

            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                tables.add(tableName);
            }

            rs.close();

        } catch (SQLException e) {
            System.err.println("Error fetching tables: " + e.getMessage());
            e.printStackTrace();
        }

        return tables;
    }

    /**
     * Get list of all views in the database
     */
    public static List<String> getViews(DatabaseConnection dbConnection) {
        List<String> views = new ArrayList<>();

        try (Connection conn = ConnectionService.createConnection(dbConnection)) {
            DatabaseMetaData metaData = conn.getMetaData();

            String catalog = dbConnection.getDatabaseName();
            String schemaPattern = null;

            if ("SQL Server".equals(dbConnection.getDatabaseType())) {
                schemaPattern = "dbo";
            } else if ("Oracle".equals(dbConnection.getDatabaseType())) {
                schemaPattern = dbConnection.getUsername().toUpperCase();
            }

            ResultSet rs = metaData.getTables(catalog, schemaPattern, "%", new String[]{"VIEW"});

            while (rs.next()) {
                String viewName = rs.getString("TABLE_NAME");
                views.add(viewName);
            }

            rs.close();

        } catch (SQLException e) {
            System.err.println("Error fetching views: " + e.getMessage());
        }

        return views;
    }

    /**
     * Get list of all stored procedures in the database
     */
    public static List<String> getProcedures(DatabaseConnection dbConnection) {
        List<String> procedures = new ArrayList<>();

        try (Connection conn = ConnectionService.createConnection(dbConnection)) {
            DatabaseMetaData metaData = conn.getMetaData();

            String catalog = dbConnection.getDatabaseName();
            String schemaPattern = null;

            if ("SQL Server".equals(dbConnection.getDatabaseType())) {
                schemaPattern = "dbo";
            } else if ("Oracle".equals(dbConnection.getDatabaseType())) {
                schemaPattern = dbConnection.getUsername().toUpperCase();
            }

            ResultSet rs = metaData.getProcedures(catalog, schemaPattern, "%");

            while (rs.next()) {
                String procName = rs.getString("PROCEDURE_NAME");
                procedures.add(procName);
            }

            rs.close();

        } catch (SQLException e) {
            System.err.println("Error fetching procedures: " + e.getMessage());
        }

        return procedures;
    }

    /**
     * Get list of all functions in the database
     */
    public static List<String> getFunctions(DatabaseConnection dbConnection) {
        List<String> functions = new ArrayList<>();

        try (Connection conn = ConnectionService.createConnection(dbConnection)) {
            DatabaseMetaData metaData = conn.getMetaData();

            String catalog = dbConnection.getDatabaseName();
            String schemaPattern = null;

            if ("SQL Server".equals(dbConnection.getDatabaseType())) {
                schemaPattern = "dbo";
            } else if ("Oracle".equals(dbConnection.getDatabaseType())) {
                schemaPattern = dbConnection.getUsername().toUpperCase();
            }

            ResultSet rs = metaData.getFunctions(catalog, schemaPattern, "%");

            while (rs.next()) {
                String funcName = rs.getString("FUNCTION_NAME");
                functions.add(funcName);
            }

            rs.close();

        } catch (SQLException e) {
            System.err.println("Error fetching functions: " + e.getMessage());
        }

        return functions;
    }

    /**
     * Get count of tables
     */
    public static int getTableCount(DatabaseConnection dbConnection) {
        return getTables(dbConnection).size();
    }

    /**
     * Get count of views
     */
    public static int getViewCount(DatabaseConnection dbConnection) {
        return getViews(dbConnection).size();
    }

    /**
     * Get columns for a specific table
     * Returns list of formatted strings: "columnName (dataType, length)"
     */
    public static List<String> getTableColumns(DatabaseConnection dbConnection, String tableName) {
        List<String> columns = new ArrayList<>();

        try (Connection conn = ConnectionService.createConnection(dbConnection)) {
            DatabaseMetaData metaData = conn.getMetaData();

            String catalog = dbConnection.getDatabaseName();
            String schemaPattern = null;

            if ("SQL Server".equals(dbConnection.getDatabaseType())) {
                schemaPattern = "dbo";
            } else if ("Oracle".equals(dbConnection.getDatabaseType())) {
                schemaPattern = dbConnection.getUsername().toUpperCase();
            }

            ResultSet rs = metaData.getColumns(catalog, schemaPattern, tableName, "%");

            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME");
                String dataType = rs.getString("TYPE_NAME");
                int columnSize = rs.getInt("COLUMN_SIZE");
                int decimalDigits = rs.getInt("DECIMAL_DIGITS");
                String isNullable = rs.getString("IS_NULLABLE");

                // Format: ColumnName (DataType(Size), Nullable)
                StringBuilder columnInfo = new StringBuilder();
                columnInfo.append(columnName).append(" (");
                columnInfo.append(dataType);

                // Add size/precision for applicable types
                if (needsSize(dataType)) {
                    columnInfo.append("(");
                    if (decimalDigits > 0) {
                        columnInfo.append(columnSize).append(",").append(decimalDigits);
                    } else {
                        columnInfo.append(columnSize);
                    }
                    columnInfo.append(")");
                }

                // Add nullable indicator
                if ("YES".equals(isNullable)) {
                    columnInfo.append(", NULL");
                } else {
                    columnInfo.append(", NOT NULL");
                }

                columnInfo.append(")");

                columns.add(columnInfo.toString());
            }

            rs.close();

        } catch (SQLException e) {
            System.err.println("Error fetching columns for table " + tableName + ": " + e.getMessage());
        }

        return columns;
    }

    /**
     * Check if data type needs size specification
     */
    private static boolean needsSize(String dataType) {
        if (dataType == null) return false;

        String type = dataType.toUpperCase();
        return type.contains("CHAR") || type.contains("VARCHAR") ||
               type.contains("BINARY") || type.contains("VARBINARY") ||
               type.contains("DECIMAL") || type.contains("NUMERIC");
    }
}

