package com.dbassist.dbassist.service;

import com.dbassist.dbassist.model.DatabaseConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.*;

/**
 * Service to fetch table data with filtering
 */
public class TableDataService {

    /**
     * Fetch table data with optional filters
     */
    public static TableDataResult fetchTableData(DatabaseConnection dbConnection,
                                                  String tableName,
                                                  Map<String, String> filters,
                                                  Map<String, Boolean> exactSearchFlags,
                                                  int maxRows) {
        TableDataResult result = new TableDataResult();

        try (Connection conn = ConnectionService.createConnection(dbConnection)) {
            // Build SQL query with filters
            String sql = buildSelectQuery(tableName, filters, exactSearchFlags, maxRows);
            System.out.println("Executing: " + sql);

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            ResultSetMetaData metaData = rs.getMetaData();

            int columnCount = metaData.getColumnCount();

            // Get column names and types
            List<String> columnNames = new ArrayList<>();
            Map<String, String> columnTypes = new HashMap<>();

            for (int i = 1; i <= columnCount; i++) {
                String columnName = metaData.getColumnName(i);
                String columnType = metaData.getColumnTypeName(i);
                columnNames.add(columnName);
                columnTypes.put(columnName, columnType);
            }

            result.setColumnNames(columnNames);
            result.setColumnTypes(columnTypes);

            // Fetch rows
            ObservableList<Map<String, Object>> rows = FXCollections.observableArrayList();
            int rowCount = 0;

            while (rs.next() && rowCount < maxRows) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (String columnName : columnNames) {
                    Object value = rs.getObject(columnName);
                    row.put(columnName, value);
                }
                rows.add(row);
                rowCount++;
            }

            result.setRows(rows);
            result.setRowCount(rowCount);

            rs.close();
            stmt.close();

        } catch (SQLException e) {
            System.err.println("Error fetching table data: " + e.getMessage());
            e.printStackTrace();
            result.setError(e.getMessage());
        }

        return result;
    }

    /**
     * Build SELECT query with filters
     */
    private static String buildSelectQuery(String tableName, Map<String, String> filters, int maxRows) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT TOP ").append(maxRows).append(" * FROM ").append(tableName);

        if (filters != null && !filters.isEmpty()) {
            sql.append(" WHERE ");
            List<String> conditions = new ArrayList<>();

            for (Map.Entry<String, String> filter : filters.entrySet()) {
                String column = filter.getKey();
                String value = filter.getValue();

                if (value != null && !value.trim().isEmpty()) {
                    // Use LIKE for string matching
                    conditions.add(column + " LIKE '%" + escapeSQL(value) + "%'");
                }
            }

            sql.append(String.join(" AND ", conditions));
        }

        return sql.toString();
    }

    /**
     * Escape SQL special characters
     */
    private static String escapeSQL(String input) {
        if (input == null) return "";
        return input.replace("'", "''");
    }

    /**
     * Result class for table data
     */
    public static class TableDataResult {
        private List<String> columnNames;
        private Map<String, String> columnTypes;
        private ObservableList<Map<String, Object>> rows;
        private int rowCount;
        private String error;

        public List<String> getColumnNames() {
            return columnNames;
        }

        public void setColumnNames(List<String> columnNames) {
            this.columnNames = columnNames;
        }

        public Map<String, String> getColumnTypes() {
            return columnTypes;
        }

        public void setColumnTypes(Map<String, String> columnTypes) {
            this.columnTypes = columnTypes;
        }

        public ObservableList<Map<String, Object>> getRows() {
            return rows;
        }

        public void setRows(ObservableList<Map<String, Object>> rows) {
            this.rows = rows;
        }

        public int getRowCount() {
            return rowCount;
        }

        public void setRowCount(int rowCount) {
            this.rowCount = rowCount;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public boolean hasError() {
            return error != null && !error.isEmpty();
        }
    }
}

