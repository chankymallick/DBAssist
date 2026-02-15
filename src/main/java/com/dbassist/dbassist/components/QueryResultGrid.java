package com.dbassist.dbassist.components;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.*;

/**
 * Read-only grid for displaying SQL query results
 * Unlike TableDataGrid, this doesn't connect to database or support filtering
 */
public class QueryResultGrid extends VBox {

    private final String connectionName;
    private final String querySnippet;
    private final List<Map<String, Object>> data;
    private final List<String> columns;
    private TableView<Map<String, Object>> tableView;
    private Label statusLabel;
    private Map<String, Boolean> columnVisibilityState;

    public QueryResultGrid(String connectionName, String querySnippet,
                          List<Map<String, Object>> data, List<String> columns) {
        this.connectionName = connectionName;
        this.querySnippet = querySnippet;
        this.data = new ArrayList<>(data); // Make a copy
        this.columns = new ArrayList<>(columns);
        this.columnVisibilityState = new HashMap<>();

        initialize();
    }

    private void initialize() {
        this.setSpacing(10);
        this.setPadding(new Insets(15));
        this.setStyle("-fx-background-color: #f5f7fa;");

        // Header
        HBox header = createHeader();
        this.getChildren().add(header);

        // TableView
        tableView = new TableView<>();
        tableView.setStyle(
            "-fx-background-color: white;" +
            "-fx-control-inner-background: #f5f5f5;" +
            "-fx-table-cell-border-color: transparent;"
        );
        tableView.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        tableView.setPlaceholder(new Label("No data available"));
        VBox.setVgrow(tableView, Priority.ALWAYS);

        // Create columns
        createColumns();

        // Load data
        tableView.getItems().addAll(data);

        this.getChildren().add(tableView);

        // Status bar
        statusLabel = new Label("Query Result - " + data.size() + " rows (Read-only snapshot)");
        statusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d; -fx-font-style: italic;");
        this.getChildren().add(statusLabel);
    }

    private HBox createHeader() {
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10));
        header.setStyle("-fx-background-color: white; -fx-background-radius: 5;");

        Label titleLabel = new Label("📊 Query Result");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label connectionLabel = new Label("Connection: " + connectionName);
        connectionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #546e7a;");

        Label queryLabel = new Label("Query: " +
            (querySnippet.length() > 50 ? querySnippet.substring(0, 47) + "..." : querySnippet));
        queryLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d; -fx-font-style: italic;");
        queryLabel.setMaxWidth(400);

        Button columnSelectorButton = new Button("📋 Columns");
        columnSelectorButton.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 8 15;");
        columnSelectorButton.setOnAction(e -> showColumnSelector());

        Label readOnlyLabel = new Label("🔒 Read-Only");
        readOnlyLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #e67e22; -fx-font-weight: bold;");
        readOnlyLabel.setTooltip(new Tooltip("This is a snapshot of query results. No filtering or refresh available."));

        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        VBox infoBox = new VBox(3);
        infoBox.getChildren().addAll(connectionLabel, queryLabel);

        header.getChildren().addAll(titleLabel, infoBox, spacer, readOnlyLabel, columnSelectorButton);

        return header;
    }

    private void createColumns() {
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

            // Initialize visibility state
            columnVisibilityState.put(columnName, true);

            tableView.getColumns().add(column);
        }
    }

    private void showColumnSelector() {
        if (tableView.getColumns().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Columns");
            alert.setHeaderText("No columns to select");
            alert.setContentText("The query result has no columns.");
            alert.showAndWait();
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Column Visibility");
        dialog.setHeaderText("Select columns to display");

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white;");

        // Add search filter
        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search columns...");
        searchField.setStyle("-fx-font-size: 13px; -fx-padding: 8;");
        content.getChildren().add(searchField);

        // Add buttons
        HBox buttonBox = new HBox(10);
        Button selectAllBtn = new Button("Select All");
        Button unselectAllBtn = new Button("Unselect All");

        selectAllBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand;");
        unselectAllBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");

        buttonBox.getChildren().addAll(selectAllBtn, unselectAllBtn);
        content.getChildren().add(buttonBox);

        content.getChildren().add(new Separator());

        // Scrollable list
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);
        scrollPane.setStyle("-fx-background-color: white;");

        VBox checkboxContainer = new VBox(8);
        checkboxContainer.setPadding(new Insets(10));

        List<CheckBox> checkBoxes = new ArrayList<>();

        for (TableColumn<Map<String, Object>, ?> column : tableView.getColumns()) {
            String columnName = column.getText();
            CheckBox checkBox = new CheckBox(columnName);
            checkBox.setSelected(column.isVisible());
            checkBox.setStyle("-fx-font-size: 13px;");
            checkBox.setUserData(column);
            checkBoxes.add(checkBox);
            checkboxContainer.getChildren().add(checkBox);
        }

        scrollPane.setContent(checkboxContainer);
        content.getChildren().add(scrollPane);

        // Search functionality
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

        selectAllBtn.setOnAction(e -> {
            for (CheckBox cb : checkBoxes) {
                if (cb.isVisible()) cb.setSelected(true);
            }
        });

        unselectAllBtn.setOnAction(e -> {
            for (CheckBox cb : checkBoxes) {
                if (cb.isVisible()) cb.setSelected(false);
            }
        });

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                for (CheckBox cb : checkBoxes) {
                    @SuppressWarnings("unchecked")
                    TableColumn<Map<String, Object>, ?> column =
                        (TableColumn<Map<String, Object>, ?>) cb.getUserData();
                    column.setVisible(cb.isSelected());
                    columnVisibilityState.put(cb.getText(), cb.isSelected());
                }
            }
        });
    }

    /**
     * Get list of visible column names (for comparison)
     */
    public List<String> getVisibleColumns() {
        List<String> visibleColumns = new ArrayList<>();
        for (TableColumn<Map<String, Object>, ?> column : tableView.getColumns()) {
            if (column.isVisible()) {
                visibleColumns.add(column.getText());
            }
        }
        return visibleColumns;
    }

    /**
     * Get current data (for comparison)
     */
    public javafx.collections.ObservableList<Map<String, Object>> getCurrentData() {
        return tableView.getItems();
    }

    /**
     * Get connection name
     */
    public String getConnectionName() {
        return connectionName;
    }

    /**
     * Get query snippet
     */
    public String getQuerySnippet() {
        return querySnippet;
    }
}

