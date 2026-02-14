package com.dbassist.dbassist.components;

import com.dbassist.dbassist.model.DataTabConfig;
import com.dbassist.dbassist.model.DatabaseConnection;
import com.dbassist.dbassist.service.ConnectionManager;
import com.dbassist.dbassist.service.TabConfigManager;
import com.dbassist.dbassist.service.TableDataService;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;

/**
 * Component to display table data in a modern grid with filters
 */
public class TableDataGrid extends VBox {

    private DataTabConfig tabConfig;
    private DatabaseConnection dbConnection;
    private TableView<Map<String, Object>> tableView;
    private Map<String, TextField> filterFields;
    private Map<String, CheckBox> exactSearchFlags; // Track exact vs like search per column
    private Label statusLabel;
    private Map<String, Boolean> columnVisibilityState; // Track column visibility

    public TableDataGrid(DataTabConfig tabConfig) {
        this.tabConfig = tabConfig;
        this.dbConnection = ConnectionManager.getInstance().getConnectionByName(tabConfig.getConnectionName());
        this.filterFields = new HashMap<>();
        this.exactSearchFlags = new HashMap<>();
        this.columnVisibilityState = new HashMap<>();

        initialize();
        loadData();
    }

    private void initialize() {
        this.setSpacing(10);
        this.setPadding(new Insets(15));
        this.setStyle("-fx-background-color: #f5f7fa;");

        // Header with table info and refresh button
        HBox header = createHeader();
        this.getChildren().add(header);

        // TableView for data
        tableView = new TableView<>();
        // Set style with light grey background for empty space
        tableView.setStyle(
            "-fx-background-color: white;" +
            "-fx-control-inner-background: #f5f5f5;" + // Light grey for empty cells
            "-fx-table-cell-border-color: transparent;"
        );
        // IMPORTANT: Remove constrained resize policy to enable horizontal scrolling
        tableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        // Disable placeholder column
        tableView.setPlaceholder(new Label("No data available"));
        VBox.setVgrow(tableView, Priority.ALWAYS);
        this.getChildren().add(tableView);

        // Status bar
        statusLabel = new Label("Ready");
        statusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");
        this.getChildren().add(statusLabel);
    }

    private HBox createHeader() {
        HBox header = new HBox(15);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        header.setPadding(new Insets(10));
        header.setStyle("-fx-background-color: white; -fx-background-radius: 5;");

        Label titleLabel = new Label("Table: " + tabConfig.getTableName());
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Button columnSelectorButton = new Button("📋 Columns");
        columnSelectorButton.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 8 15;");
        columnSelectorButton.setOnAction(e -> showColumnSelector());

        Button refreshButton = new Button("🔄 Refresh");
        refreshButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 8 15;");
        refreshButton.setOnAction(e -> loadData());

        Button clearFiltersButton = new Button("✖ Clear Filters");
        clearFiltersButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 8 15;");
        clearFiltersButton.setOnAction(e -> clearFilters());

        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(titleLabel, spacer, columnSelectorButton, clearFiltersButton, refreshButton);

        return header;
    }

