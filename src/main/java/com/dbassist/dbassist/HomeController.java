package com.dbassist.dbassist;

import com.dbassist.dbassist.components.ConnectionTreeCellFactory;
import com.dbassist.dbassist.components.TableDataGrid;
import com.dbassist.dbassist.connection.NewConnectionController;
import com.dbassist.dbassist.model.DatabaseConnection;
import com.dbassist.dbassist.model.DataTabConfig;
import com.dbassist.dbassist.service.ConnectionManager;
import com.dbassist.dbassist.service.DatabaseMetadataService;
import com.dbassist.dbassist.service.TabConfigManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HomeController {

    @FXML
    private Label titleLabel;

    @FXML
    private Label subtitleLabel;

    @FXML
    private Button newConnectionButton;

    @FXML
    private Button recentConnectionsButton;

    @FXML
    private Button settingsButton;

    @FXML
    private Button aboutButton;

    @FXML
    private VBox mainContainer;

    @FXML
    private HBox titleBar;

    @FXML
    private HBox iconContainer;

    @FXML
    private Button minimizeButton;

    @FXML
    private Button maximizeButton;

    @FXML
    private Button closeButton;

    @FXML
    private TreeView<String> connectionTree;

    @FXML
    private TextField treeSearchField;

    @FXML
    private TabPane mainTabPane;

    @FXML
    private Tab homeTab;

    private Stage stage;
    private double xOffset = 0;
    private double yOffset = 0;
    private boolean isMaximized = false;
    private double previousX, previousY, previousWidth, previousHeight;

    @FXML
    public void initialize() {
        // Initialize the home page
        System.out.println("Home Page Initialized");

        // Add FontAwesome database icon to title bar
        if (iconContainer != null) {
            FontIcon dbIcon = new FontIcon(FontAwesomeSolid.DATABASE);
            dbIcon.setIconSize(18);
            dbIcon.setIconColor(Color.WHITE);
            iconContainer.getChildren().add(0, dbIcon);
        }

        // Initialize connection tree with sample data
        initializeConnectionTree();

        // Apply custom cell factory for icons
        if (connectionTree != null) {
            connectionTree.setCellFactory(new ConnectionTreeCellFactory());

            // Add context menu for tree items
            connectionTree.setCellFactory(tv -> {
                TreeCell<String> cell = new ConnectionTreeCellFactory().call(tv);

                cell.setOnContextMenuRequested(event -> {
                    TreeItem<String> item = cell.getTreeItem();
                    if (item != null && item.getParent() != null) {
                        String parentValue = item.getParent().getValue();

                        // Context menu for table items
                        if ("Tables".equals(parentValue)) {
                            ContextMenu contextMenu = new ContextMenu();
                            MenuItem fetchDataItem = new MenuItem("Fetch Data");
                            fetchDataItem.setOnAction(e -> openTableDataTab(item.getValue()));
                            contextMenu.getItems().add(fetchDataItem);
                            contextMenu.show(cell, event.getScreenX(), event.getScreenY());
                        }
                        // Context menu for connection items (root level)
                        else if ("All Connections".equals(parentValue)) {
                            ContextMenu contextMenu = new ContextMenu();
                            MenuItem cloneItem = new MenuItem("Clone Connection");
                            cloneItem.setOnAction(e -> cloneConnection(item.getValue()));
                            contextMenu.getItems().add(cloneItem);
                            contextMenu.show(cell, event.getScreenX(), event.getScreenY());
                        }
                    }
                });

                return cell;
            });
        }

        // Add search filter functionality for tree
        if (treeSearchField != null) {
            treeSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
                filterTreeView(newValue);
            });
        }

        // Load saved tabs on startup
        loadSavedTabs();
    }

    private void initializeConnectionTree() {
        if (connectionTree != null) {
            // Create root item
            TreeItem<String> rootItem = new TreeItem<>("All Connections");
            rootItem.setExpanded(true);

            // Load existing connections from ConnectionManager
            for (DatabaseConnection conn : ConnectionManager.getInstance().getAllConnections()) {
                TreeItem<String> connectionItem = createConnectionItem(
                    conn.getConnectionName(),
                    conn.getDatabaseType() + " • " + conn.getHost() + ":" + conn.getPort()
                );
                rootItem.getChildren().add(connectionItem);
            }

            connectionTree.setRoot(rootItem);
            connectionTree.setShowRoot(false);

            // Add expand/collapse listener to load data on demand
            connectionTree.setOnMouseClicked(event -> {
                TreeItem<String> selectedItem = connectionTree.getSelectionModel().getSelectedItem();

                if (selectedItem != null) {
                    String itemValue = selectedItem.getValue();

                    // Single click on Tables/Views/Procedures/Functions
                    if (event.getClickCount() == 1) {
                        if ("Tables".equals(itemValue) || "Views".equals(itemValue) ||
                            "Stored Procedures".equals(itemValue) || "Functions".equals(itemValue)) {

                            // Expand the node if collapsed
                            if (!selectedItem.isExpanded()) {
                                selectedItem.setExpanded(true);
                                loadDatabaseObjects(selectedItem);
                            }
                        }
                        // Single click on a table item - load columns
                        else if (selectedItem.getParent() != null &&
                                 "Tables".equals(selectedItem.getParent().getValue())) {
                            if (!selectedItem.isExpanded()) {
                                selectedItem.setExpanded(true);
                                loadTableColumns(selectedItem);
                            }
                        }
                    }
                    // Double click for other actions
                    else if (event.getClickCount() == 2) {
                        if (selectedItem.getParent() != null) {
                            System.out.println("Double-clicked on: " + itemValue);
                            // TODO: Open table/view/procedure details
                        }
                    }
                }
            });
        }
    }

    private TreeItem<String> createConnectionItem(String name, String details) {
        // Create main connection item with database icon
        TreeItem<String> item = new TreeItem<>(name);

        // Add child items for database structure with icons
        TreeItem<String> tablesItem = new TreeItem<>("Tables");
        TreeItem<String> viewsItem = new TreeItem<>("Views");
        TreeItem<String> proceduresItem = new TreeItem<>("Stored Procedures");
        TreeItem<String> functionsItem = new TreeItem<>("Functions");

        item.getChildren().addAll(tablesItem, viewsItem, proceduresItem, functionsItem);

        return item;
    }

    /**
     * Load database objects (tables, views, procedures, functions) dynamically
     */
    private void loadDatabaseObjects(TreeItem<String> selectedItem) {
        String nodeType = selectedItem.getValue();

        // Check if already loaded
        if (!selectedItem.getChildren().isEmpty()) {
            return; // Already loaded
        }

        // Find the parent connection
        TreeItem<String> connectionNode = selectedItem.getParent();
        if (connectionNode == null) return;

        String connectionName = connectionNode.getValue();
        DatabaseConnection dbConnection = ConnectionManager.getInstance().getConnectionByName(connectionName);

        if (dbConnection == null) {
            System.err.println("Connection not found: " + connectionName);
            return;
        }

        // Add loading indicator
        TreeItem<String> loadingItem = new TreeItem<>("Loading...");
        selectedItem.getChildren().add(loadingItem);

        // Load data in background thread
        new Thread(() -> {
            List<String> items = new ArrayList<>();

            try {
                switch (nodeType) {
                    case "Tables":
                        items = com.dbassist.dbassist.service.DatabaseMetadataService.getTables(dbConnection);
                        break;
                    case "Views":
                        items = com.dbassist.dbassist.service.DatabaseMetadataService.getViews(dbConnection);
                        break;
                    case "Stored Procedures":
                        items = com.dbassist.dbassist.service.DatabaseMetadataService.getProcedures(dbConnection);
                        break;
                    case "Functions":
                        items = com.dbassist.dbassist.service.DatabaseMetadataService.getFunctions(dbConnection);
                        break;
                }

                // Update UI on JavaFX thread
                List<String> finalItems = items;
                Platform.runLater(() -> {
                    // Remove loading indicator
                    selectedItem.getChildren().clear();

                    if (finalItems.isEmpty()) {
                        TreeItem<String> emptyItem = new TreeItem<>("(No " + nodeType.toLowerCase() + " found)");
                        selectedItem.getChildren().add(emptyItem);
                    } else {
                        // Add all items
                        for (String itemName : finalItems) {
                            TreeItem<String> item = new TreeItem<>(itemName);
                            selectedItem.getChildren().add(item);
                        }
                        System.out.println("Loaded " + finalItems.size() + " " + nodeType);
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    selectedItem.getChildren().clear();
                    TreeItem<String> errorItem = new TreeItem<>("Error: " + e.getMessage());
                    selectedItem.getChildren().add(errorItem);
                });
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Load columns for a specific table
     */
    private void loadTableColumns(TreeItem<String> tableItem) {
        String tableName = tableItem.getValue();

        // Check if already loaded
        if (!tableItem.getChildren().isEmpty()) {
            return; // Already loaded
        }

        // Find the connection - go up the tree: Table -> Tables -> Connection
        TreeItem<String> tablesNode = tableItem.getParent();
        if (tablesNode == null) return;

        TreeItem<String> connectionNode = tablesNode.getParent();
        if (connectionNode == null) return;

        String connectionName = connectionNode.getValue();
        DatabaseConnection dbConnection = ConnectionManager.getInstance().getConnectionByName(connectionName);

        if (dbConnection == null) {
            System.err.println("Connection not found: " + connectionName);
            return;
        }

        // Add loading indicator
        TreeItem<String> loadingItem = new TreeItem<>("Loading columns...");
        tableItem.getChildren().add(loadingItem);

        // Load columns in background thread
        new Thread(() -> {
            try {
                List<String> columns = DatabaseMetadataService.getTableColumns(dbConnection, tableName);

                // Update UI on JavaFX thread
                Platform.runLater(() -> {
                    // Remove loading indicator
                    tableItem.getChildren().clear();

                    if (columns.isEmpty()) {
                        TreeItem<String> emptyItem = new TreeItem<>("(No columns found)");
                        tableItem.getChildren().add(emptyItem);
                    } else {
                        // Add all columns
                        for (String columnInfo : columns) {
                            TreeItem<String> columnItem = new TreeItem<>(columnInfo);
                            tableItem.getChildren().add(columnItem);
                        }
                        System.out.println("Loaded " + columns.size() + " columns for table: " + tableName);
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    tableItem.getChildren().clear();
                    TreeItem<String> errorItem = new TreeItem<>("Error: " + e.getMessage());
                    tableItem.getChildren().add(errorItem);
                });
                e.printStackTrace();
            }
        }).start();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        // Mark as maximized since app launches in full screen
        this.isMaximized = true;
        setupWindowDragging();
    }

    private void setupWindowDragging() {
        if (titleBar != null && stage != null) {
            titleBar.setOnMousePressed(event -> {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            });

            titleBar.setOnMouseDragged(event -> {
                if (!isMaximized) {
                    stage.setX(event.getScreenX() - xOffset);
                    stage.setY(event.getScreenY() - yOffset);
                }
            });

            titleBar.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    toggleMaximize();
                }
            });
        }
    }

    @FXML
    protected void onMinimize() {
        if (stage != null) {
            stage.setIconified(true);
        }
    }

    @FXML
    protected void onMaximize() {
        toggleMaximize();
    }

    @FXML
    protected void onClose() {
        Platform.exit();
    }

    private void toggleMaximize() {
        if (stage != null) {
            if (!isMaximized) {
                // Save current position and size
                previousX = stage.getX();
                previousY = stage.getY();
                previousWidth = stage.getWidth();
                previousHeight = stage.getHeight();

                // Maximize
                javafx.stage.Screen screen = javafx.stage.Screen.getPrimary();
                javafx.geometry.Rectangle2D bounds = screen.getVisualBounds();
                stage.setX(bounds.getMinX());
                stage.setY(bounds.getMinY());
                stage.setWidth(bounds.getWidth());
                stage.setHeight(bounds.getHeight());
                isMaximized = true;
            } else {
                // Restore
                stage.setX(previousX);
                stage.setY(previousY);
                stage.setWidth(previousWidth);
                stage.setHeight(previousHeight);
                isMaximized = false;
            }
        }
    }

    @FXML
    protected void onNewConnection() {
        System.out.println("New Connection clicked");
        try {
            // Load the new connection dialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource("connection/new-connection-view.fxml"));
            BorderPane page = loader.load();

            // Create the dialog Stage
            Stage dialogStage = new Stage();
            dialogStage.setTitle("New Database Connection");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(stage);
            dialogStage.initStyle(StageStyle.DECORATED);

            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Set the controller
            NewConnectionController controller = loader.getController();
            controller.setDialogStage(dialogStage);

            // Show the dialog and wait until user closes it
            dialogStage.showAndWait();

            // Check if save was clicked
            if (controller.isSaveClicked()) {
                DatabaseConnection connection = controller.getConnection();

                // Save to connection manager
                ConnectionManager.getInstance().addConnection(connection);

                // Add to tree view
                addConnectionToTree(connection);

                System.out.println("Connection saved: " + connection.getConnectionName());
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error opening new connection dialog: " + e.getMessage());
        }
    }

    private void addConnectionToTree(DatabaseConnection connection) {
        if (connectionTree != null && connectionTree.getRoot() != null) {
            String displayName = connection.getConnectionName();
            String details = connection.getDatabaseType() + " • " + connection.getHost() + ":" + connection.getPort();

            TreeItem<String> newConnection = createConnectionItem(displayName, details);
            connectionTree.getRoot().getChildren().add(newConnection);

            // Select and expand the new connection
            connectionTree.getSelectionModel().select(newConnection);
            newConnection.setExpanded(true);
        }
    }

    @FXML
    protected void onRecentConnections() {
        System.out.println("Recent Connections clicked");
        // TODO: Show recent connections
    }

    @FXML
    protected void onSettings() {
        System.out.println("Settings clicked");
        // TODO: Navigate to settings page
    }

    @FXML
    protected void onAbout() {
        System.out.println("About clicked");
        // TODO: Show about dialog
    }

    /**
     * Open a new tab to display table data
     */
    private void openTableDataTab(String tableName) {
        // Find the connection for this table
        TreeItem<String> selectedItem = connectionTree.getSelectionModel().getSelectedItem();
        if (selectedItem == null) return;

        // Navigate up to find connection node
        TreeItem<String> tablesNode = selectedItem.getParent();
        if (tablesNode == null) return;

        TreeItem<String> connectionNode = tablesNode.getParent();
        if (connectionNode == null) return;

        String connectionName = connectionNode.getValue();

        // Create tab configuration
        DataTabConfig tabConfig = new DataTabConfig(connectionName, tableName);

        // Check if tab already exists
        for (Tab tab : mainTabPane.getTabs()) {
            if (tab.getUserData() != null && tab.getUserData() instanceof String) {
                String tabId = (String) tab.getUserData();
                if (tabId.equals(tabConfig.getTabId())) {
                    // Tab already exists, just select it
                    mainTabPane.getSelectionModel().select(tab);
                    return;
                }
            }
        }

        // Create new tab
        Tab dataTab = createDataTab(tabConfig);
        mainTabPane.getTabs().add(dataTab);
        mainTabPane.getSelectionModel().select(dataTab);

        // Save tab configuration
        TabConfigManager.getInstance().addTabConfig(tabConfig);
    }

    /**
     * Create a data tab with close button
     */
    private Tab createDataTab(DataTabConfig tabConfig) {
        Tab tab = new Tab();
        tab.setText(tabConfig.getTableName());
        tab.setUserData(tabConfig.getTabId());

        // Create close button
        Button closeButton = new Button("✕");
        closeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #7f8c8d; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 0 5;");
        closeButton.setOnMouseEntered(e -> closeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 0 5;"));
        closeButton.setOnMouseExited(e -> closeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #7f8c8d; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 0 5;"));
        closeButton.setOnAction(e -> {
            mainTabPane.getTabs().remove(tab);
            TabConfigManager.getInstance().removeTabConfig(tabConfig.getTabId());
        });

        HBox tabHeader = new HBox(8);
        tabHeader.setAlignment(javafx.geometry.Pos.CENTER);
        Label tabLabel = new Label(tabConfig.getTableName());
        tabHeader.getChildren().addAll(tabLabel, closeButton);
        tab.setGraphic(tabHeader);
        tab.setText(null);

        // Create table data grid
        TableDataGrid dataGrid = new TableDataGrid(tabConfig);
        tab.setContent(dataGrid);

        return tab;
    }

    /**
     * Load saved tabs from disk on startup
     */
    private void loadSavedTabs() {
        List<DataTabConfig> savedTabs = TabConfigManager.getInstance().getAllTabConfigs();

        for (DataTabConfig tabConfig : savedTabs) {
            Tab dataTab = createDataTab(tabConfig);
            mainTabPane.getTabs().add(dataTab);
        }

        System.out.println("Loaded " + savedTabs.size() + " saved tabs");
    }

    /**
     * Clone an existing connection
     */
    private void cloneConnection(String connectionName) {
        DatabaseConnection existingConnection = ConnectionManager.getInstance().getConnectionByName(connectionName);

        if (existingConnection == null) {
            System.err.println("Connection not found: " + connectionName);
            return;
        }

        try {
            // Load the new connection dialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource("connection/new-connection-view.fxml"));
            BorderPane page = loader.load();

            // Create the dialog Stage
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Clone Database Connection");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(stage);
            dialogStage.initStyle(StageStyle.DECORATED);

            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Set the controller and pre-fill with existing values
            NewConnectionController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setConnectionToClone(existingConnection);

            // Show the dialog and wait until user closes it
            dialogStage.showAndWait();

            // Check if save was clicked
            if (controller.isSaveClicked()) {
                DatabaseConnection connection = controller.getConnection();

                // Save to connection manager
                ConnectionManager.getInstance().addConnection(connection);

                // Add to tree view
                addConnectionToTree(connection);

                System.out.println("Cloned connection saved: " + connection.getConnectionName());
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error opening clone connection dialog: " + e.getMessage());
        }
    }

    /**
     * Filter tree view based on search text
     */
    private void filterTreeView(String searchText) {
        if (connectionTree == null || connectionTree.getRoot() == null) {
            return;
        }

        String filterText = searchText == null ? "" : searchText.toLowerCase().trim();

        // Rebuild tree with filter
        TreeItem<String> rootItem = connectionTree.getRoot();

        // If search is empty, reload all data
        if (filterText.isEmpty()) {
            initializeConnectionTree();
            return;
        }

        // Create new filtered root
        TreeItem<String> filteredRoot = new TreeItem<>("All Connections");
        filteredRoot.setExpanded(true);

        // Filter through connections
        for (TreeItem<String> connectionItem : rootItem.getChildren()) {
            TreeItem<String> filteredConnection = new TreeItem<>(connectionItem.getValue());
            boolean hasMatchingTables = false;

            // Go through each category (Tables, Views, etc.)
            for (TreeItem<String> categoryItem : connectionItem.getChildren()) {
                if ("Tables".equals(categoryItem.getValue())) {
                    TreeItem<String> filteredTables = new TreeItem<>("Tables");

                    // Filter table items
                    for (TreeItem<String> tableItem : categoryItem.getChildren()) {
                        String tableName = tableItem.getValue().toLowerCase();
                        if (tableName.contains(filterText)) {
                            filteredTables.getChildren().add(new TreeItem<>(tableItem.getValue()));
                            hasMatchingTables = true;
                        }
                    }

                    if (hasMatchingTables) {
                        filteredTables.setExpanded(true);
                        filteredConnection.getChildren().add(filteredTables);
                    }
                }
            }

            if (hasMatchingTables) {
                filteredConnection.setExpanded(true);
                filteredRoot.getChildren().add(filteredConnection);
            }
        }

        connectionTree.setRoot(filteredRoot);
        connectionTree.setShowRoot(false);
    }
}

