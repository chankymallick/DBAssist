package com.dbassist.dbassist.components;

import com.dbassist.dbassist.model.DatabaseConnection;
import com.dbassist.dbassist.service.ConnectionManager;
import com.dbassist.dbassist.service.DatabaseMetadataService;
import com.dbassist.dbassist.service.WorksheetManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.sql.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SQL Worksheet with syntax highlighting and auto-completion
 */
public class SqlWorksheet extends BorderPane {

    private final String connectionName;
    private final String worksheetId; // Unique ID for this worksheet
    private final CodeArea codeArea;
    private final Label statusLabel;
    private final TabPane resultsTabPane;
    private final ExecutorService executor;
    private ContextMenu autoCompleteMenu;
    private List<String> tableNames;
    private Map<String, List<String>> tableColumns;
    private QueryResultCallback queryResultCallback;
    private Button openInGridButton;
    private String lastSuccessfulQuery;
    private List<Map<String, Object>> lastResultData;
    private List<String> lastResultColumns;
    private boolean isLoadingContent = false; // Flag to prevent saving while loading

    // SQL Keywords for syntax highlighting
    private static final String[] KEYWORDS = new String[]{
        "SELECT", "FROM", "WHERE", "INSERT", "UPDATE", "DELETE", "CREATE", "DROP", "ALTER",
        "TABLE", "INDEX", "VIEW", "JOIN", "INNER", "LEFT", "RIGHT", "OUTER", "ON", "AS",
        "GROUP", "BY", "ORDER", "HAVING", "DISTINCT", "COUNT", "SUM", "AVG", "MIN", "MAX",
        "AND", "OR", "NOT", "NULL", "IS", "IN", "LIKE", "BETWEEN", "EXISTS", "CASE", "WHEN",
        "THEN", "ELSE", "END", "UNION", "ALL", "TOP", "LIMIT", "OFFSET", "WITH", "VALUES",
        "SET", "INTO", "DEFAULT", "PRIMARY", "KEY", "FOREIGN", "REFERENCES", "CONSTRAINT",
        "UNIQUE", "CHECK", "CASCADE", "BEGIN", "COMMIT", "ROLLBACK", "TRANSACTION"
    };

    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String STRING_PATTERN = "'([^'\\\\]|\\\\.)*'";
    private static final String COMMENT_PATTERN = "--[^\n]*|/\\*.*?\\*/";
    private static final String NUMBER_PATTERN = "\\b\\d+\\.?\\d*\\b";

    private static final Pattern PATTERN = Pattern.compile(
        "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
        + "|(?<STRING>" + STRING_PATTERN + ")"
        + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
        + "|(?<NUMBER>" + NUMBER_PATTERN + ")",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    public interface QueryResultCallback {
        void onQueryResult(String query, List<Map<String, Object>> data, List<String> columns);
    }

    public SqlWorksheet(String connectionName) {
        this(connectionName, null); // Delegate to overloaded constructor
    }

    public SqlWorksheet(String connectionName, String existingWorksheetId) {
        this.connectionName = connectionName;

        // Use existing ID or generate new one
        if (existingWorksheetId != null && !existingWorksheetId.trim().isEmpty()) {
            this.worksheetId = existingWorksheetId;
        } else {
            this.worksheetId = WorksheetManager.getInstance().generateWorksheetId(connectionName);
        }

        this.executor = Executors.newSingleThreadExecutor();
        this.tableNames = new ArrayList<>();
        this.tableColumns = new HashMap<>();

        // Create CodeArea with syntax highlighting
        codeArea = new CodeArea();
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.setStyle(
            "-fx-font-family: 'Consolas', 'Courier New', monospace; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-background-color: #ffffff; " +  // White background
            "-fx-control-inner-background: #ffffff;"  // White inner background
        );

        // Apply syntax highlighting and auto-save on text change
        codeArea.textProperty().addListener((obs, oldText, newText) -> {
            // Apply syntax highlighting
            codeArea.setStyleSpans(0, computeHighlighting(newText));

            // Auto-save content (skip if we're loading saved content)
            if (!isLoadingContent) {
                WorksheetManager.getInstance().saveWorksheet(worksheetId, newText);
            }
        });

        // Setup auto-completion
        setupAutoCompletion();

        // Load metadata for auto-completion
        loadDatabaseMetadata();

        // Load previously saved content
        loadSavedContent();

        // Create toolbar
        HBox toolbar = createToolbar();

        // Create results pane
        resultsTabPane = new TabPane();
        resultsTabPane.setStyle("-fx-background-color: #f5f7fa;");
        resultsTabPane.setMinHeight(200);

        // Status bar
        statusLabel = new Label("Ready");
        statusLabel.setStyle("-fx-padding: 5 10; -fx-background-color: #ecf0f1; -fx-text-fill: #7f8c8d; -fx-font-size: 11px;");

        // Create split pane
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.setDividerPositions(0.6);

        VBox editorPane = new VBox(5);
        editorPane.getChildren().addAll(toolbar, codeArea);
        VBox.setVgrow(codeArea, Priority.ALWAYS);

        splitPane.getItems().addAll(editorPane, resultsTabPane);

        this.setCenter(splitPane);
        this.setBottom(statusLabel);
    }

    private HBox createToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(8));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle("-fx-background-color: #455a64;");