    private void loadData() {
        if (dbConnection == null) {
            showError("Connection not found: " + tabConfig.getConnectionName());
            return;
        }

        statusLabel.setText("Loading data...");

        // Save current column visibility state before clearing
        saveColumnVisibilityState();

        tableView.getItems().clear();
        tableView.getColumns().clear();

        // Load data in background thread
        new Thread(() -> {
            try {
                TableDataService.TableDataResult result = TableDataService.fetchTableData(
                    dbConnection,
                    tabConfig.getTableName(),
                    tabConfig.getColumnFilters(),
                    tabConfig.getMaxRows()
                );

                Platform.runLater(() -> {
                    if (result.hasError()) {
                        showError("Error: " + result.getError());
                    } else {
                        displayData(result);
                        statusLabel.setText("Loaded " + result.getRowCount() + " rows (max " + tabConfig.getMaxRows() + ")");
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> showError("Error loading data: " + e.getMessage()));
                e.printStackTrace();
            }
        }).start();
    }

    private void displayData(TableDataService.TableDataResult result) {
        // Create columns with filters
        for (String columnName : result.getColumnNames()) {
            TableColumn<Map<String, Object>, Object> column = new TableColumn<>();

            // Create column header with filter
            VBox headerBox = new VBox(5);
            headerBox.setAlignment(javafx.geometry.Pos.CENTER);

            Label headerLabel = new Label(columnName);
            headerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

            TextField filterField = new TextField();
            filterField.setPromptText("Filter...");
            filterField.setStyle("-fx-font-size: 11px;");

            // Set existing filter value if any
            String existingFilter = tabConfig.getColumnFilters().get(columnName);
            if (existingFilter != null) {
                filterField.setText(existingFilter);
            }

            // Add exact search checkbox
            CheckBox exactSearchCheckBox = new CheckBox("Exact");
            exactSearchCheckBox.setStyle("-fx-font-size: 10px;");
            exactSearchCheckBox.setSelected(false); // Default is LIKE search
            exactSearchFlags.put(columnName, exactSearchCheckBox);

            filterField.setOnAction(e -> applyFilter(columnName, filterField.getText()));
            exactSearchCheckBox.setOnAction(e -> {
                if (!filterField.getText().isEmpty()) {
                    applyFilter(columnName, filterField.getText());
                }
            });

            filterFields.put(columnName, filterField);

            headerBox.getChildren().addAll(headerLabel, filterField, exactSearchCheckBox);
            column.setGraphic(headerBox);

            // Set cell value factory
            column.setCellValueFactory(cellData -> {
                Object value = cellData.getValue().get(columnName);
                return new javafx.beans.property.SimpleObjectProperty<>(value);
            });

            // Set cell factory for display
            column.setCellFactory(col -> new TableCell<Map<String, Object>, Object>() {
                @Override
                protected void updateItem(Object item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setText(null);
                        setStyle("");
                    } else if (item == null) {
                        setText("null");
                        setStyle("-fx-text-fill: #95a5a6; -fx-font-style: italic;"); // Grey italic for null
                    } else {
                        setText(item.toString());
                        setStyle("");
                    }
                }
            });

            // Calculate column width based on column name length
            // Minimum 100px, add 10px per character, maximum 300px
            int nameLength = columnName.length();
            double calculatedWidth = Math.min(Math.max(100, nameLength * 10 + 60), 300);
            column.setPrefWidth(calculatedWidth);
            column.setMinWidth(80);

            // Make column resizable
            column.setResizable(true);

            // Restore visibility state if it was saved
            Boolean savedVisibility = columnVisibilityState.get(columnName);
            if (savedVisibility != null) {
                column.setVisible(savedVisibility);
            }

            tableView.getColumns().add(column);
        }

        // Set data
        tableView.setItems(result.getRows());
    }

    private void applyFilter(String columnName, String filterValue) {
        if (filterValue == null || filterValue.trim().isEmpty()) {
            tabConfig.removeFilter(columnName);
        } else {
            tabConfig.addFilter(columnName, filterValue);
        }

        // Save configuration
        TabConfigManager.getInstance().updateTabConfig(tabConfig);

        // Reload data with new filter
        loadData();
    }

    private void clearFilters() {
        tabConfig.clearFilters();
        TabConfigManager.getInstance().updateTabConfig(tabConfig);

        // Clear filter fields
        for (TextField field : filterFields.values()) {
            field.clear();
        }

        // Reload data
        loadData();
    }

    private void showError(String message) {
        statusLabel.setText(message);
        statusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #e74c3c;");

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Failed to load table data");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public DataTabConfig getTabConfig() {
        return tabConfig;
    }

    /**
     * Show column visibility selector dialog
     */
    private void showColumnSelector() {
        if (tableView.getColumns().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Columns");
            alert.setHeaderText("No columns to select");
            alert.setContentText("Please load data first.");
            alert.showAndWait();
            return;
        }

        // Create dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Column Visibility");
        dialog.setHeaderText("Select columns to display");

        // Create content
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white;");

        // Add search filter
        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search columns...");
        searchField.setStyle("-fx-font-size: 13px; -fx-padding: 8;");
        content.getChildren().add(searchField);

        // Add "Select All" and "Unselect All" buttons
        HBox buttonBox = new HBox(10);
        Button selectAllBtn = new Button("Select All");
        Button unselectAllBtn = new Button("Unselect All");

        selectAllBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand;");
        unselectAllBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");

        buttonBox.getChildren().addAll(selectAllBtn, unselectAllBtn);
        content.getChildren().add(buttonBox);

        // Add separator
        javafx.scene.control.Separator separator = new javafx.scene.control.Separator();
        content.getChildren().add(separator);

        // Create scrollable list of checkboxes
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);
        scrollPane.setStyle("-fx-background-color: white;");

        VBox checkboxContainer = new VBox(8);
        checkboxContainer.setPadding(new Insets(10));

        java.util.List<CheckBox> checkBoxes = new java.util.ArrayList<>();

        for (TableColumn<Map<String, Object>, ?> column : tableView.getColumns()) {
            // Get column name from header graphic
            String columnName = "";
            if (column.getGraphic() instanceof VBox) {
                VBox headerBox = (VBox) column.getGraphic();
                if (!headerBox.getChildren().isEmpty() && headerBox.getChildren().get(0) instanceof Label) {
                    columnName = ((Label) headerBox.getChildren().get(0)).getText();
                }
            }

            CheckBox checkBox = new CheckBox(columnName);
            checkBox.setSelected(column.isVisible());
            checkBox.setStyle("-fx-font-size: 13px;");

            // Store reference to column
            checkBox.setUserData(column);
            checkBoxes.add(checkBox);

            checkboxContainer.getChildren().add(checkBox);
        }

        scrollPane.setContent(checkboxContainer);
        content.getChildren().add(scrollPane);

        // Add search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            String searchText = newValue.toLowerCase().trim();

            for (CheckBox cb : checkBoxes) {
                if (searchText.isEmpty()) {
                    cb.setVisible(true);
                    cb.setManaged(true);
                } else {
                    boolean matches = cb.getText().toLowerCase().contains(searchText);
                    cb.setVisible(matches);
                    cb.setManaged(matches);
                }
            }
        });

        // Select All button action
        selectAllBtn.setOnAction(e -> {
            for (CheckBox cb : checkBoxes) {
                if (cb.isVisible()) { // Only select visible (filtered) items
                    cb.setSelected(true);
                }
            }
        });

        // Unselect All button action
        unselectAllBtn.setOnAction(e -> {
            for (CheckBox cb : checkBoxes) {
                if (cb.isVisible()) { // Only unselect visible (filtered) items
                    cb.setSelected(false);
                }
            }
        });

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Apply selections when OK is clicked
        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                for (CheckBox cb : checkBoxes) {
                    TableColumn<Map<String, Object>, ?> column =
                        (TableColumn<Map<String, Object>, ?>) cb.getUserData();
                    column.setVisible(cb.isSelected());
                }
            }
        });
    }

    /**
     * Save current column visibility state
     */
    private void saveColumnVisibilityState() {
        columnVisibilityState.clear();
        for (TableColumn<Map<String, Object>, ?> column : tableView.getColumns()) {
            // Get column name from header graphic
            String columnName = getColumnName(column);
            if (columnName != null && !columnName.isEmpty()) {
                columnVisibilityState.put(columnName, column.isVisible());
            }
        }
    }

    /**
     * Get column name from column header graphic
     */
    private String getColumnName(TableColumn<Map<String, Object>, ?> column) {
        if (column.getGraphic() instanceof VBox) {
            VBox headerBox = (VBox) column.getGraphic();
            if (!headerBox.getChildren().isEmpty() && headerBox.getChildren().get(0) instanceof Label) {
                return ((Label) headerBox.getChildren().get(0)).getText();
            }
        }
        return null;
    }
}

