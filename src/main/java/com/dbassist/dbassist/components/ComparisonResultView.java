package com.dbassist.dbassist.components;

import com.dbassist.dbassist.model.ComparisonResult;
import com.dbassist.dbassist.model.ComparisonResult.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.Map;

/**
 * Component to display comparison results in a colored grid
 */
public class ComparisonResultView extends VBox {

    private ComparisonResult result;
    private TableView<ComparisonRow> tableView;
    private Map<String, ColumnStats> columnStats; // Track statistics per column

    /**
     * Statistics for a single column
     */
    private static class ColumnStats {
        int totalComparisons = 0;
        int matches = 0;
        int mismatches = 0;

        public boolean hasAllMatches() {
            return mismatches == 0 && totalComparisons > 0;
        }

        public boolean hasSomeMismatches() {
            return mismatches > 0 && mismatches < totalComparisons;
        }

        public boolean hasAllMismatches() {
            return totalComparisons > 0 && matches == 0;
        }

        public int getMatchPercentage() {
            if (totalComparisons == 0) return 0;
            return (matches * 100) / totalComparisons;
        }
    }

    public ComparisonResultView(ComparisonResult result) {
        this.result = result;
        this.columnStats = new java.util.HashMap<>();
        calculateColumnStats();
        initialize();
    }

    private void initialize() {
        this.setSpacing(10);
        this.setPadding(new Insets(15));
        this.setStyle("-fx-background-color: #f5f7fa;");

        // Header with summary and export buttons
        HBox header = createHeader();
        this.getChildren().add(header);

        // Summary cards
        HBox summaryCards = createSummaryCards();
        this.getChildren().add(summaryCards);

        // TableView for comparison data
        tableView = new TableView<>();
        tableView.setStyle("-fx-background-color: white;");
        tableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tableView, Priority.ALWAYS);

        createColumns();

        tableView.getItems().addAll(result.getComparisonRows());