        Button runButton = new Button("▶ Execute");
        runButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 6 15; -fx-font-size: 12px; -fx-background-radius: 4;");
        runButton.setOnAction(e -> executeQuery());

        Button formatButton = new Button("⚡ Format");
        formatButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 6 15; -fx-font-size: 12px; -fx-background-radius: 4;");
        formatButton.setOnAction(e -> formatQuery());

        Button clearButton = new Button("🗑 Clear");
        clearButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 6 15; -fx-font-size: 12px; -fx-background-radius: 4;");
        clearButton.setOnAction(e -> codeArea.clear());

        Button commentButton = new Button("💬 Comment");
        commentButton.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 6 15; -fx-font-size: 12px; -fx-background-radius: 4;");
        commentButton.setOnAction(e -> toggleComment());

        openInGridButton = new Button("📊 Open in Grid");
        openInGridButton.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-cursor: hand; -fx-padding: 6 15; -fx-font-size: 12px; -fx-background-radius: 4;");
        openInGridButton.setDisable(true); // Initially disabled
        openInGridButton.setOnAction(e -> openLastResultInGrid());

        Label connectionLabel = new Label("📁 " + connectionName);
        connectionLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: 600;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        toolbar.getChildren().addAll(runButton, formatButton, commentButton, clearButton, openInGridButton, spacer, connectionLabel);

