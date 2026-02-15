package com.dbassist.dbassist;

import com.dbassist.dbassist.components.ComparisonResultView;
import com.dbassist.dbassist.components.ConnectionTreeCellFactory;
import com.dbassist.dbassist.components.QueryResultGrid;
import com.dbassist.dbassist.components.SqlWorksheet;
import com.dbassist.dbassist.components.TableDataGrid;
import com.dbassist.dbassist.components.TabSelectionDialog;
import com.dbassist.dbassist.connection.NewConnectionController;
import com.dbassist.dbassist.model.ComparisonResult;
import com.dbassist.dbassist.model.DatabaseConnection;
import com.dbassist.dbassist.model.DataTabConfig;
import com.dbassist.dbassist.service.ConnectionEventManager;
import com.dbassist.dbassist.service.ConnectionManager;
import com.dbassist.dbassist.service.DataComparisonService;
import com.dbassist.dbassist.service.DatabaseMetadataService;
import com.dbassist.dbassist.service.TabConfigManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
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
import java.util.Map;

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

    @FXML
    private HBox progressContainer;

    @FXML
    private Label progressLabel;

    @FXML
    private javafx.scene.control.ProgressBar progressBar;

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
                        ContextMenu contextMenu = new ContextMenu();

                        // Context menu for table items
                        if ("Tables".equals(parentValue)) {
                            MenuItem fetchDataItem = new MenuItem("Fetch Data");
                            fetchDataItem.setOnAction(e -> openTableDataTab(item.getValue()));

                            MenuItem refreshTableItem = new MenuItem("Refresh Table Metadata");
                            refreshTableItem.setOnAction(e -> refreshTableMetadata(item));

                            contextMenu.getItems().addAll(fetchDataItem, refreshTableItem);
                            contextMenu.show(cell, event.getScreenX(), event.getScreenY());
                        }
                        // Context menu for connection items (root level)
                        else if ("All Connections".equals(parentValue)) {
                            MenuItem worksheetItem = new MenuItem("📝 New SQL Worksheet");
                            worksheetItem.setOnAction(e -> openSqlWorksheet(item.getValue()));

                            MenuItem cloneItem = new MenuItem("Clone Connection");
                            cloneItem.setOnAction(e -> cloneConnection(item.getValue()));

                            MenuItem editItem = new MenuItem("Edit Connection");
                            editItem.setOnAction(e -> editConnection(item.getValue()));

                            MenuItem deleteItem = new MenuItem("Delete Connection");
                            deleteItem.setOnAction(e -> deleteConnection(item.getValue()));

                            MenuItem refreshConnectionItem = new MenuItem("Refresh Connection");
                            refreshConnectionItem.setOnAction(e -> refreshConnection(item));

                            MenuItem disconnectItem = new MenuItem("Disconnect");
                            disconnectItem.setOnAction(e -> System.out.println("Disconnect: " + item.getValue()));

                            contextMenu.getItems().addAll(
                                worksheetItem,
                                new SeparatorMenuItem(),
                                cloneItem,
                                editItem,
                                new SeparatorMenuItem(),
                                refreshConnectionItem,
                                disconnectItem,
                                new SeparatorMenuItem(),
                                deleteItem
                            );
                            contextMenu.show(cell, event.getScreenX(), event.getScreenY());
                        }
                        // Context menu for Tables node
                        else if (item.getValue().equals("Tables")) {
                            MenuItem refreshTablesItem = new MenuItem("Refresh Tables List");
                            refreshTablesItem.setOnAction(e -> refreshCategoryNode(item));

                            contextMenu.getItems().add(refreshTablesItem);
                            contextMenu.show(cell, event.getScreenX(), event.getScreenY());
                        }
                        // Context menu for Views node
                        else if (item.getValue().equals("Views")) {
                            MenuItem refreshViewsItem = new MenuItem("Refresh Views List");
                            refreshViewsItem.setOnAction(e -> refreshCategoryNode(item));

                            contextMenu.getItems().add(refreshViewsItem);
                            contextMenu.show(cell, event.getScreenX(), event.getScreenY());
                        }
                        // Context menu for Stored Procedures node
                        else if (item.getValue().equals("Stored Procedures")) {
                            MenuItem refreshProcsItem = new MenuItem("Refresh Procedures List");
                            refreshProcsItem.setOnAction(e -> refreshCategoryNode(item));

                            contextMenu.getItems().add(refreshProcsItem);
                            contextMenu.show(cell, event.getScreenX(), event.getScreenY());
                        }
                        // Context menu for Functions node
                        else if (item.getValue().equals("Functions")) {
                            MenuItem refreshFuncsItem = new MenuItem("Refresh Functions List");
                            refreshFuncsItem.setOnAction(e -> refreshCategoryNode(item));

                            contextMenu.getItems().add(refreshFuncsItem);
                            contextMenu.show(cell, event.getScreenX(), event.getScreenY());
                        }
                        // Context menu for view items
                        else if (item.getParent() != null && "Views".equals(item.getParent().getValue())) {
                            MenuItem viewDataItem = new MenuItem("View Data");
                            viewDataItem.setOnAction(e -> System.out.println("View data: " + item.getValue()));

                            MenuItem refreshViewItem = new MenuItem("Refresh");
                            refreshViewItem.setOnAction(e -> System.out.println("Refresh view: " + item.getValue()));

                            contextMenu.getItems().addAll(viewDataItem, refreshViewItem);
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

            // Add listener for expand/collapse and loading data
            connectionTree.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_CLICKED, event -> {
                TreeItem<String> selectedItem = connectionTree.getSelectionModel().getSelectedItem();

                if (selectedItem != null && event.getClickCount() == 2) {
                    // Double-click handler for tables
                    if (selectedItem.getParent() != null && "Tables".equals(selectedItem.getParent().getValue())) {
                        openTableDataTab(selectedItem.getValue());
                        return;
                    }
                }

                if (selectedItem != null && event.getClickCount() == 1) {
                    String itemValue = selectedItem.getValue();

                    // Load data for Tables/Views/Procedures/Functions when expanded
                    if ("Tables".equals(itemValue) || "Views".equals(itemValue) ||
                        "Stored Procedures".equals(itemValue) || "Functions".equals(itemValue)) {

                        // Check if already loaded
                        if (selectedItem.getChildren().isEmpty() ||
                            (selectedItem.getChildren().size() == 1 &&
                             "Loading...".equals(selectedItem.getChildren().get(0).getValue()))) {
                            loadDatabaseObjects(selectedItem);
                        }
                    }
                    // Load columns for table items
                    else if (selectedItem.getParent() != null &&
                             "Tables".equals(selectedItem.getParent().getValue())) {

                        if (selectedItem.getChildren().isEmpty() ||
                            (selectedItem.getChildren().size() == 1 &&
                             "Loading columns...".equals(selectedItem.getChildren().get(0).getValue()))) {
                            loadTableColumns(selectedItem);
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

        // Show progress
        showProgress("Loading " + nodeType.toLowerCase() + " from " + connectionName + "...");

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

                    // Hide progress
                    hideProgress();
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    selectedItem.getChildren().clear();
                    TreeItem<String> errorItem = new TreeItem<>("Error: " + e.getMessage());
                    selectedItem.getChildren().add(errorItem);

                    // Hide progress
                    hideProgress();
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

        // Show progress
        showProgress("Loading columns for table: " + tableName + "...");

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

                    // Hide progress
                    hideProgress();
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    tableItem.getChildren().clear();
                    TreeItem<String> errorItem = new TreeItem<>("Error: " + e.getMessage());
                    tableItem.getChildren().add(errorItem);

                    // Hide progress
                    hideProgress();
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

                // Broadcast connection added event to all listeners (open tabs)
                ConnectionEventManager.getInstance().notifyConnectionAdded(connection.getConnectionName());

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
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About DBAssist");
        alert.setHeaderText("DBAssist - Your AI-Powered Database Assistant");

        // Create content with developer details
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white;");

        Label versionLabel = new Label("Version 1.0.0");
        versionLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label descriptionLabel = new Label("AI-powered database explorer supporting SQL Server and Oracle databases.");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #546e7a;");

        Separator separator1 = new Separator();

        Label developerTitle = new Label("Developer Information");
        developerTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label developerName = new Label("Developed by: Masaddat Mallick");
        developerName.setStyle("-fx-font-size: 12px; -fx-text-fill: #34495e;");

        Label emailLabel = new Label("Email: masaddat.mallick@gmail.com");
        emailLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #3498db;");

        Label licenseLabel = new Label("License: Open Source");
        licenseLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #27ae60; -fx-font-weight: 600;");

        Separator separator2 = new Separator();

        Label featuresTitle = new Label("Key Features");
        featuresTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        VBox featuresList = new VBox(5);
        String[] features = {
            "✓ Connect to SQL Server and Oracle databases",
            "✓ Browse tables, views, and database objects",
            "✓ Advanced data filtering and column selection",
            "✓ Data comparison between databases",
            "✓ Export comparison reports (Excel, HTML, CSV)",
            "✓ Clone tabs with preserved filters",
            "✓ Connection switching for easy comparison"
        };

        for (String feature : features) {
            Label featureLabel = new Label(feature);
            featureLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #546e7a;");
            featuresList.getChildren().add(featureLabel);
        }

        Separator separator3 = new Separator();

        Label copyrightLabel = new Label("© 2026 DBAssist. All rights reserved.");
        copyrightLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d; -fx-font-style: italic;");

        content.getChildren().addAll(
            versionLabel,
            descriptionLabel,
            separator1,
            developerTitle,
            developerName,
            emailLabel,
            licenseLabel,
            separator2,
            featuresTitle,
            featuresList,
            separator3,
            copyrightLabel
        );

        alert.getDialogPane().setContent(content);
        alert.getDialogPane().setPrefWidth(500);
        alert.showAndWait();
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

        // Show progress
        showProgress("Opening table: " + tableName + "...");

        // Create tab configuration
        DataTabConfig tabConfig = new DataTabConfig(connectionName, tableName);

        // Check if tab already exists
        for (Tab tab : mainTabPane.getTabs()) {
            if (tab.getUserData() != null && tab.getUserData() instanceof String) {
                String tabId = (String) tab.getUserData();
                if (tabId.equals(tabConfig.getTabId())) {
                    // Tab already exists, just select it
                    mainTabPane.getSelectionModel().select(tab);
                    hideProgress();
                    return;
                }
            }
        }

        // Create new tab (this will load data asynchronously)
        Tab dataTab = createDataTab(tabConfig);
        mainTabPane.getTabs().add(dataTab);
        mainTabPane.getSelectionModel().select(dataTab);

        // Save tab configuration
        TabConfigManager.getInstance().addTabConfig(tabConfig);

        // Hide progress after a short delay (data loading happens in TableDataGrid)
        new Thread(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            hideProgress();
        }).start();
    }

    /**
     * Open a new SQL Worksheet tab for the connection
     */
    private void openSqlWorksheet(String connectionName) {
        // Show progress
        showProgress("Opening SQL Worksheet for " + connectionName + "...");

        // Create SQL Worksheet component (it will generate its own ID)
        SqlWorksheet worksheet = new SqlWorksheet(connectionName);

        // Create new worksheet tab
        Tab worksheetTab = new Tab("📝 " + connectionName + " - Worksheet");
        worksheetTab.setClosable(true);
        // Store worksheet ID in UserData for identification
        worksheetTab.setUserData("WORKSHEET:" + worksheet.getWorksheetId());

        // Load CSS for syntax highlighting
        try {
            worksheet.getStylesheets().add(
                getClass().getResource("sql-worksheet.css").toExternalForm()
            );
        } catch (Exception e) {
            System.err.println("Could not load SQL worksheet CSS: " + e.getMessage());
        }

        // Set callback for query results
        worksheet.setQueryResultCallback((query, data, columns) -> {
            // Create a table grid tab for SELECT query results
            createQueryResultTab(connectionName, query, data, columns);
        });

        worksheetTab.setContent(worksheet);

        // Cleanup when tab is closed
        worksheetTab.setOnClosed(e -> {
            worksheet.cleanup();
            System.out.println("Worksheet closed and saved: " + worksheet.getWorksheetId());
        });

        mainTabPane.getTabs().add(worksheetTab);
        mainTabPane.getSelectionModel().select(worksheetTab);

        // Hide progress
        hideProgress();

        System.out.println("Opened worksheet with ID: " + worksheet.getWorksheetId());
    }

    /**
     * Create a table grid tab from SQL query results
     */
    private void createQueryResultTab(String connectionName, String query,
                                      List<Map<String, Object>> data, List<String> columns) {
        // Create a unique tab name
        String tabName = "📊 Query Result - " + connectionName;
        String tabId = "QUERY:" + connectionName + ":" + System.currentTimeMillis();

        // Create tab
        Tab queryTab = new Tab(tabName);
        queryTab.setClosable(true);
        queryTab.setUserData(tabId);

        // Create QueryResultGrid (read-only) instead of TableDataGrid
        QueryResultGrid resultGrid = new QueryResultGrid(connectionName, query, data, columns);

        queryTab.setContent(resultGrid);

        mainTabPane.getTabs().add(queryTab);
        mainTabPane.getSelectionModel().select(queryTab);
    }

    /**
     * Create a data tab with close button
     */
    private Tab createDataTab(DataTabConfig tabConfig) {
        Tab tab = new Tab();
        tab.setText(tabConfig.getDisplayName()); // Now shows: "ConnectionName - TableName"
        tab.setUserData(tabConfig.getTabId());
        tab.setClosable(false); // Disable default close button, we have custom one

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
        Label tabLabel = new Label(tabConfig.getDisplayName()); // Show connection + table
        tabHeader.getChildren().addAll(tabLabel, closeButton);
        tab.setGraphic(tabHeader);
        tab.setText(null);

        // Create table data grid
        TableDataGrid dataGrid = new TableDataGrid(tabConfig);
        dataGrid.setParentTab(tab); // Set reference so grid can update tab name
        dataGrid.setCloneCallback(clonedConfig -> handleCloneTab(clonedConfig)); // Set clone callback
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

                // Broadcast connection added event to all listeners (open tabs)
                ConnectionEventManager.getInstance().notifyConnectionAdded(connection.getConnectionName());

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

    /**
     * Handle Compare Tables button click
     */
    @FXML
    private void onCompare() {
        // Get all data tabs (exclude Home tab, worksheet tabs, and query result tabs)
        java.util.List<TabSelectionDialog.TabInfo> availableTabs = new java.util.ArrayList<>();

        for (Tab tab : mainTabPane.getTabs()) {
            // Skip home tab
            if (tab == homeTab) continue;

            // Skip worksheet tabs (their UserData starts with "WORKSHEET:")
            if (tab.getUserData() != null && tab.getUserData().toString().startsWith("WORKSHEET:")) {
                continue;
            }

            // NOTE: We DO NOT skip QUERY: tabs anymore - they use QueryResultGrid which supports comparison

            // Include both TableDataGrid and QueryResultGrid tabs
            if (tab.getContent() instanceof TableDataGrid) {
                TableDataGrid dataGrid = (TableDataGrid) tab.getContent();
                DataTabConfig tabConfig = dataGrid.getTabConfig();

                java.util.List<String> visibleColumns = dataGrid.getVisibleColumns();
                System.out.println("Tab: " + tabConfig.getDisplayName() +
                                  " - Visible columns: " + visibleColumns.size() +
                                  " - " + visibleColumns);

                TabSelectionDialog.TabInfo tabInfo = new TabSelectionDialog.TabInfo(
                    tabConfig.getTabId(),
                    tabConfig.getConnectionName(),
                    tabConfig.getTableName(),
                    visibleColumns
                );
                availableTabs.add(tabInfo);
            } else if (tab.getContent() instanceof QueryResultGrid) {
                QueryResultGrid resultGrid = (QueryResultGrid) tab.getContent();

                java.util.List<String> visibleColumns = resultGrid.getVisibleColumns();
                System.out.println("Query Result Tab: " + tab.getText() +
                                  " - Visible columns: " + visibleColumns.size() +
                                  " - " + visibleColumns);

                TabSelectionDialog.TabInfo tabInfo = new TabSelectionDialog.TabInfo(
                    tab.getUserData().toString(),
                    resultGrid.getConnectionName(),
                    "Query Result",
                    visibleColumns
                );
                availableTabs.add(tabInfo);
            }
        }

        // Check if we have at least 2 tabs
        if (availableTabs.size() < 2) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Compare Tables");
            alert.setHeaderText("Not enough tabs for comparison");
            alert.setContentText("You need at least 2 table data tabs open to compare.\n\n" +
                "Tip: Open tables by right-clicking them in the tree and selecting 'Fetch Data'.");
            alert.showAndWait();
            return;
        }

        // Show tab selection dialog
        TabSelectionDialog dialog = new TabSelectionDialog(availableTabs);
        java.util.Optional<TabSelectionDialog.ComparisonConfig> result = dialog.showAndWait();

        result.ifPresent(config -> {
            performComparison(config);
        });
    }

    /**
     * Perform the actual comparison
     */
    private void performComparison(TabSelectionDialog.ComparisonConfig config) {
        // Find the actual tabs - can be TableDataGrid or QueryResultGrid
        Object sourceGridObj = null;
        Object targetGridObj = null;
        String sourceTabId = config.getSourceTab().getTabId();
        String targetTabId = config.getTargetTab().getTabId();

        for (Tab tab : mainTabPane.getTabs()) {
            String tabId = tab.getUserData() != null ? tab.getUserData().toString() : null;

            if (tab.getContent() instanceof TableDataGrid) {
                TableDataGrid grid = (TableDataGrid) tab.getContent();
                String gridTabId = grid.getTabConfig().getTabId();

                if (gridTabId.equals(sourceTabId)) {
                    sourceGridObj = grid;
                }
                if (gridTabId.equals(targetTabId)) {
                    targetGridObj = grid;
                }
            } else if (tab.getContent() instanceof QueryResultGrid && tabId != null) {
                QueryResultGrid grid = (QueryResultGrid) tab.getContent();

                if (tabId.equals(sourceTabId)) {
                    sourceGridObj = grid;
                }
                if (tabId.equals(targetTabId)) {
                    targetGridObj = grid;
                }
            }
        }

        if (sourceGridObj == null || targetGridObj == null) {
            showError("Could not find selected tabs for comparison");
            return;
        }

        // Get visible columns and data from both grids (works for both types)
        java.util.List<String> sourceColumns;
        java.util.List<String> targetColumns;
        javafx.collections.ObservableList<Map<String, Object>> sourceData;
        javafx.collections.ObservableList<Map<String, Object>> targetData;

        if (sourceGridObj instanceof TableDataGrid) {
            TableDataGrid grid = (TableDataGrid) sourceGridObj;
            sourceColumns = grid.getVisibleColumns();
            sourceData = grid.getCurrentData();
        } else {
            QueryResultGrid grid = (QueryResultGrid) sourceGridObj;
            sourceColumns = grid.getVisibleColumns();
            sourceData = grid.getCurrentData();
        }

        if (targetGridObj instanceof TableDataGrid) {
            TableDataGrid grid = (TableDataGrid) targetGridObj;
            targetColumns = grid.getVisibleColumns();
            targetData = grid.getCurrentData();
        } else {
            QueryResultGrid grid = (QueryResultGrid) targetGridObj;
            targetColumns = grid.getVisibleColumns();
            targetData = grid.getCurrentData();
        }

        // Find common columns
        java.util.List<String> commonColumns = new java.util.ArrayList<>(sourceColumns);
        commonColumns.retainAll(targetColumns);

        if (commonColumns.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Comparison Error");
            alert.setHeaderText("No common columns");
            alert.setContentText("The selected tabs have no common visible columns to compare.");
            alert.showAndWait();
            return;
        }

        // Get identification columns from user selection
        java.util.List<String> identificationColumns = config.getIdentificationColumns();

        if (identificationColumns.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Comparison Error");
            alert.setHeaderText("No identification columns selected");
            alert.setContentText("Please select at least one column to identify matching rows.");
            alert.showAndWait();
            return;
        }

        // Show progress
        showProgress("Comparing data between " + config.getSourceTab().getTableName() + " and " + config.getTargetTab().getTableName() + "...");

        // Perform comparison in background
        final javafx.collections.ObservableList<Map<String, Object>> finalSourceData = sourceData;
        final javafx.collections.ObservableList<Map<String, Object>> finalTargetData = targetData;

        new Thread(() -> {
            try {
                ComparisonResult compResult = DataComparisonService.compareData(
                    config.getSourceTab().getTableName(),
                    config.getSourceTab().getConnectionName(),
                    config.getTargetTab().getConnectionName(),
                    finalSourceData,
                    finalTargetData,
                    commonColumns,
                    identificationColumns
                );

                // Show result in new tab
                javafx.application.Platform.runLater(() -> {
                    showComparisonResult(compResult);
                    hideProgress();
                });

            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    showError("Error performing comparison: " + e.getMessage());
                    hideProgress();
                });
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Show comparison result in a new tab
     */
    private void showComparisonResult(ComparisonResult result) {
        Tab comparisonTab = new Tab();
        String tabName = "Comparison: " + result.getTableName();
        comparisonTab.setText(tabName);

        // Create close button
        Button closeButton = new Button("✕");
        closeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #7f8c8d; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 0 5;");
        closeButton.setOnMouseEntered(e -> closeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #e74c3c; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 0 5;"));
        closeButton.setOnMouseExited(e -> closeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #7f8c8d; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 0 5;"));
        closeButton.setOnAction(e -> mainTabPane.getTabs().remove(comparisonTab));

        HBox tabHeader = new HBox(8);
        tabHeader.setAlignment(javafx.geometry.Pos.CENTER);
        Label tabLabel = new Label(tabName);
        tabHeader.getChildren().addAll(tabLabel, closeButton);
        comparisonTab.setGraphic(tabHeader);
        comparisonTab.setText(null);
        comparisonTab.setClosable(false);

        // Create comparison view
        ComparisonResultView comparisonView = new ComparisonResultView(result);
        comparisonTab.setContent(comparisonView);

        // Add and select tab
        mainTabPane.getTabs().add(comparisonTab);
        mainTabPane.getSelectionModel().select(comparisonTab);

        System.out.println("Comparison complete: " + result.getSummary().getTotalRows() + " rows compared");
    }

    /**
     * Handle tab clone request from TableDataGrid
     */
    private void handleCloneTab(DataTabConfig clonedConfig) {
        System.out.println("Creating cloned tab: " + clonedConfig.getDisplayName());

        // Create new tab with cloned configuration
        Tab clonedTab = createDataTab(clonedConfig);

        // Add to tab pane
        mainTabPane.getTabs().add(clonedTab);

        // Select the new cloned tab
        mainTabPane.getSelectionModel().select(clonedTab);

        // Save tab configuration
        TabConfigManager.getInstance().addTabConfig(clonedConfig);

        System.out.println("Cloned tab created successfully");
    }

    /**
     * Show error alert
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("An error occurred");
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Refresh entire connection - reload all categories
     */
    private void refreshConnection(TreeItem<String> connectionItem) {
        String connectionName = connectionItem.getValue();
        System.out.println("Refreshing connection: " + connectionName);

        // Clear all children
        connectionItem.getChildren().clear();

        // Recreate structure
        TreeItem<String> tablesItem = new TreeItem<>("Tables");
        TreeItem<String> viewsItem = new TreeItem<>("Views");
        TreeItem<String> proceduresItem = new TreeItem<>("Stored Procedures");
        TreeItem<String> functionsItem = new TreeItem<>("Functions");

        connectionItem.getChildren().addAll(tablesItem, viewsItem, proceduresItem, functionsItem);

        System.out.println("Connection refreshed: " + connectionName);
    }

    /**
     * Refresh category node (Tables, Views, Procedures, Functions)
     */
    private void refreshCategoryNode(TreeItem<String> categoryItem) {
        String categoryName = categoryItem.getValue();
        System.out.println("Refreshing category: " + categoryName);

        // Clear existing children
        categoryItem.getChildren().clear();

        // Reload data
        loadDatabaseObjects(categoryItem);
    }

    /**
     * Refresh single table metadata (columns)
     */
    private void refreshTableMetadata(TreeItem<String> tableItem) {
        String tableName = tableItem.getValue();
        System.out.println("Refreshing table metadata: " + tableName);

        // Clear existing children (columns)
        tableItem.getChildren().clear();

        // Reload columns
        if (tableItem.isExpanded()) {
            loadTableColumns(tableItem);
        }
    }

    /**
     * Edit an existing connection
     */
    private void editConnection(String connectionName) {
        DatabaseConnection existingConnection = ConnectionManager.getInstance().getConnectionByName(connectionName);

        if (existingConnection == null) {
            showError("Connection not found: " + connectionName);
            return;
        }

        try {
            // Load the connection dialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource("connection/new-connection-view.fxml"));
            BorderPane page = loader.load();

            // Create the dialog Stage
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Database Connection");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(stage);
            dialogStage.initStyle(StageStyle.DECORATED);

            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            // Set the controller and pre-fill with existing values
            NewConnectionController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setConnectionToEdit(existingConnection);

            // Show the dialog and wait until user closes it
            dialogStage.showAndWait();

            // Check if save was clicked
            if (controller.isSaveClicked()) {
                DatabaseConnection updatedConnection = controller.getConnection();

                // Update in connection manager
                ConnectionManager.getInstance().updateConnection(connectionName, updatedConnection);

                // Update tree view
                updateConnectionInTree(connectionName, updatedConnection);

                // Broadcast connection updated event
                ConnectionEventManager.getInstance().notifyConnectionUpdated(updatedConnection.getConnectionName());

                System.out.println("Connection updated: " + updatedConnection.getConnectionName());
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error opening edit connection dialog: " + e.getMessage());
        }
    }

    /**
     * Delete an existing connection
     */
    private void deleteConnection(String connectionName) {
        // Confirm deletion
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Connection");
        confirmAlert.setHeaderText("Are you sure you want to delete this connection?");
        confirmAlert.setContentText("Connection: " + connectionName + "\n\nThis action cannot be undone.");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Remove from connection manager
                ConnectionManager.getInstance().removeConnection(connectionName);

                // Remove from tree view
                removeConnectionFromTree(connectionName);

                // Close any tabs using this connection
                closeTabsForConnection(connectionName);

                System.out.println("Connection deleted: " + connectionName);
            }
        });
    }

    /**
     * Update connection in tree view
     */
    private void updateConnectionInTree(String oldConnectionName, DatabaseConnection newConnection) {
        if (connectionTree != null && connectionTree.getRoot() != null) {
            for (TreeItem<String> item : connectionTree.getRoot().getChildren()) {
                if (item.getValue().equals(oldConnectionName)) {
                    item.setValue(newConnection.getConnectionName());
                    // Clear children to force reload
                    item.getChildren().clear();
                    TreeItem<String> tablesItem = new TreeItem<>("Tables");
                    TreeItem<String> viewsItem = new TreeItem<>("Views");
                    TreeItem<String> proceduresItem = new TreeItem<>("Stored Procedures");
                    TreeItem<String> functionsItem = new TreeItem<>("Functions");
                    item.getChildren().addAll(tablesItem, viewsItem, proceduresItem, functionsItem);
                    break;
                }
            }
        }
    }

    /**
     * Remove connection from tree view
     */
    private void removeConnectionFromTree(String connectionName) {
        if (connectionTree != null && connectionTree.getRoot() != null) {
            connectionTree.getRoot().getChildren().removeIf(item -> item.getValue().equals(connectionName));
        }
    }

    /**
     * Close all tabs using a specific connection
     */
    private void closeTabsForConnection(String connectionName) {
        List<Tab> tabsToRemove = new ArrayList<>();
        for (Tab tab : mainTabPane.getTabs()) {
            if (tab != homeTab && tab.getContent() instanceof TableDataGrid) {
                TableDataGrid grid = (TableDataGrid) tab.getContent();
                if (grid.getTabConfig().getConnectionName().equals(connectionName)) {
                    tabsToRemove.add(tab);
                }
            }
        }

        // Remove tabs and their configurations
        for (Tab tab : tabsToRemove) {
            String tabId = (String) tab.getUserData();
            mainTabPane.getTabs().remove(tab);
            TabConfigManager.getInstance().removeTabConfig(tabId);
        }

        if (!tabsToRemove.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Tabs Closed");
            alert.setHeaderText("Related tabs have been closed");
            alert.setContentText(tabsToRemove.size() + " tab(s) using this connection were closed.");
            alert.showAndWait();
        }
    }

    /**
     * Show progress bar with message
     */
    private void showProgress(String message) {
        Platform.runLater(() -> {
            if (progressContainer != null && progressLabel != null && progressBar != null) {
                progressLabel.setText(message);
                progressBar.setProgress(-1); // Indeterminate progress
                progressContainer.setVisible(true);
                progressContainer.setManaged(true);
            }
        });
    }

    /**
     * Hide progress bar
     */
    private void hideProgress() {
        Platform.runLater(() -> {
            if (progressContainer != null) {
                progressContainer.setVisible(false);
                progressContainer.setManaged(false);
            }
        });
    }
}