        this.getChildren().add(tableView);
    }

    /**
     * Calculate statistics for each column
     */
    private void calculateColumnStats() {
        // Initialize stats for all columns
        for (String column : result.getCommonColumns()) {
            if (!result.getPrimaryKeyColumns().contains(column)) {
                columnStats.put(column, new ColumnStats());
            }
        }

        // Analyze each row
        for (ComparisonRow row : result.getComparisonRows()) {
            // Only count rows that exist in both sources for column comparison
            if (row.getStatus() == RowStatus.MATCHED || row.getStatus() == RowStatus.MISMATCHED) {
                Map<String, CellComparison> cellComparisons = row.getCellComparisons();

                for (String column : columnStats.keySet()) {
                    CellComparison cellComp = cellComparisons.get(column);
                    if (cellComp != null) {
                        ColumnStats stats = columnStats.get(column);
                        stats.totalComparisons++;

                        if (cellComp.isMatched()) {
                            stats.matches++;
                        } else {
                            stats.mismatches++;
                        }
                    }
                }
            }
        }

        // Log statistics
        System.out.println("Column Statistics:");
        for (Map.Entry<String, ColumnStats> entry : columnStats.entrySet()) {
            ColumnStats stats = entry.getValue();
            System.out.println("  " + entry.getKey() + ": " +
                stats.matches + "/" + stats.totalComparisons + " matches (" +
                stats.getMatchPercentage() + "%)");
        }
    }

    private HBox createHeader() {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10));
        header.setStyle("-fx-background-color: white; -fx-background-radius: 5;");

        Label titleLabel = new Label("Data Comparison: " + result.getTableName());
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox connectionInfo = new VBox(3);
        Label sourceLabel = new Label("Source: " + result.getSourceConnection());
        sourceLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #546e7a; -fx-font-weight: 600;");

        Label targetLabel = new Label("Target: " + result.getTargetConnection());
        targetLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #546e7a; -fx-font-weight: 600;");

        Label idColsLabel = new Label("ID Columns: " + String.join(", ", result.getPrimaryKeyColumns()));
        idColsLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d; -fx-font-style: italic;");

        connectionInfo.getChildren().addAll(sourceLabel, new Label("→"), targetLabel, idColsLabel);

        // Export buttons
        Button exportExcelBtn = new Button("📊 Export Excel");
        exportExcelBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 8 15; -fx-font-size: 11px;");
        exportExcelBtn.setOnAction(e -> exportToExcel());

        Button exportHtmlBtn = new Button("🌐 Export HTML");
        exportHtmlBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 8 15; -fx-font-size: 11px;");
        exportHtmlBtn.setOnAction(e -> exportToHtml());

        Button exportCsvBtn = new Button("📄 Export CSV");
        exportCsvBtn.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 8 15; -fx-font-size: 11px;");
        exportCsvBtn.setOnAction(e -> exportToCsv());

        header.getChildren().addAll(titleLabel, spacer, connectionInfo, exportExcelBtn, exportHtmlBtn, exportCsvBtn);

        return header;
    }

    private HBox createSummaryCards() {
        HBox summaryBox = new HBox(15);
        summaryBox.setPadding(new Insets(10, 0, 10, 0));

        ComparisonSummary summary = result.getSummary();

        summaryBox.getChildren().addAll(
            createSummaryCard("Total Rows", String.valueOf(summary.getTotalRows()), "#607d8b"),
            createSummaryCard("✓ Matched", String.valueOf(summary.getMatchedRows()), "#27ae60"),
            createSummaryCard("⚠ Mismatched", String.valueOf(summary.getMismatchedRows()), "#e67e22"),
            createSummaryCard("◄ Source Only", String.valueOf(summary.getSourceOnlyRows()), "#9b59b6"),
            createSummaryCard("► Target Only", String.valueOf(summary.getTargetOnlyRows()), "#3498db")
        );

        return summaryBox;
    }

    private VBox createSummaryCard(String label, String value, String color) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-border-color: " +
            color + "; -fx-border-width: 2; -fx-border-radius: 5;");
        card.setPrefWidth(150);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        Label labelText = new Label(label);
        labelText.setStyle("-fx-font-size: 11px; -fx-text-fill: #546e7a;");

        card.getChildren().addAll(valueLabel, labelText);

        return card;
    }

    private void createColumns() {
        // Status column
        TableColumn<ComparisonRow, String> statusCol = new TableColumn<>("Status");
        statusCol.setPrefWidth(120);
        statusCol.setCellValueFactory(cellData -> {
            RowStatus status = cellData.getValue().getStatus();
            return new javafx.beans.property.SimpleStringProperty(getStatusText(status));
        });
        statusCol.setCellFactory(col -> new TableCell<ComparisonRow, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    ComparisonRow row = getTableView().getItems().get(getIndex());
                    setStyle(getStatusStyle(row.getStatus()));
                }
            }
        });
        tableView.getColumns().add(statusCol);

        // Primary key columns
        for (String pkCol : result.getPrimaryKeyColumns()) {
            TableColumn<ComparisonRow, String> col = new TableColumn<>(pkCol + " (PK)");
            col.setPrefWidth(120);
            col.setCellValueFactory(cellData -> {
                Object value = cellData.getValue().getPrimaryKeyValues().get(pkCol);
                return new javafx.beans.property.SimpleStringProperty(value != null ? value.toString() : "null");
            });
            col.setCellFactory(this::createPKCellFactory);
            tableView.getColumns().add(col);
        }

        // Data columns with comparison
        for (String column : result.getCommonColumns()) {
            if (!result.getPrimaryKeyColumns().contains(column)) {
                TableColumn<ComparisonRow, String> col = new TableColumn<>();
                col.setPrefWidth(150);

                // Create custom header with match indicator
                VBox headerBox = createColumnHeaderWithStats(column);
                col.setGraphic(headerBox);

                col.setCellFactory(c -> createComparisonCell(column));
                tableView.getColumns().add(col);
            }
        }
    }

    /**
     * Create column header with match/mismatch indicator
     */
    private VBox createColumnHeaderWithStats(String columnName) {
        VBox headerBox = new VBox(3);
        headerBox.setAlignment(Pos.CENTER);

        Label nameLabel = new Label(columnName);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        ColumnStats stats = columnStats.get(columnName);

        if (stats != null && stats.totalComparisons > 0) {
            HBox statusBox = new HBox(5);
            statusBox.setAlignment(Pos.CENTER);

            String statusIcon;
            String statusText;
            String statusStyle;

            if (stats.hasAllMatches()) {
                // All rows match - green checkmark
                statusIcon = "✓";
                statusText = "All Match";
                statusStyle = "-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 11px;";
            } else if (stats.hasAllMismatches()) {
                // All rows mismatch - red X
                statusIcon = "✗";
                statusText = "All Differ";
                statusStyle = "-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 11px;";
            } else {
                // Some match, some don't - orange warning
                statusIcon = "⚠";
                statusText = stats.getMatchPercentage() + "% Match";
                statusStyle = "-fx-text-fill: #e67e22; -fx-font-weight: bold; -fx-font-size: 11px;";
            }

            Label statusLabel = new Label(statusIcon + " " + statusText);
            statusLabel.setStyle(statusStyle);

            // Tooltip with details
            Tooltip tooltip = new Tooltip(
                String.format("%s\nMatches: %d\nMismatches: %d\nTotal: %d",
                    columnName, stats.matches, stats.mismatches, stats.totalComparisons)
            );
            Tooltip.install(statusLabel, tooltip);

            statusBox.getChildren().add(statusLabel);
            headerBox.getChildren().addAll(nameLabel, statusBox);
        } else {
            headerBox.getChildren().add(nameLabel);
        }

        return headerBox;
    }

    private TableCell<ComparisonRow, String> createPKCellFactory(TableColumn<ComparisonRow, String> col) {
        return new TableCell<ComparisonRow, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-font-weight: bold; -fx-background-color: #e8eaf6;");
                }
            }
        };
    }

    private TableCell<ComparisonRow, String> createComparisonCell(String columnName) {
        return new TableCell<ComparisonRow, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                    return;
                }

                ComparisonRow row = getTableView().getItems().get(getIndex());
                CellComparison cellComp = row.getCellComparisons().get(columnName);

                if (cellComp == null) {
                    setText("");
                    setStyle("");
                    return;
                }

                VBox cellContent = new VBox(3);
                cellContent.setAlignment(Pos.CENTER_LEFT);

                // Source value
                Label sourceLabel = new Label("S: " + formatValue(cellComp.getSourceValue()));
                sourceLabel.setStyle("-fx-font-size: 11px;");

                // Target value
                Label targetLabel = new Label("T: " + formatValue(cellComp.getTargetValue()));
                targetLabel.setStyle("-fx-font-size: 11px;");

                cellContent.getChildren().addAll(sourceLabel, targetLabel);

                setText(null);
                setGraphic(cellContent);

                // Color coding
                if (cellComp.isMatched()) {
                    setStyle("-fx-background-color: #c8e6c9; -fx-border-color: #81c784; -fx-border-width: 0.5;"); // Light green
                } else {
                    setStyle("-fx-background-color: #ffcdd2; -fx-border-color: #e57373; -fx-border-width: 0.5;"); // Light red
                }
            }
        };
    }

    private String formatValue(Object value) {
        if (value == null) return "null";
        String str = value.toString();
        return str.length() > 30 ? str.substring(0, 27) + "..." : str;
    }

    private String getStatusText(RowStatus status) {
        switch (status) {
            case MATCHED: return "✓ Matched";
            case MISMATCHED: return "⚠ Mismatched";
            case SOURCE_ONLY: return "◄ Source Only";
            case TARGET_ONLY: return "► Target Only";
            default: return "Unknown";
        }
    }

    private String getStatusStyle(RowStatus status) {
        switch (status) {
            case MATCHED:
                return "-fx-background-color: #c8e6c9; -fx-text-fill: #2e7d32; -fx-font-weight: bold;";
            case MISMATCHED:
                return "-fx-background-color: #ffe0b2; -fx-text-fill: #e65100; -fx-font-weight: bold;";
            case SOURCE_ONLY:
                return "-fx-background-color: #e1bee7; -fx-text-fill: #6a1b9a; -fx-font-weight: bold;";
            case TARGET_ONLY:
                return "-fx-background-color: #bbdefb; -fx-text-fill: #0d47a1; -fx-font-weight: bold;";
            default:
                return "";
        }
    }

    /**
     * Export comparison results to Excel format
     */
    private void exportToExcel() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Export to Excel");
        fileChooser.setInitialFileName("comparison_" + result.getTableName() + ".xlsx");
        fileChooser.getExtensionFilters().add(
            new javafx.stage.FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
        );

        java.io.File file = fileChooser.showSaveDialog(this.getScene().getWindow());
        if (file != null) {
            new Thread(() -> {
                try {
                    exportToExcelFile(file);
                    javafx.application.Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Export Successful");
                        alert.setHeaderText("Comparison exported to Excel");
                        alert.setContentText("File saved: " + file.getAbsolutePath());
                        alert.showAndWait();
                    });
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Export Failed");
                        alert.setHeaderText("Failed to export to Excel");
                        alert.setContentText(e.getMessage());
                        alert.showAndWait();
                    });
                    e.printStackTrace();
                }
            }).start();
        }
    }

    /**
     * Export comparison results to HTML format
     */
    private void exportToHtml() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Export to HTML");
        fileChooser.setInitialFileName("comparison_" + result.getTableName() + ".html");
        fileChooser.getExtensionFilters().add(
            new javafx.stage.FileChooser.ExtensionFilter("HTML Files", "*.html")
        );

        java.io.File file = fileChooser.showSaveDialog(this.getScene().getWindow());
        if (file != null) {
            new Thread(() -> {
                try {
                    exportToHtmlFile(file);
                    javafx.application.Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Export Successful");
                        alert.setHeaderText("Comparison exported to HTML");
                        alert.setContentText("File saved: " + file.getAbsolutePath());
                        alert.showAndWait();
                    });
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Export Failed");
                        alert.setHeaderText("Failed to export to HTML");
                        alert.setContentText(e.getMessage());
                        alert.showAndWait();
                    });
                    e.printStackTrace();
                }
            }).start();
        }
    }

    /**
     * Export comparison results to CSV format
     */
    private void exportToCsv() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Export to CSV");
        fileChooser.setInitialFileName("comparison_" + result.getTableName() + ".csv");
        fileChooser.getExtensionFilters().add(
            new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        java.io.File file = fileChooser.showSaveDialog(this.getScene().getWindow());
        if (file != null) {
            new Thread(() -> {
                try {
                    exportToCsvFile(file);
                    javafx.application.Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Export Successful");
                        alert.setHeaderText("Comparison exported to CSV");
                        alert.setContentText("File saved: " + file.getAbsolutePath());
                        alert.showAndWait();
                    });
                } catch (Exception e) {
                    javafx.application.Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Export Failed");
                        alert.setHeaderText("Failed to export to CSV");
                        alert.setContentText(e.getMessage());
                        alert.showAndWait();
                    });
                    e.printStackTrace();
                }
            }).start();
        }
    }

    /**
     * Write comparison data to Excel file
     */
    private void exportToExcelFile(java.io.File file) throws Exception {
        org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
        org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Comparison");

        // Create cell styles
        org.apache.poi.ss.usermodel.CellStyle headerStyle = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);

        org.apache.poi.ss.usermodel.CellStyle matchStyle = workbook.createCellStyle();
        matchStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.LIGHT_GREEN.getIndex());
        matchStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);

        org.apache.poi.ss.usermodel.CellStyle mismatchStyle = workbook.createCellStyle();
        mismatchStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.LIGHT_ORANGE.getIndex());
        mismatchStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);

        // Write summary
        int rowNum = 0;
        org.apache.poi.ss.usermodel.Row titleRow = sheet.createRow(rowNum++);
        titleRow.createCell(0).setCellValue("Data Comparison: " + result.getTableName());

        org.apache.poi.ss.usermodel.Row sourceRow = sheet.createRow(rowNum++);
        sourceRow.createCell(0).setCellValue("Source: " + result.getSourceConnection());

        org.apache.poi.ss.usermodel.Row targetRow = sheet.createRow(rowNum++);
        targetRow.createCell(0).setCellValue("Target: " + result.getTargetConnection());

        rowNum++; // Empty row

        org.apache.poi.ss.usermodel.Row summaryRow = sheet.createRow(rowNum++);
        summaryRow.createCell(0).setCellValue("Total Rows: " + result.getSummary().getTotalRows());
        summaryRow.createCell(1).setCellValue("Matched: " + result.getSummary().getMatchedRows());
        summaryRow.createCell(2).setCellValue("Mismatched: " + result.getSummary().getMismatchedRows());
        summaryRow.createCell(3).setCellValue("Source Only: " + result.getSummary().getSourceOnlyRows());
        summaryRow.createCell(4).setCellValue("Target Only: " + result.getSummary().getTargetOnlyRows());

        rowNum++; // Empty row

        // Write header
        org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(rowNum++);
        int colNum = 0;

        org.apache.poi.ss.usermodel.Cell statusCell = headerRow.createCell(colNum++);
        statusCell.setCellValue("Status");
        statusCell.setCellStyle(headerStyle);

        // PK columns
        for (String pkCol : result.getPrimaryKeyColumns()) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(colNum++);
            cell.setCellValue(pkCol + " (PK)");
            cell.setCellStyle(headerStyle);
        }

        // Data columns - Source and Target
        for (String column : result.getCommonColumns()) {
            if (!result.getPrimaryKeyColumns().contains(column)) {
                org.apache.poi.ss.usermodel.Cell sourceCell = headerRow.createCell(colNum++);
                sourceCell.setCellValue(column + " (Source)");
                sourceCell.setCellStyle(headerStyle);

                org.apache.poi.ss.usermodel.Cell targetCell = headerRow.createCell(colNum++);
                targetCell.setCellValue(column + " (Target)");
                targetCell.setCellStyle(headerStyle);

                org.apache.poi.ss.usermodel.Cell matchCell = headerRow.createCell(colNum++);
                matchCell.setCellValue(column + " (Match?)");
                matchCell.setCellStyle(headerStyle);
            }
        }

        // Write data rows
        for (ComparisonRow compRow : result.getComparisonRows()) {
            org.apache.poi.ss.usermodel.Row dataRow = sheet.createRow(rowNum++);
            colNum = 0;

            // Status
            dataRow.createCell(colNum++).setCellValue(getStatusText(compRow.getStatus()));

            // PK values
            for (String pkCol : result.getPrimaryKeyColumns()) {
                Object pkValue = compRow.getPrimaryKeyValues().get(pkCol);
                dataRow.createCell(colNum++).setCellValue(pkValue != null ? pkValue.toString() : "null");
            }

            // Data columns
            for (String column : result.getCommonColumns()) {
                if (!result.getPrimaryKeyColumns().contains(column)) {
                    CellComparison cellComp = compRow.getCellComparisons().get(column);
                    if (cellComp != null) {
                        org.apache.poi.ss.usermodel.Cell sourceCell = dataRow.createCell(colNum++);
                        sourceCell.setCellValue(cellComp.getSourceValue() != null ? cellComp.getSourceValue().toString() : "null");

                        org.apache.poi.ss.usermodel.Cell targetCell = dataRow.createCell(colNum++);
                        targetCell.setCellValue(cellComp.getTargetValue() != null ? cellComp.getTargetValue().toString() : "null");

                        org.apache.poi.ss.usermodel.Cell matchCell = dataRow.createCell(colNum++);
                        matchCell.setCellValue(cellComp.isMatched() ? "✓" : "✗");
                        matchCell.setCellStyle(cellComp.isMatched() ? matchStyle : mismatchStyle);
                    } else {
                        colNum += 3; // Skip source, target, match columns
                    }
                }
            }
        }

        // Auto-size columns
        for (int i = 0; i < colNum; i++) {
            sheet.autoSizeColumn(i);
        }

        // Write to file
        try (java.io.FileOutputStream fileOut = new java.io.FileOutputStream(file)) {
            workbook.write(fileOut);
        }
        workbook.close();
    }

    /**
     * Write comparison data to HTML file
     */
    private void exportToHtmlFile(java.io.File file) throws Exception {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html>\n<head>\n");
        html.append("<meta charset='UTF-8'>\n");
        html.append("<title>Data Comparison: ").append(result.getTableName()).append("</title>\n");
        html.append("<style>\n");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; }\n");
        html.append("h1 { color: #2c3e50; }\n");
        html.append(".summary { margin: 20px 0; padding: 10px; background-color: #f5f7fa; border-radius: 5px; }\n");
        html.append("table { border-collapse: collapse; width: 100%; margin-top: 20px; }\n");
        html.append("th { background-color: #34495e; color: white; padding: 10px; text-align: left; }\n");
        html.append("td { padding: 8px; border: 1px solid #ddd; }\n");
        html.append(".matched { background-color: #c8e6c9; }\n");
        html.append(".mismatched { background-color: #ffcdd2; }\n");
        html.append(".source-only { background-color: #e1bee7; }\n");
        html.append(".target-only { background-color: #bbdefb; }\n");
        html.append("</style>\n</head>\n<body>\n");

        html.append("<h1>Data Comparison: ").append(result.getTableName()).append("</h1>\n");
        html.append("<div class='summary'>\n");
        html.append("<p><strong>Source:</strong> ").append(result.getSourceConnection()).append("</p>\n");
        html.append("<p><strong>Target:</strong> ").append(result.getTargetConnection()).append("</p>\n");
        html.append("<p><strong>ID Columns:</strong> ").append(String.join(", ", result.getPrimaryKeyColumns())).append("</p>\n");
        html.append("<p><strong>Total Rows:</strong> ").append(result.getSummary().getTotalRows());
        html.append(" | <strong>Matched:</strong> ").append(result.getSummary().getMatchedRows());
        html.append(" | <strong>Mismatched:</strong> ").append(result.getSummary().getMismatchedRows());
        html.append(" | <strong>Source Only:</strong> ").append(result.getSummary().getSourceOnlyRows());
        html.append(" | <strong>Target Only:</strong> ").append(result.getSummary().getTargetOnlyRows());
        html.append("</p>\n</div>\n");

        html.append("<table>\n<thead>\n<tr>\n");
        html.append("<th>Status</th>\n");

        for (String pkCol : result.getPrimaryKeyColumns()) {
            html.append("<th>").append(pkCol).append(" (PK)</th>\n");
        }

        for (String column : result.getCommonColumns()) {
            if (!result.getPrimaryKeyColumns().contains(column)) {
                html.append("<th>").append(column).append(" (S)</th>\n");
                html.append("<th>").append(column).append(" (T)</th>\n");
            }
        }

        html.append("</tr>\n</thead>\n<tbody>\n");

        for (ComparisonRow compRow : result.getComparisonRows()) {
            String rowClass = "";
            switch (compRow.getStatus()) {
                case MATCHED: rowClass = "matched"; break;
                case MISMATCHED: rowClass = "mismatched"; break;
                case SOURCE_ONLY: rowClass = "source-only"; break;
                case TARGET_ONLY: rowClass = "target-only"; break;
            }

            html.append("<tr class='").append(rowClass).append("'>\n");
            html.append("<td>").append(getStatusText(compRow.getStatus())).append("</td>\n");

            for (String pkCol : result.getPrimaryKeyColumns()) {
                Object pkValue = compRow.getPrimaryKeyValues().get(pkCol);
                html.append("<td>").append(pkValue != null ? pkValue.toString() : "null").append("</td>\n");
            }

            for (String column : result.getCommonColumns()) {
                if (!result.getPrimaryKeyColumns().contains(column)) {
                    CellComparison cellComp = compRow.getCellComparisons().get(column);
                    if (cellComp != null) {
                        String cellClass = cellComp.isMatched() ? "matched" : "mismatched";
                        html.append("<td class='").append(cellClass).append("'>").append(cellComp.getSourceValue() != null ? cellComp.getSourceValue().toString() : "null").append("</td>\n");
                        html.append("<td class='").append(cellClass).append("'>").append(cellComp.getTargetValue() != null ? cellComp.getTargetValue().toString() : "null").append("</td>\n");
                    } else {
                        html.append("<td></td><td></td>\n");
                    }
                }
            }

            html.append("</tr>\n");
        }

        html.append("</tbody>\n</table>\n</body>\n</html>");

        try (java.io.FileWriter writer = new java.io.FileWriter(file)) {
            writer.write(html.toString());
        }
    }

    /**
     * Write comparison data to CSV file
     */
    private void exportToCsvFile(java.io.File file) throws Exception {
        StringBuilder csv = new StringBuilder();

        // Write header
        csv.append("Status,");

        for (String pkCol : result.getPrimaryKeyColumns()) {
            csv.append(pkCol).append(" (PK),");
        }

        for (String column : result.getCommonColumns()) {
            if (!result.getPrimaryKeyColumns().contains(column)) {
                csv.append(column).append(" (Source),");
                csv.append(column).append(" (Target),");
                csv.append(column).append(" (Match),");
            }
        }
        csv.append("\n");

        // Write data
        for (ComparisonRow compRow : result.getComparisonRows()) {
            csv.append("\"").append(getStatusText(compRow.getStatus())).append("\",");

            for (String pkCol : result.getPrimaryKeyColumns()) {
                Object pkValue = compRow.getPrimaryKeyValues().get(pkCol);
                csv.append("\"").append(pkValue != null ? pkValue.toString().replace("\"", "\"\"") : "null").append("\",");
            }

            for (String column : result.getCommonColumns()) {
                if (!result.getPrimaryKeyColumns().contains(column)) {
                    CellComparison cellComp = compRow.getCellComparisons().get(column);
                    if (cellComp != null) {
                        csv.append("\"").append(cellComp.getSourceValue() != null ? cellComp.getSourceValue().toString().replace("\"", "\"\"") : "null").append("\",");
                        csv.append("\"").append(cellComp.getTargetValue() != null ? cellComp.getTargetValue().toString().replace("\"", "\"\"") : "null").append("\",");
                        csv.append("\"").append(cellComp.isMatched() ? "Yes" : "No").append("\",");
                    } else {
                        csv.append(",,,");
                    }
                }
            }
            csv.append("\n");
        }

        try (java.io.FileWriter writer = new java.io.FileWriter(file)) {
            writer.write(csv.toString());
        }
    }
}