        return toolbar;
    }

    private void setupAutoCompletion() {
        autoCompleteMenu = new ContextMenu();

        codeArea.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.SPACE && event.isControlDown()) {
                showAutoComplete();
                event.consume();
            } else if (event.getCode() == KeyCode.ENTER && event.isControlDown()) {
                executeQuery();
                event.consume();
            }
        });

        codeArea.textProperty().addListener((obs, oldText, newText) -> {
            int caretPos = codeArea.getCaretPosition();
            if (caretPos > 0) {
                String textBeforeCaret = newText.substring(0, caretPos);
                if (textBeforeCaret.endsWith(".")) {
                    // Show column suggestions for table
                    showColumnSuggestions(textBeforeCaret);
                } else if (shouldShowAutoComplete(textBeforeCaret)) {
                    showAutoComplete();
                }
            }
        });
    }

    private boolean shouldShowAutoComplete(String text) {
        // Show auto-complete after typing 2+ characters of a word
        String[] words = text.split("\\s+");
        if (words.length > 0) {
            String lastWord = words[words.length - 1];
            return lastWord.length() >= 2 && !lastWord.contains(".");
        }
        return false;
    }

    private void showAutoComplete() {
        autoCompleteMenu.getItems().clear();

        int caretPos = codeArea.getCaretPosition();
        String text = codeArea.getText().substring(0, caretPos);
        String[] words = text.split("\\s+");
        String currentWord = words.length > 0 ? words[words.length - 1] : "";

        List<String> suggestions = new ArrayList<>();

        // Add table names
        for (String table : tableNames) {
            if (table.toLowerCase().startsWith(currentWord.toLowerCase())) {
                suggestions.add(table);
            }
        }

        // Add keywords
        for (String keyword : KEYWORDS) {
            if (keyword.toLowerCase().startsWith(currentWord.toLowerCase())) {
                suggestions.add(keyword);
            }
        }

        if (!suggestions.isEmpty()) {
            for (String suggestion : suggestions.subList(0, Math.min(10, suggestions.size()))) {
                MenuItem item = new MenuItem(suggestion);
                item.setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: bold; -fx-font-size: 13px;");
                item.setOnAction(e -> insertSuggestion(suggestion, currentWord.length()));
                autoCompleteMenu.getItems().add(item);
            }

            // Style the context menu itself
            autoCompleteMenu.setStyle("-fx-background-color: white; -fx-border-color: #ccc; -fx-border-width: 1px;");
            autoCompleteMenu.show(codeArea, codeArea.getCaretBounds().get().getMaxX(),
                                  codeArea.getCaretBounds().get().getMaxY());
        }
    }

    private void showColumnSuggestions(String textBeforeCaret) {
        // Extract table name before the dot
        String[] tokens = textBeforeCaret.split("\\s+");
        if (tokens.length > 0) {
            String lastToken = tokens[tokens.length - 1];
            String tableName = lastToken.substring(0, lastToken.length() - 1); // Remove the dot

            List<String> columns = tableColumns.get(tableName.toUpperCase());
            if (columns != null && !columns.isEmpty()) {
                autoCompleteMenu.getItems().clear();
                for (String column : columns) {
                    MenuItem item = new MenuItem(column);
                    item.setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: bold; -fx-font-size: 13px;");
                    item.setOnAction(e -> insertSuggestion(column, 0));
                    autoCompleteMenu.getItems().add(item);
                }
                // Style the context menu
                autoCompleteMenu.setStyle("-fx-background-color: white; -fx-border-color: #ccc; -fx-border-width: 1px;");
                autoCompleteMenu.show(codeArea, codeArea.getCaretBounds().get().getMaxX(),
                                      codeArea.getCaretBounds().get().getMaxY());
            }
        }
    }

    private void insertSuggestion(String suggestion, int replaceLength) {
        int caretPos = codeArea.getCaretPosition();
        codeArea.replaceText(caretPos - replaceLength, caretPos, suggestion);
        autoCompleteMenu.hide();
    }

    private StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();

        while (matcher.find()) {
            String styleClass =
                matcher.group("KEYWORD") != null ? "keyword" :
                matcher.group("STRING") != null ? "string" :
                matcher.group("COMMENT") != null ? "comment" :
                matcher.group("NUMBER") != null ? "number" :
                null;

            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }

        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

    private void loadDatabaseMetadata() {
        DatabaseConnection dbConnection = ConnectionManager.getInstance().getConnectionByName(connectionName);
        if (dbConnection == null) return;

        new Thread(() -> {
            try {
                // Load tables
                tableNames = DatabaseMetadataService.getTables(dbConnection);

                // Load columns for each table
                for (String tableName : tableNames) {
                    List<String> columns = DatabaseMetadataService.getTableColumns(dbConnection, tableName);
                    List<String> columnNames = new ArrayList<>();
                    for (String columnInfo : columns) {
                        // Extract column name (before the hyphen)
                        String[] parts = columnInfo.split(" - ");
                        if (parts.length > 0) {
                            columnNames.add(parts[0]);
                        }
                    }
                    tableColumns.put(tableName.toUpperCase(), columnNames);
                }

                Platform.runLater(() -> updateStatus("Loaded metadata: " + tableNames.size() + " tables"));
            } catch (Exception e) {
                Platform.runLater(() -> updateStatus("Error loading metadata: " + e.getMessage()));
                e.printStackTrace();
            }
        }).start();
    }

    private void executeQuery() {
        String query = codeArea.getSelectedText();
        if (query == null || query.trim().isEmpty()) {
            query = codeArea.getText();
        }

        if (query.trim().isEmpty()) {
            updateStatus("No query to execute");
            return;
        }

        updateStatus("Executing query...");

        final String finalQuery = query.trim();
        DatabaseConnection dbConnection = ConnectionManager.getInstance().getConnectionByName(connectionName);

        if (dbConnection == null) {
            updateStatus("Connection not found: " + connectionName);
            return;
        }

        new Thread(() -> {
            try (Connection conn = getConnection(dbConnection);
                 Statement stmt = conn.createStatement()) {

                boolean isResultSet = stmt.execute(finalQuery);

                if (isResultSet) {
                    // SELECT query - show results
                    ResultSet rs = stmt.getResultSet();
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();

                    List<String> columns = new ArrayList<>();
                    for (int i = 1; i <= columnCount; i++) {
                        columns.add(metaData.getColumnName(i));
                    }

                    List<Map<String, Object>> data = new ArrayList<>();
                    while (rs.next()) {
                        Map<String, Object> row = new LinkedHashMap<>();
                        for (int i = 1; i <= columnCount; i++) {
                            row.put(metaData.getColumnName(i), rs.getObject(i));
                        }
                        data.add(row);
                    }

                    Platform.runLater(() -> {
                        displayResults(finalQuery, data, columns);
                        updateStatus("Query executed successfully. " + data.size() + " rows returned.");

                        // Store results for later grid opening
                        lastSuccessfulQuery = finalQuery;
                        lastResultData = data;
                        lastResultColumns = columns;
                        openInGridButton.setDisable(false); // Enable the button
                    });
                } else {
                    // DML query - show rows affected
                    int updateCount = stmt.getUpdateCount();
                    Platform.runLater(() -> {
                        displayMessage("Query executed successfully. " + updateCount + " row(s) affected.");
                        updateStatus("Query executed successfully. " + updateCount + " row(s) affected.");
                        // Disable grid button for DML queries
                        openInGridButton.setDisable(true);
                    });
                }

            } catch (Exception e) {
                Platform.runLater(() -> {
                    displayError("Error executing query: " + e.getMessage());
                    updateStatus("Error: " + e.getMessage());
                    // Disable button on error
                    openInGridButton.setDisable(true);
                });
                e.printStackTrace();
            }
        }).start();
    }

    private Connection getConnection(DatabaseConnection dbConnection) throws SQLException {
        String url;
        switch (dbConnection.getDatabaseType()) {
            case "SQL Server":
                url = "jdbc:sqlserver://" + dbConnection.getHost() + ":" + dbConnection.getPort() +
                      ";databaseName=" + dbConnection.getDatabaseName() + ";encrypt=false";
                return DriverManager.getConnection(url, dbConnection.getUsername(), dbConnection.getPassword());
            case "Oracle":
                url = "jdbc:oracle:thin:@" + dbConnection.getHost() + ":" + dbConnection.getPort() +
                      ":" + dbConnection.getDatabaseName();
                return DriverManager.getConnection(url, dbConnection.getUsername(), dbConnection.getPassword());
            default:
                throw new SQLException("Unsupported database type: " + dbConnection.getDatabaseType());
        }
    }

    private void displayResults(String query, List<Map<String, Object>> data, List<String> columns) {
        Tab resultTab = new Tab("Results " + (resultsTabPane.getTabs().size() + 1));
        resultTab.setClosable(true);

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        Label queryLabel = new Label("Query: " + (query.length() > 100 ? query.substring(0, 97) + "..." : query));
        queryLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #7f8c8d;");

        TableView<Map<String, Object>> table = new TableView<>();
        table.setStyle("-fx-background-color: white;");

        for (String columnName : columns) {
            TableColumn<Map<String, Object>, String> column = new TableColumn<>(columnName);
            column.setCellValueFactory(cellData -> {
                Object value = cellData.getValue().get(columnName);
                return new javafx.beans.property.SimpleStringProperty(value != null ? value.toString() : "null");
            });
            table.getColumns().add(column);
        }

        table.getItems().addAll(data);

        VBox.setVgrow(table, Priority.ALWAYS);
        content.getChildren().addAll(queryLabel, table);

        resultTab.setContent(content);
        resultsTabPane.getTabs().add(resultTab);
        resultsTabPane.getSelectionModel().select(resultTab);
    }

    private void displayMessage(String message) {
        Tab messageTab = new Tab("Message");
        messageTab.setClosable(true);

        Label label = new Label(message);
        label.setStyle("-fx-font-size: 13px; -fx-padding: 20;");

        messageTab.setContent(label);
        resultsTabPane.getTabs().add(messageTab);
        resultsTabPane.getSelectionModel().select(messageTab);
    }

    private void displayError(String error) {
        Tab errorTab = new Tab("Error");
        errorTab.setClosable(true);

        TextArea textArea = new TextArea(error);
        textArea.setEditable(false);
        textArea.setStyle("-fx-text-fill: #e74c3c; -fx-font-family: monospace;");

        errorTab.setContent(textArea);
        resultsTabPane.getTabs().add(errorTab);
        resultsTabPane.getSelectionModel().select(errorTab);
    }

    private void formatQuery() {
        String query = codeArea.getText();
        if (query.trim().isEmpty()) return;

        // Simple SQL formatting
        String formatted = query
            .replaceAll("(?i)\\bSELECT\\b", "\nSELECT")
            .replaceAll("(?i)\\bFROM\\b", "\nFROM")
            .replaceAll("(?i)\\bWHERE\\b", "\nWHERE")
            .replaceAll("(?i)\\bAND\\b", "\n  AND")
            .replaceAll("(?i)\\bOR\\b", "\n  OR")
            .replaceAll("(?i)\\bORDER BY\\b", "\nORDER BY")
            .replaceAll("(?i)\\bGROUP BY\\b", "\nGROUP BY")
            .replaceAll("(?i)\\bJOIN\\b", "\nJOIN")
            .replaceAll("(?i)\\bINNER JOIN\\b", "\nINNER JOIN")
            .replaceAll("(?i)\\bLEFT JOIN\\b", "\nLEFT JOIN")
            .replaceAll("(?i)\\bRIGHT JOIN\\b", "\nRIGHT JOIN")
            .replaceAll("\\n+", "\n")
            .trim();

        codeArea.replaceText(formatted);
        updateStatus("Query formatted");
    }

    private void toggleComment() {
        int start = codeArea.getSelection().getStart();
        int end = codeArea.getSelection().getEnd();

        if (start == end) {
            // Comment current line
            int lineStart = codeArea.getText().lastIndexOf('\n', start - 1) + 1;
            codeArea.insertText(lineStart, "-- ");
        } else {
            // Comment selection
            String selectedText = codeArea.getSelectedText();
            if (selectedText.startsWith("--")) {
                codeArea.replaceSelection(selectedText.replaceFirst("-- ", ""));
            } else {
                codeArea.replaceSelection("-- " + selectedText);
            }
        }
    }

    private void updateStatus(String message) {
        statusLabel.setText(message);
    }

    /**
     * Load saved content from disk
     */
    private void loadSavedContent() {
        String savedContent = WorksheetManager.getInstance().loadWorksheet(worksheetId);
        if (savedContent != null && !savedContent.trim().isEmpty()) {
            isLoadingContent = true; // Prevent auto-save while loading
            Platform.runLater(() -> {
                codeArea.replaceText(savedContent);
                codeArea.moveTo(0); // Move cursor to beginning
                isLoadingContent = false;
                updateStatus("Loaded saved content");
            });
        }
    }

    /**
     * Open last successful query result in a Table Grid tab
     */
    private void openLastResultInGrid() {
        if (lastResultData != null && lastResultColumns != null && queryResultCallback != null) {
            queryResultCallback.onQueryResult(lastSuccessfulQuery, lastResultData, lastResultColumns);
            updateStatus("Opened result in grid tab");
        }
    }

    public void setQueryResultCallback(QueryResultCallback callback) {
        this.queryResultCallback = callback;
    }

    public String getConnectionName() {
        return connectionName;
    }

    public String getWorksheetId() {
        return worksheetId;
    }

    public void cleanup() {
        // Final save before cleanup
        WorksheetManager.getInstance().saveWorksheet(worksheetId, codeArea.getText());
        executor.shutdown();
    }
}

