package com.dbassist.dbassist.components;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.ArrayList;

/**
 * Dialog for selecting two tabs to compare and choosing row identification columns
 */
public class TabSelectionDialog extends Dialog<TabSelectionDialog.ComparisonConfig> {

    private ComboBox<TabInfo> sourceTabCombo;
    private ComboBox<TabInfo> targetTabCombo;
    private Label validationLabel;
    private VBox columnSelectionBox;
    private List<CheckBox> columnCheckBoxes;
    private Label columnInstructionLabel;
    private TextField columnSearchField;
    private ScrollPane columnScrollPane;

    public TabSelectionDialog(List<TabInfo> availableTabs) {
        setTitle("Compare Table Data");
        setHeaderText("Configure data comparison");

        columnCheckBoxes = new ArrayList<>();

        // Create content
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white;");
        content.setPrefWidth(550);
        content.setMinHeight(400); // Ensure dialog is tall enough

        // Instructions
        Label instructionLabel = new Label("Step 1: Select source and target tabs to compare");
        instructionLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #546e7a; -fx-font-weight: bold;");

        // Grid for combo boxes
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);

        Label sourceLabel = new Label("Source Tab:");
        sourceLabel.setStyle("-fx-font-weight: 600;");
        sourceTabCombo = new ComboBox<>();
        sourceTabCombo.setPrefWidth(350);
        sourceTabCombo.setItems(FXCollections.observableArrayList(availableTabs));

        Label targetLabel = new Label("Target Tab:");
        targetLabel.setStyle("-fx-font-weight: 600;");
        targetTabCombo = new ComboBox<>();
        targetTabCombo.setPrefWidth(350);
        targetTabCombo.setItems(FXCollections.observableArrayList(availableTabs));

        grid.add(sourceLabel, 0, 0);
        grid.add(sourceTabCombo, 1, 0);
        grid.add(targetLabel, 0, 1);
        grid.add(targetTabCombo, 1, 1);

        // Validation label
        validationLabel = new Label("");
        validationLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");
        validationLabel.setVisible(false);

        // Separator
        Separator separator = new Separator();
        separator.setPadding(new Insets(10, 0, 10, 0));

        // Column selection section
        columnInstructionLabel = new Label("Step 2: Select columns for row identification");
        columnInstructionLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #546e7a; -fx-font-weight: bold;");
        columnInstructionLabel.setVisible(false);

        Label columnHelp = new Label("Choose columns that uniquely identify rows (e.g., CustomerID, OrderNumber)");
        columnHelp.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d; -fx-font-style: italic;");
        columnHelp.setWrapText(true);
        columnHelp.setVisible(false);

        // Search filter for columns
        columnSearchField = new TextField();
        columnSearchField.setPromptText("🔍 Search columns...");
        columnSearchField.setStyle("-fx-font-size: 12px; -fx-padding: 8;");
        columnSearchField.setVisible(false);

