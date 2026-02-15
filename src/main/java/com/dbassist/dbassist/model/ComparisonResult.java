package com.dbassist.dbassist.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents the result of comparing two table data sets
 */
public class ComparisonResult {

    private String tableName;
    private String sourceConnection;
    private String targetConnection;
    private List<String> commonColumns;
    private List<String> primaryKeyColumns;
    private List<ComparisonRow> comparisonRows;
    private ComparisonSummary summary;

    public ComparisonResult() {
        this.commonColumns = new ArrayList<>();
        this.primaryKeyColumns = new ArrayList<>();
        this.comparisonRows = new ArrayList<>();
        this.summary = new ComparisonSummary();
    }

    // Getters and Setters
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getSourceConnection() {
        return sourceConnection;
    }

    public void setSourceConnection(String sourceConnection) {
        this.sourceConnection = sourceConnection;
    }

    public String getTargetConnection() {
        return targetConnection;
    }

    public void setTargetConnection(String targetConnection) {
        this.targetConnection = targetConnection;
    }

    public List<String> getCommonColumns() {
        return commonColumns;
    }

    public void setCommonColumns(List<String> commonColumns) {
        this.commonColumns = commonColumns;
    }

    public List<String> getPrimaryKeyColumns() {
        return primaryKeyColumns;
    }

    public void setPrimaryKeyColumns(List<String> primaryKeyColumns) {
        this.primaryKeyColumns = primaryKeyColumns;
    }

    public List<ComparisonRow> getComparisonRows() {
        return comparisonRows;
    }

    public void setComparisonRows(List<ComparisonRow> comparisonRows) {
        this.comparisonRows = comparisonRows;
    }

    public ComparisonSummary getSummary() {
        return summary;
    }

    public void setSummary(ComparisonSummary summary) {
        this.summary = summary;
    }

    /**
     * Represents a single row comparison
     */
    public static class ComparisonRow {
        private Map<String, Object> primaryKeyValues;
        private RowStatus status;
        private Map<String, CellComparison> cellComparisons;

        public ComparisonRow() {
        }

        public Map<String, Object> getPrimaryKeyValues() {
            return primaryKeyValues;
        }

        public void setPrimaryKeyValues(Map<String, Object> primaryKeyValues) {
            this.primaryKeyValues = primaryKeyValues;
        }

        public RowStatus getStatus() {
            return status;
        }

        public void setStatus(RowStatus status) {
            this.status = status;
        }

        public Map<String, CellComparison> getCellComparisons() {
            return cellComparisons;
        }

        public void setCellComparisons(Map<String, CellComparison> cellComparisons) {
            this.cellComparisons = cellComparisons;
        }
    }

    /**
     * Row status enumeration
     */
    public enum RowStatus {
        MATCHED,           // Row exists in both with all values matching
        MISMATCHED,        // Row exists in both but some values differ
        SOURCE_ONLY,       // Row only exists in source
        TARGET_ONLY        // Row only exists in target
    }

    /**
     * Represents comparison of a single cell
     */
    public static class CellComparison {
        private String columnName;
        private Object sourceValue;
        private Object targetValue;
        private boolean matched;

        public CellComparison(String columnName, Object sourceValue, Object targetValue) {
            this.columnName = columnName;
            this.sourceValue = sourceValue;
            this.targetValue = targetValue;
            this.matched = areValuesEqual(sourceValue, targetValue);
        }

        private boolean areValuesEqual(Object v1, Object v2) {
            if (v1 == null && v2 == null) return true;
            if (v1 == null || v2 == null) return false;
            return v1.toString().equals(v2.toString());
        }

        public String getColumnName() {
            return columnName;
        }

        public Object getSourceValue() {
            return sourceValue;
        }

        public Object getTargetValue() {
            return targetValue;
        }

        public boolean isMatched() {
            return matched;
        }
    }

    /**
     * Summary statistics for comparison
     */
    public static class ComparisonSummary {
        private int totalRows;
        private int matchedRows;
        private int mismatchedRows;
        private int sourceOnlyRows;
        private int targetOnlyRows;

        public int getTotalRows() {
            return totalRows;
        }

        public void setTotalRows(int totalRows) {
            this.totalRows = totalRows;
        }

        public int getMatchedRows() {
            return matchedRows;
        }

        public void setMatchedRows(int matchedRows) {
            this.matchedRows = matchedRows;
        }

        public int getMismatchedRows() {
            return mismatchedRows;
        }

        public void setMismatchedRows(int mismatchedRows) {
            this.mismatchedRows = mismatchedRows;
        }

        public int getSourceOnlyRows() {
            return sourceOnlyRows;
        }

        public void setSourceOnlyRows(int sourceOnlyRows) {
            this.sourceOnlyRows = sourceOnlyRows;
        }

        public int getTargetOnlyRows() {
            return targetOnlyRows;
        }

        public void setTargetOnlyRows(int targetOnlyRows) {
            this.targetOnlyRows = targetOnlyRows;
        }
    }
}

