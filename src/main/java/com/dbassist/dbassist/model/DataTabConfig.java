package com.dbassist.dbassist.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a data tab configuration
 */
public class DataTabConfig {
    private String tabId;
    private String connectionName;
    private String tableName;
    private Map<String, String> columnFilters; // column name -> filter value
    private int maxRows = 1000;

    public DataTabConfig() {
        this.columnFilters = new HashMap<>();
    }

    public DataTabConfig(String connectionName, String tableName) {
        this.connectionName = connectionName;
        this.tableName = tableName;
        this.tabId = generateTabId(connectionName, tableName);
        this.columnFilters = new HashMap<>();
    }

    private String generateTabId(String connectionName, String tableName) {
        return connectionName + "_" + tableName + "_" + System.currentTimeMillis();
    }

    // Getters and Setters
    public String getTabId() {
        return tabId;
    }

    public void setTabId(String tabId) {
        this.tabId = tabId;
    }

    public String getConnectionName() {
        return connectionName;
    }

    public void setConnectionName(String connectionName) {
        this.connectionName = connectionName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Map<String, String> getColumnFilters() {
        return columnFilters;
    }

    public void setColumnFilters(Map<String, String> columnFilters) {
        this.columnFilters = columnFilters;
    }

    public void addFilter(String columnName, String filterValue) {
        this.columnFilters.put(columnName, filterValue);
    }

    public void removeFilter(String columnName) {
        this.columnFilters.remove(columnName);
    }

    public void clearFilters() {
        this.columnFilters.clear();
    }

    public int getMaxRows() {
        return maxRows;
    }

    public void setMaxRows(int maxRows) {
        this.maxRows = maxRows;
    }

    public String getDisplayName() {
        return tableName;
    }
}