        // Column search filter action
        columnSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterColumnCheckboxes(newVal);
        });

        // Use simple VBox with border - this will be wrapped in ScrollPane
        columnSelectionBox = new VBox(8);
        columnSelectionBox.setStyle("-fx-background-color: white; -fx-padding: 15;");

        // Wrap VBox in ScrollPane for scrolling
        columnScrollPane = new ScrollPane(columnSelectionBox);
        columnScrollPane.setFitToWidth(true);
        columnScrollPane.setPrefHeight(250);
        columnScrollPane.setMaxHeight(250);
        columnScrollPane.setStyle("-fx-background-color: white; " +
                                  "-fx-border-color: #cfd8dc; -fx-border-width: 1; -fx-border-radius: 5; " +
                                  "-fx-background-radius: 5;");
        columnScrollPane.setVisible(false);
        columnScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        columnScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // Info box
        VBox infoBox = new VBox(8);
        infoBox.setStyle("-fx-background-color: #e3f2fd; -fx-padding: 12; -fx-background-radius: 5;");
        Label infoTitle = new Label("ℹ Comparison Requirements:");
        infoTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #1565c0;");
        Label info1 = new Label("• Both tabs must display the same table");
        Label info2 = new Label("• Only common visible columns will be compared");
        Label info3 = new Label("• Select columns that uniquely identify matching rows");
        info1.setStyle("-fx-text-fill: #424242; -fx-font-size: 12px;");
        info2.setStyle("-fx-text-fill: #424242; -fx-font-size: 12px;");
        info3.setStyle("-fx-text-fill: #424242; -fx-font-size: 12px;");
        infoBox.getChildren().addAll(infoTitle, info1, info2, info3);

        content.getChildren().addAll(
            instructionLabel,
            grid,
            validationLabel,
            separator,
            columnInstructionLabel,
            columnHelp,
            columnSearchField,
            columnScrollPane, // ScrollPane wrapping the VBox
            infoBox
        );

        getDialogPane().setContent(content);
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Validation
        Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(true);

        // Add listeners
        sourceTabCombo.valueProperty().addListener((obs, old, newVal) -> {
            validateSelection(okButton);
            if (newVal != null && targetTabCombo.getValue() != null) {
                updateColumnSelection(newVal, targetTabCombo.getValue(), columnHelp);
            }
        });

        targetTabCombo.valueProperty().addListener((obs, old, newVal) -> {
            validateSelection(okButton);
            if (newVal != null && sourceTabCombo.getValue() != null) {
                updateColumnSelection(sourceTabCombo.getValue(), newVal, columnHelp);
            }
        });

        // Result converter
        setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                List<String> selectedColumns = new ArrayList<>();
                for (CheckBox cb : columnCheckBoxes) {
                    if (cb.isSelected()) {
                        selectedColumns.add(cb.getText());
                    }
                }
                return new ComparisonConfig(
                    sourceTabCombo.getValue(),
                    targetTabCombo.getValue(),
                    selectedColumns
                );
            }
            return null;
        });
    }

    private void validateSelection(Button okButton) {
        TabInfo source = sourceTabCombo.getValue();
        TabInfo target = targetTabCombo.getValue();

        if (source == null || target == null) {
            okButton.setDisable(true);
            validationLabel.setVisible(false);
            return;
        }

        // Check if same tab
        if (source.getTabId().equals(target.getTabId())) {
            validationLabel.setText("⚠ Please select two different tabs");
            validationLabel.setVisible(true);
            okButton.setDisable(true);
            return;
        }

        // Check if same table
        if (!source.getTableName().equals(target.getTableName())) {
            validationLabel.setText("⚠ Tabs must display the same table (Source: " +
                source.getTableName() + ", Target: " + target.getTableName() + ")");
            validationLabel.setVisible(true);
            okButton.setDisable(true);
            return;
        }

        // If we get here, tab selection is valid
        // Now check if columns are populated and at least one is selected
        if (columnCheckBoxes.isEmpty()) {
            // Columns not loaded yet - disable OK button
            validationLabel.setText("⚠ Please wait for columns to load...");
            validationLabel.setVisible(true);
            okButton.setDisable(true);
            return;
        }

        // Check if at least one column is selected for identification
        boolean hasSelectedColumn = false;
        for (CheckBox cb : columnCheckBoxes) {
            if (cb.isSelected()) {
                hasSelectedColumn = true;
                break;
            }
        }

        if (!hasSelectedColumn) {
            validationLabel.setText("⚠ Please select at least one column for row identification");
            validationLabel.setVisible(true);
            okButton.setDisable(true);
            return;
        }

        // Valid selection
        validationLabel.setVisible(false);
        okButton.setDisable(false);
    }

    private void updateColumnSelection(TabInfo source, TabInfo target, Label helpLabel) {
        System.out.println("updateColumnSelection called - Source: " + source.getTableName() +
                          ", Target: " + target.getTableName());

        // Find common columns
        List<String> commonColumns = new ArrayList<>(source.getVisibleColumns());
        commonColumns.retainAll(target.getVisibleColumns());

        System.out.println("Common columns found: " + commonColumns.size());
        System.out.println("Common columns: " + commonColumns);

        columnCheckBoxes.clear();
        columnSelectionBox.getChildren().clear();

        if (commonColumns.isEmpty()) {
            Label noColumnsLabel = new Label("⚠ No common columns found between selected tabs");
            noColumnsLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");
            columnSelectionBox.getChildren().add(noColumnsLabel);
        } else {
            // Add "Select All" and "Clear All" buttons
            HBox buttonBox = new HBox(10);
            buttonBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            Button selectAllBtn = new Button("Select All");
            Button clearAllBtn = new Button("Clear All");

            selectAllBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 6 12;");
            clearAllBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 6 12;");

            selectAllBtn.setOnAction(e -> {
                for (CheckBox cb : columnCheckBoxes) {
                    cb.setSelected(true);
                }
                validateSelection((Button) getDialogPane().lookupButton(ButtonType.OK));
            });

            clearAllBtn.setOnAction(e -> {
                for (CheckBox cb : columnCheckBoxes) {
                    cb.setSelected(false);
                }
                validateSelection((Button) getDialogPane().lookupButton(ButtonType.OK));
            });

            buttonBox.getChildren().addAll(selectAllBtn, clearAllBtn);
            columnSelectionBox.getChildren().add(buttonBox);

            // Add separator
            Separator sep = new Separator();
            sep.setPadding(new Insets(8, 0, 8, 0));
            columnSelectionBox.getChildren().add(sep);

            // Add label showing count
            Label countLabel = new Label("Select one or more columns (" + commonColumns.size() + " available):");
            countLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #546e7a; -fx-font-weight: 600;");
            columnSelectionBox.getChildren().add(countLabel);

            // Add checkboxes for each column
            for (String column : commonColumns) {
                CheckBox cb = new CheckBox(column);
                cb.setStyle("-fx-font-size: 13px; -fx-padding: 4 0 4 0;");
                cb.selectedProperty().addListener((obs, old, newVal) -> {
                    validateSelection((Button) getDialogPane().lookupButton(ButtonType.OK));
                });
                columnCheckBoxes.add(cb);
                columnSelectionBox.getChildren().add(cb);
            }

            System.out.println("Added " + columnCheckBoxes.size() + " column checkboxes to VBox");
        }

        // Make everything visible
        columnInstructionLabel.setVisible(true);
        helpLabel.setVisible(true);
        columnSearchField.setVisible(true);
        columnScrollPane.setVisible(true);

        System.out.println("Column selection UI visibility set to true");
        System.out.println("VBox children count: " + columnSelectionBox.getChildren().size());

        // Re-validate after loading columns
        validateSelection((Button) getDialogPane().lookupButton(ButtonType.OK));
    }

    /**
     * Filter column checkboxes based on search text
     */
    private void filterColumnCheckboxes(String searchText) {
        String search = searchText == null ? "" : searchText.toLowerCase().trim();

        for (CheckBox cb : columnCheckBoxes) {
            if (search.isEmpty()) {
                cb.setVisible(true);
                cb.setManaged(true);
            } else {
                boolean matches = cb.getText().toLowerCase().contains(search);
                cb.setVisible(matches);
                cb.setManaged(matches);
            }
        }
    }

    /**
     * Information about a tab for selection
     */
    public static class TabInfo {
        private String tabId;
        private String connectionName;
        private String tableName;
        private String displayName;
        private List<String> visibleColumns;

        public TabInfo(String tabId, String connectionName, String tableName, List<String> visibleColumns) {
            this.tabId = tabId;
            this.connectionName = connectionName;
            this.tableName = tableName;
            this.visibleColumns = visibleColumns;
            this.displayName = connectionName + " - " + tableName;
        }

        public String getTabId() {
            return tabId;
        }

        public String getConnectionName() {
            return connectionName;
        }

        public String getTableName() {
            return tableName;
        }

        public List<String> getVisibleColumns() {
            return visibleColumns;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    /**
     * Configuration for comparison
     */
    public static class ComparisonConfig {
        private TabInfo sourceTab;
        private TabInfo targetTab;
        private List<String> identificationColumns;

        public ComparisonConfig(TabInfo sourceTab, TabInfo targetTab, List<String> identificationColumns) {
            this.sourceTab = sourceTab;
            this.targetTab = targetTab;
            this.identificationColumns = identificationColumns;
        }

        public TabInfo getSourceTab() {
            return sourceTab;
        }

        public TabInfo getTargetTab() {
            return targetTab;
        }

        public List<String> getIdentificationColumns() {
            return identificationColumns;
        }
    }
}


