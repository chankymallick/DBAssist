package com.dbassist.dbassist.service;

import com.dbassist.dbassist.model.ComparisonResult;
import com.dbassist.dbassist.model.ComparisonResult.*;
import javafx.collections.ObservableList;

import java.util.*;

/**
 * Service for comparing table data between two sources
 */
public class DataComparisonService {

    /**
     * Compare data from two sources
     *
     * @param tableName The table name
     * @param sourceConnection Source connection name
     * @param targetConnection Target connection name
     * @param sourceData Source data rows
     * @param targetData Target data rows
     * @param visibleColumns List of visible columns to compare
     * @param primaryKeyColumns List of primary key columns for row identification
     * @return ComparisonResult with detailed comparison
     */
    public static ComparisonResult compareData(
            String tableName,
            String sourceConnection,
            String targetConnection,
            ObservableList<Map<String, Object>> sourceData,
            ObservableList<Map<String, Object>> targetData,
            List<String> visibleColumns,
            List<String> primaryKeyColumns) {

        ComparisonResult result = new ComparisonResult();
        result.setTableName(tableName);
        result.setSourceConnection(sourceConnection);
        result.setTargetConnection(targetConnection);
        result.setCommonColumns(visibleColumns);
        result.setPrimaryKeyColumns(primaryKeyColumns);

        // Build maps for quick lookup by primary key
        Map<String, Map<String, Object>> sourceMap = buildPKMap(sourceData, primaryKeyColumns);
        Map<String, Map<String, Object>> targetMap = buildPKMap(targetData, primaryKeyColumns);

        Set<String> allPKs = new HashSet<>();
        allPKs.addAll(sourceMap.keySet());
        allPKs.addAll(targetMap.keySet());

        List<ComparisonRow> comparisonRows = new ArrayList<>();
        int matchedCount = 0;
        int mismatchedCount = 0;
        int sourceOnlyCount = 0;
        int targetOnlyCount = 0;

        // Compare each row
        for (String pkKey : allPKs) {
            Map<String, Object> sourceRow = sourceMap.get(pkKey);
            Map<String, Object> targetRow = targetMap.get(pkKey);

            ComparisonRow compRow = new ComparisonRow();
            compRow.setPrimaryKeyValues(extractPKValues(sourceRow != null ? sourceRow : targetRow, primaryKeyColumns));

            if (sourceRow != null && targetRow != null) {
                // Row exists in both - compare values
                Map<String, CellComparison> cellComparisons = new HashMap<>();
                boolean allMatch = true;

                for (String column : visibleColumns) {
                    Object sourceValue = sourceRow.get(column);
                    Object targetValue = targetRow.get(column);

                    CellComparison cellComp = new CellComparison(column, sourceValue, targetValue);
                    cellComparisons.put(column, cellComp);

                    if (!cellComp.isMatched()) {
                        allMatch = false;
                    }
                }

                compRow.setCellComparisons(cellComparisons);
                compRow.setStatus(allMatch ? RowStatus.MATCHED : RowStatus.MISMATCHED);

                if (allMatch) {
                    matchedCount++;
                } else {
                    mismatchedCount++;
                }

            } else if (sourceRow != null) {
                // Row only in source
                Map<String, CellComparison> cellComparisons = new HashMap<>();
                for (String column : visibleColumns) {
                    Object sourceValue = sourceRow.get(column);
                    cellComparisons.put(column, new CellComparison(column, sourceValue, null));
                }
                compRow.setCellComparisons(cellComparisons);
                compRow.setStatus(RowStatus.SOURCE_ONLY);
                sourceOnlyCount++;

            } else {
                // Row only in target
                Map<String, CellComparison> cellComparisons = new HashMap<>();
                for (String column : visibleColumns) {
                    Object targetValue = targetRow.get(column);
                    cellComparisons.put(column, new CellComparison(column, null, targetValue));
                }
                compRow.setCellComparisons(cellComparisons);
                compRow.setStatus(RowStatus.TARGET_ONLY);
                targetOnlyCount++;
            }

            comparisonRows.add(compRow);
        }

        result.setComparisonRows(comparisonRows);

        // Set summary
        ComparisonSummary summary = new ComparisonSummary();
        summary.setTotalRows(allPKs.size());
        summary.setMatchedRows(matchedCount);
        summary.setMismatchedRows(mismatchedCount);
        summary.setSourceOnlyRows(sourceOnlyCount);
        summary.setTargetOnlyRows(targetOnlyCount);
        result.setSummary(summary);

        return result;
    }

    /**
     * Build a map of rows keyed by primary key composite
     */
    private static Map<String, Map<String, Object>> buildPKMap(
            ObservableList<Map<String, Object>> data,
            List<String> primaryKeyColumns) {

        Map<String, Map<String, Object>> map = new HashMap<>();

        for (Map<String, Object> row : data) {
            String pkKey = buildPKKey(row, primaryKeyColumns);
            map.put(pkKey, row);
        }

        return map;
    }

    /**
     * Build composite primary key string
     */
    private static String buildPKKey(Map<String, Object> row, List<String> primaryKeyColumns) {
        StringBuilder sb = new StringBuilder();
        for (String pkCol : primaryKeyColumns) {
            Object value = row.get(pkCol);
            sb.append(value != null ? value.toString() : "NULL").append("|");
        }
        return sb.toString();
    }

    /**
     * Extract primary key values from row
     */
    private static Map<String, Object> extractPKValues(Map<String, Object> row, List<String> primaryKeyColumns) {
        Map<String, Object> pkValues = new HashMap<>();
        for (String pkCol : primaryKeyColumns) {
            pkValues.put(pkCol, row.get(pkCol));
        }
        return pkValues;
    }
}

