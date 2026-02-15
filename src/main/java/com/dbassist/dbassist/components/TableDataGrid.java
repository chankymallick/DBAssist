package com.dbassist.dbassist.components;

import com.dbassist.dbassist.model.DataTabConfig;
import com.dbassist.dbassist.model.DatabaseConnection;
import com.dbassist.dbassist.service.ConnectionEventManager;
import com.dbassist.dbassist.service.ConnectionManager;
import com.dbassist.dbassist.service.DatabaseMetadataService;
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
public class TableDataGrid extends VBox implements ConnectionEventManager.ConnectionChangeListener {

    private DataTabConfig tabConfig;
    private DatabaseConnection dbConnection;
    private TableView<Map<String, Object>> tableView;
    private Map<String, TextField> filterFields;
    private Map<String, CheckBox> exactSearchFlags; // Track exact vs like search per column
    private Map<String, Boolean> exactSearchState; // Persist exact search state
    private Label statusLabel;
    private Map<String, Boolean> columnVisibilityState; // Track column visibility
    private ComboBox<String> connectionComboBox; // Reference to connection switcher
    private Tab parentTab; // Reference to the parent tab for updating name
    private CloneTabCallback cloneCallback; // Callback for cloning tab
    private double savedHScrollValue = 0.0; // Preserve horizontal scroll position

    /**
     * Callback interface for clone tab action
     */
    public interface CloneTabCallback {
        void onCloneTab(DataTabConfig clonedConfig);
    }

    public TableDataGrid(DataTabConfig tabConfig) {
        this.tabConfig = tabConfig;
        this.dbConnection = ConnectionManager.getInstance().getConnectionByName(tabConfig.getConnectionName());
        this.filterFields = new HashMap<>();
        this.exactSearchFlags = new HashMap<>();
        this.exactSearchState = new HashMap<>();
        this.columnVisibilityState = new HashMap<>();

        initialize();
        loadData();

        // Register as listener for connection changes
        ConnectionEventManager.getInstance().addListener(this);
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

        // Connection switcher ComboBox
        Label connectionLabel = new Label("Connection:");
        connectionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #546e7a;");

        connectionComboBox = new ComboBox<>(); // Store reference
        connectionComboBox.setStyle("-fx-font-size: 12px;");
        connectionComboBox.setPrefWidth(200);

        // Populate with connections of same database type
        populateConnectionComboBox(connectionComboBox);

        // Set current connection as selected
        connectionComboBox.setValue(tabConfig.getConnectionName());

        // Handle connection change
        connectionComboBox.setOnAction(e -> {
            String newConnectionName = connectionComboBox.getValue();
            if (newConnectionName != null && !newConnectionName.equals(tabConfig.getConnectionName())) {
                switchConnection(newConnectionName);
            }
        });

        Button columnSelectorButton = new Button("📋 Columns");
        columnSelectorButton.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 8 15;");
        columnSelectorButton.setOnAction(e -> showColumnSelector());

        Button cloneButton = new Button("📑 Clone Tab");
        cloneButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 8 15;");
        cloneButton.setOnAction(e -> cloneTab());

        Button refreshButton = new Button("🔄 Refresh");
        refreshButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 8 15;");
        refreshButton.setOnAction(e -> loadData());

        Button clearFiltersButton = new Button("✖ Clear Filters");
        clearFiltersButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 8 15;");
        clearFiltersButton.setOnAction(e -> clearFilters());

        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(titleLabel, spacer, connectionLabel, connectionComboBox,
                                    columnSelectorButton, cloneButton, clearFiltersButton, refreshButton);

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

        // Save current exact search state before clearing
        saveExactSearchState();

        // Save horizontal scroll position before clearing
        saveHorizontalScrollPosition();

        // Only clear items, NOT columns - this preserves the table structure and scroll
        tableView.getItems().clear();

        // Only clear and rebuild columns if this is the first load
        boolean isFirstLoad = tableView.getColumns().isEmpty();

        // Load data in background thread
        new Thread(() -> {
            try {
                // Collect exact search flags
                Map<String, Boolean> exactFlags = new HashMap<>();
                for (Map.Entry<String, CheckBox> entry : exactSearchFlags.entrySet()) {
                    exactFlags.put(entry.getKey(), entry.getValue().isSelected());
                }

                TableDataService.TableDataResult result = TableDataService.fetchTableData(
                    dbConnection,
                    tabConfig.getTableName(),
                    tabConfig.getColumnFilters(),
                    exactFlags,
                    tabConfig.getMaxRows()
                );

                Platform.runLater(() -> {
                    if (result.hasError()) {
                        showError("Error: " + result.getError());
                    } else {
                        if (isFirstLoad) {
                            // First load: create columns
                            displayData(result);
                        } else {
                            // Subsequent loads: just update data, keep columns
                            tableView.getItems().addAll(result.getRows());
                        }

                        String statusMessage = result.getRowCount() > 0
                            ? "Loaded " + result.getRowCount() + " rows (max " + tabConfig.getMaxRows() + ")"
                            : "No data matches the current filters";
                        statusLabel.setText(statusMessage);

                        // Restore horizontal scroll position after data is displayed
                        restoreHorizontalScrollPosition();
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

            // Add exact search checkbox - LEFT ALIGNED
            CheckBox exactSearchCheckBox = new CheckBox("Exact");
            exactSearchCheckBox.setStyle("-fx-font-size: 10px;");

            // Create HBox for checkbox to control its alignment
            HBox checkboxContainer = new HBox();
            checkboxContainer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            checkboxContainer.getChildren().add(exactSearchCheckBox);

            // Restore saved exact search state if exists
            Boolean savedExactState = exactSearchState.get(columnName);
            exactSearchCheckBox.setSelected(savedExactState != null ? savedExactState : false);

            exactSearchFlags.put(columnName, exactSearchCheckBox);

            filterField.setOnAction(e -> applyFilter(columnName, filterField.getText()));

            exactSearchCheckBox.setOnAction(e -> {
                // Save the exact search state
                exactSearchState.put(columnName, exactSearchCheckBox.isSelected());

                // Re-apply filter if there's text in the field
                if (!filterField.getText().isEmpty()) {
                    applyFilter(columnName, filterField.getText());
                }
            });

            filterFields.put(columnName, filterField);

            headerBox.getChildren().addAll(headerLabel, filterField, checkboxContainer);
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
            } else {
                // Check if there's a saved visibility in tabConfig (from disk)
                Boolean configVisibility = tabConfig.isColumnVisible(columnName);
                column.setVisible(configVisibility);
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
     * Set the parent tab reference for updating tab name
     */
    public void setParentTab(Tab parentTab) {
        this.parentTab = parentTab;
    }

    /**
     * Set clone tab callback
     */
    public void setCloneCallback(CloneTabCallback callback) {
        this.cloneCallback = callback;
    }

    /**
     * Get list of visible column names
     */
    public java.util.List<String> getVisibleColumns() {
        java.util.List<String> visibleColumns = new java.util.ArrayList<>();
        System.out.println("getVisibleColumns called - Total columns: " + tableView.getColumns().size());

        for (TableColumn<Map<String, Object>, ?> column : tableView.getColumns()) {
            if (column.isVisible()) {
                String columnName = getColumnName(column);
                System.out.println("  Column visible: " + (columnName != null ? columnName : "NULL NAME"));
                if (columnName != null && !columnName.isEmpty()) {
                    visibleColumns.add(columnName);
                }
            }
        }

        System.out.println("Returning " + visibleColumns.size() + " visible columns: " + visibleColumns);
        return visibleColumns;
    }

    /**
     * Get the current data from table view
     */
    public javafx.collections.ObservableList<Map<String, Object>> getCurrentData() {
        return tableView.getItems();
    }

    /**
     * Get primary key columns (for comparison purposes)
     * For now, we'll try to identify them or use first column as fallback
     */
    public java.util.List<String> getPrimaryKeyColumns() {
        java.util.List<String> pkColumns = new java.util.ArrayList<>();

        // Try to get PK columns from metadata
        // For now, use first visible column as PK (simplified)
        // In production, this should query database metadata
        java.util.List<String> visibleCols = getVisibleColumns();
        if (!visibleCols.isEmpty()) {
            pkColumns.add(visibleCols.get(0)); // Use first column as PK
        }

        return pkColumns;
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
                // Clear existing visibility settings
                tabConfig.getColumnVisibility().clear();

                for (CheckBox cb : checkBoxes) {
                    TableColumn<Map<String, Object>, ?> column =
                        (TableColumn<Map<String, Object>, ?>) cb.getUserData();
                    boolean isVisible = cb.isSelected();
                    column.setVisible(isVisible);

                    // Save to tabConfig
                    String columnName = cb.getText();
                    tabConfig.setColumnVisible(columnName, isVisible);
                }

                // Persist to disk
                TabConfigManager.getInstance().updateTabConfig(tabConfig);
                System.out.println("Column visibility saved for table: " + tabConfig.getTableName());
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
        // Try to get from graphic (VBox header with filter)
        if (column.getGraphic() instanceof VBox) {
            VBox headerBox = (VBox) column.getGraphic();
            if (!headerBox.getChildren().isEmpty() && headerBox.getChildren().get(0) instanceof Label) {
                String name = ((Label) headerBox.getChildren().get(0)).getText();
                System.out.println("    Found column name from graphic: " + name);
                return name;
            }
        }

        // Fallback: try to get from column text
        if (column.getText() != null && !column.getText().isEmpty()) {
            System.out.println("    Found column name from text: " + column.getText());
            return column.getText();
        }

        System.out.println("    WARNING: Could not find column name!");
        return null;
    }

    /**
     * Save current exact search state before refresh
     */
    private void saveExactSearchState() {
        exactSearchState.clear();
        for (Map.Entry<String, CheckBox> entry : exactSearchFlags.entrySet()) {
            exactSearchState.put(entry.getKey(), entry.getValue().isSelected());
        }
    }

    /**
     * Save horizontal scroll position
     */
    private void saveHorizontalScrollPosition() {
        // Get the horizontal scrollbar from the TableView
        javafx.scene.Node node = tableView.lookup(".scroll-bar:horizontal");
        if (node instanceof ScrollBar) {
            ScrollBar scrollBar = (ScrollBar) node;
            savedHScrollValue = scrollBar.getValue();
            System.out.println("Saved horizontal scroll position: " + savedHScrollValue);
        }
    }

    /**
     * Restore horizontal scroll position
     */
    private void restoreHorizontalScrollPosition() {
        // Delay restoration to ensure table is fully rendered
        Platform.runLater(() -> {
            javafx.scene.Node node = tableView.lookup(".scroll-bar:horizontal");
            if (node instanceof ScrollBar) {
                ScrollBar scrollBar = (ScrollBar) node;
                scrollBar.setValue(savedHScrollValue);
                System.out.println("Restored horizontal scroll position: " + savedHScrollValue);
            }
        });
    }

    /**
     * Populate connection combo box with connections of same database type
     */
    private void populateConnectionComboBox(ComboBox<String> comboBox) {
        if (dbConnection == null) return;

        String currentDbType = dbConnection.getDatabaseType();

        // Get all connections of the same type
        for (DatabaseConnection conn : ConnectionManager.getInstance().getAllConnections()) {
            if (conn.getDatabaseType().equals(currentDbType)) {
                comboBox.getItems().add(conn.getConnectionName());
            }
        }
    }

    /**
     * Switch to a different connection - validate table and columns exist first
     */
    private void switchConnection(String newConnectionName) {
        DatabaseConnection newConnection = ConnectionManager.getInstance().getConnectionByName(newConnectionName);

        if (newConnection == null) {
            showError("Connection not found: " + newConnectionName);
            return;
        }

        // Validate in background thread
        statusLabel.setText("Validating connection compatibility...");

        new Thread(() -> {
            try {
                // Check if table exists in new connection
                boolean tableExists = validateTableExists(newConnection, tabConfig.getTableName());

                if (!tableExists) {
                    Platform.runLater(() -> {
                        showError("Table '" + tabConfig.getTableName() +
                                "' does not exist in connection '" + newConnectionName + "'");
                        // Revert combo box selection
                        refreshConnectionComboBox();
                    });
                    return;
                }

                // Get columns from new connection
                java.util.List<String> newColumns = getTableColumns(newConnection, tabConfig.getTableName());

                // Get columns from current view
                java.util.List<String> currentColumns = getCurrentColumnNames();

                // Check if all current columns exist in new connection
                java.util.List<String> missingColumns = new java.util.ArrayList<>();
                for (String col : currentColumns) {
                    if (!newColumns.contains(col)) {
                        missingColumns.add(col);
                    }
                }

                if (!missingColumns.isEmpty()) {
                    Platform.runLater(() -> {
                        showError("Connection '" + newConnectionName +
                                "' is missing columns: " + String.join(", ", missingColumns) +
                                "\n\nCannot switch connections.");
                        // Revert combo box selection
                        refreshConnectionComboBox();
                    });
                    return;
                }

                // All validations passed - switch connection
                Platform.runLater(() -> {
                    dbConnection = newConnection;
                    tabConfig.setConnectionName(newConnectionName);

                    // Save updated config
                    TabConfigManager.getInstance().updateTabConfig(tabConfig);

                    // Update tab name with new connection
                    updateTabName();

                    // Reload data with new connection
                    statusLabel.setText("Switched to: " + newConnectionName);
                    loadData();

                    System.out.println("Successfully switched to connection: " + newConnectionName);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Error validating connection: " + e.getMessage());
                    refreshConnectionComboBox();
                });
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Validate if table exists in the connection
     */
    private boolean validateTableExists(DatabaseConnection connection, String tableName) {
        try {
            java.util.List<String> tables = DatabaseMetadataService.getTables(connection);
            return tables.contains(tableName);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get column names from a table in specific connection
     */
    private java.util.List<String> getTableColumns(DatabaseConnection connection, String tableName) {
        try {
            TableDataService.TableDataResult result = TableDataService.fetchTableData(
                connection, tableName, new java.util.HashMap<>(),
                new java.util.HashMap<>(), 1);
            return result.getColumnNames();
        } catch (Exception e) {
            e.printStackTrace();
            return new java.util.ArrayList<>();
        }
    }

    /**
     * Get current column names being displayed
     */
    private java.util.List<String> getCurrentColumnNames() {
        java.util.List<String> columns = new java.util.ArrayList<>();
        for (TableColumn<Map<String, Object>, ?> column : tableView.getColumns()) {
            String columnName = getColumnName(column);
            if (columnName != null) {
                columns.add(columnName);
            }
        }
        return columns;
    }

    /**
     * Refresh connection combo box to current selection
     */
    private void refreshConnectionComboBox() {
        if (connectionComboBox != null) {
            connectionComboBox.setValue(tabConfig.getConnectionName());
        }
    }

    /**
     * Update parent tab name to reflect new connection
     */
    private void updateTabName() {
        if (parentTab != null) {
            // When connection switches, clear custom name so connection name shows
            // Custom names are only for clones with same connection
            tabConfig.setCustomDisplayName(null);

            String newTabName = tabConfig.getDisplayName(); // "ConnectionName - TableName"

            // Update the tab header label
            if (parentTab.getGraphic() instanceof HBox) {
                HBox headerBox = (HBox) parentTab.getGraphic();
                for (javafx.scene.Node node : headerBox.getChildren()) {
                    if (node instanceof Label) {
                        Label tabLabel = (Label) node;
                        tabLabel.setText(newTabName);
                        System.out.println("Updated tab name to: " + newTabName);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Clone the current tab with all filters and settings
     */
    private void cloneTab() {
        if (cloneCallback == null) {
            showError("Clone functionality not available");
            return;
        }

        // Ask user for new tab name
        TextInputDialog dialog = new TextInputDialog(tabConfig.getTableName() + " (Copy)");
        dialog.setTitle("Clone Tab");
        dialog.setHeaderText("Create a clone of this tab");
        dialog.setContentText("Enter name for cloned tab:");

        java.util.Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            // Create cloned configuration
            DataTabConfig clonedConfig = new DataTabConfig(
                tabConfig.getConnectionName(),
                tabConfig.getTableName()
            );

            // Generate unique tab ID
            clonedConfig.setTabId(tabConfig.getConnectionName() + "_" +
                                 tabConfig.getTableName() + "_" +
                                 System.currentTimeMillis());

            // Set custom display name from user input
            clonedConfig.setCustomDisplayName(newName);

            // Copy all filters
            clonedConfig.setColumnFilters(new java.util.HashMap<>(tabConfig.getColumnFilters()));

            // Copy column visibility settings
            Map<String, Boolean> currentVisibility = new java.util.HashMap<>();
            for (TableColumn<Map<String, Object>, ?> column : tableView.getColumns()) {
                String columnName = getColumnName(column);
                if (columnName != null) {
                    currentVisibility.put(columnName, column.isVisible());
                }
            }
            clonedConfig.setColumnVisibility(currentVisibility);

            // Copy max rows setting
            clonedConfig.setMaxRows(tabConfig.getMaxRows());

            System.out.println("Cloning tab: " + tabConfig.getDisplayName());
            System.out.println("  New name: " + newName);
            System.out.println("  Filters: " + clonedConfig.getColumnFilters().size());
            System.out.println("  Visible columns: " + currentVisibility.size());

            // Callback to parent controller to create new tab
            cloneCallback.onCloneTab(clonedConfig);

            // Show success message
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Tab Cloned");
                alert.setHeaderText("Success");
                alert.setContentText("Tab '" + newName + "' created successfully with all filters and settings preserved.");
                alert.showAndWait();
            });
        });
    }

    // ========== ConnectionChangeListener Implementation ==========

    @Override
    public void onConnectionAdded(String connectionName) {
        // Run on JavaFX thread
        Platform.runLater(() -> {
            if (connectionComboBox != null && dbConnection != null) {
                DatabaseConnection newConnection = ConnectionManager.getInstance().getConnectionByName(connectionName);

                // Only add if same database type
                if (newConnection != null && newConnection.getDatabaseType().equals(dbConnection.getDatabaseType())) {
                    // Check if not already in list
                    if (!connectionComboBox.getItems().contains(connectionName)) {
                        connectionComboBox.getItems().add(connectionName);
                        System.out.println("Added connection to combo box: " + connectionName);
                    }
                }
            }
        });
    }

    @Override
    public void onConnectionRemoved(String connectionName) {
        // Run on JavaFX thread
        Platform.runLater(() -> {
            if (connectionComboBox != null) {
                connectionComboBox.getItems().remove(connectionName);
                System.out.println("Removed connection from combo box: " + connectionName);

                // If current connection was removed, might need to handle that
                if (connectionName.equals(tabConfig.getConnectionName())) {
                    statusLabel.setText("Warning: Current connection was removed");
                }
            }
        });
    }

    @Override
    public void onConnectionUpdated(String connectionName) {
        // Run on JavaFX thread
        Platform.runLater(() -> {
            if (connectionComboBox != null && connectionName.equals(tabConfig.getConnectionName())) {
                // Reload connection reference
                dbConnection = ConnectionManager.getInstance().getConnectionByName(connectionName);
                System.out.println("Connection updated: " + connectionName);
            }
        });
    }

    /**
     * Load query data directly (for SQL worksheet results)
     */
    public void loadQueryData(java.util.List<Map<String, Object>> data, java.util.List<String> columns) {
        Platform.runLater(() -> {
            // Clear existing columns
            tableView.getColumns().clear();
            filterFields.clear();

            // Create columns from query results
            for (String columnName : columns) {
                TableColumn<Map<String, Object>, String> column = new TableColumn<>(columnName);
                column.setPrefWidth(Math.max(150, columnName.length() * 10));

                column.setCellValueFactory(cellData -> {
                    Object value = cellData.getValue().get(columnName);
                    return new javafx.beans.property.SimpleStringProperty(
                        value != null ? value.toString() : "null"
                    );
                });

                // Style null values
                column.setCellFactory(col -> new TableCell<Map<String, Object>, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setStyle("");
                        } else {
                            setText(item);
                            if ("null".equals(item)) {
                                setStyle("-fx-text-fill: #95a5a6; -fx-font-style: italic;");
                            } else {
                                setStyle("");
                            }
                        }
                    }
                });

                tableView.getColumns().add(column);
            }

            // Load data
            tableView.getItems().clear();
            tableView.getItems().addAll(data);

            statusLabel.setText("Loaded " + data.size() + " rows");
        });
    }
}
