package com.dbassist.dbassist.connection;

import com.dbassist.dbassist.model.DatabaseConnection;
import com.dbassist.dbassist.service.ConnectionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class NewConnectionController {

    @FXML
    private TextField connectionNameField;

    @FXML
    private ComboBox<String> databaseTypeCombo;

    @FXML
    private TextField hostField;

    @FXML
    private TextField portField;

    @FXML
    private TextField databaseNameField;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private CheckBox savePasswordCheckbox;

    @FXML
    private Button testConnectionButton;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Label statusLabel;

    private Stage dialogStage;
    private DatabaseConnection connection;
    private boolean saveClicked = false;
    private DatabaseConnection connectionToClone = null;

    @FXML
    public void initialize() {
        // Initialize database type combo box
        databaseTypeCombo.getItems().addAll("SQL Server", "Oracle");
        databaseTypeCombo.setValue("SQL Server");

        // Set default ports based on database type
        databaseTypeCombo.setOnAction(event -> updateDefaultPort());

        // Set initial default port
        updateDefaultPort();

        // Add validation listeners
        connectionNameField.textProperty().addListener((obs, old, newVal) -> validateForm());
        hostField.textProperty().addListener((obs, old, newVal) -> validateForm());
        usernameField.textProperty().addListener((obs, old, newVal) -> validateForm());
    }

    private void updateDefaultPort() {
        String dbType = databaseTypeCombo.getValue();
        if ("SQL Server".equals(dbType)) {
            portField.setText("1433");
        } else if ("Oracle".equals(dbType)) {
            portField.setText("1521");
        }
    }

    private void validateForm() {
        boolean valid = !connectionNameField.getText().trim().isEmpty()
                     && !hostField.getText().trim().isEmpty()
                     && !usernameField.getText().trim().isEmpty();

        saveButton.setDisable(!valid);
        testConnectionButton.setDisable(!valid);
    }

    @FXML
    private void handleTestConnection() {
        statusLabel.setStyle("-fx-text-fill: #3498db;");
        statusLabel.setText("Testing connection...");

        // Create temporary connection object for testing
        DatabaseConnection testConn = new DatabaseConnection();
        testConn.setDatabaseType(databaseTypeCombo.getValue());
        testConn.setHost(hostField.getText());
        testConn.setPort(portField.getText());
        testConn.setDatabaseName(databaseNameField.getText());
        testConn.setUsername(usernameField.getText());
        testConn.setPassword(passwordField.getText());

        // Test connection in background thread
        new Thread(() -> {
            try {
                boolean success = com.dbassist.dbassist.service.ConnectionService.testConnection(testConn);

                javafx.application.Platform.runLater(() -> {
                    if (success) {
                        statusLabel.setStyle("-fx-text-fill: #27ae60;");
                        statusLabel.setText("✓ Connection successful!");
                    } else {
                        statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                        statusLabel.setText("✗ Connection failed! Check your credentials and server.");
                    }
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    statusLabel.setStyle("-fx-text-fill: #e74c3c;");
                    statusLabel.setText("✗ Error: " + e.getMessage());
                });
            }
        }).start();
    }

    @FXML
    private void handleSave() {
        if (isInputValid()) {
            connection = new DatabaseConnection();
            connection.setConnectionName(connectionNameField.getText());
            connection.setDatabaseType(databaseTypeCombo.getValue());
            connection.setHost(hostField.getText());
            connection.setPort(portField.getText());
            connection.setDatabaseName(databaseNameField.getText());
            connection.setUsername(usernameField.getText());
            connection.setPassword(passwordField.getText());
            connection.setSavePassword(savePasswordCheckbox.isSelected());

            saveClicked = true;
            dialogStage.close();
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private boolean isInputValid() {
        String errorMessage = "";

        if (connectionNameField.getText() == null || connectionNameField.getText().trim().isEmpty()) {
            errorMessage += "Connection name is required!\n";
        } else {
            // Check for duplicate connection name
            String newConnectionName = connectionNameField.getText().trim();
            DatabaseConnection existingConn = ConnectionManager.getInstance().getConnectionByName(newConnectionName);

            // Allow same name only if we're cloning and keeping the same name (which we shouldn't, but check anyway)
            if (existingConn != null) {
                // If cloning, check if it's a different connection
                if (connectionToClone == null || !newConnectionName.equals(connectionToClone.getConnectionName())) {
                    errorMessage += "Connection name '" + newConnectionName + "' already exists!\nPlease choose a different name.\n";
                }
            }
        }

        if (hostField.getText() == null || hostField.getText().trim().isEmpty()) {
            errorMessage += "Host is required!\n";
        }
        if (portField.getText() == null || portField.getText().trim().isEmpty()) {
            errorMessage += "Port is required!\n";
        }
        if (usernameField.getText() == null || usernameField.getText().trim().isEmpty()) {
            errorMessage += "Username is required!\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Invalid Fields");
            alert.setHeaderText("Please correct invalid fields");
            alert.setContentText(errorMessage);
            alert.showAndWait();
            return false;
        }
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public boolean isSaveClicked() {
        return saveClicked;
    }

    public DatabaseConnection getConnection() {
        return connection;
    }

    /**
     * Set connection to clone - pre-fills form with existing values
     */
    public void setConnectionToClone(DatabaseConnection existingConnection) {
        this.connectionToClone = existingConnection;

        // Pre-fill form with cloned values
        connectionNameField.setText(existingConnection.getConnectionName() + " (Copy)");
        databaseTypeCombo.setValue(existingConnection.getDatabaseType());
        hostField.setText(existingConnection.getHost());
        portField.setText(existingConnection.getPort());
        databaseNameField.setText(existingConnection.getDatabaseName());
        usernameField.setText(existingConnection.getUsername());
        passwordField.setText(existingConnection.getPassword());
        savePasswordCheckbox.setSelected(existingConnection.isSavePassword());

        // Select the connection name text for easy editing
        connectionNameField.selectAll();
        connectionNameField.requestFocus();
    }

    /**
     * Set connection to edit - pre-fills form with existing values for editing
     */
    public void setConnectionToEdit(DatabaseConnection existingConnection) {
        this.connectionToClone = existingConnection; // Use same field to allow name validation

        // Pre-fill form with existing values
        connectionNameField.setText(existingConnection.getConnectionName());
        databaseTypeCombo.setValue(existingConnection.getDatabaseType());
        hostField.setText(existingConnection.getHost());
        portField.setText(existingConnection.getPort());
        databaseNameField.setText(existingConnection.getDatabaseName());
        usernameField.setText(existingConnection.getUsername());
        passwordField.setText(existingConnection.getPassword());
        savePasswordCheckbox.setSelected(existingConnection.isSavePassword());

        // Focus on the first field
        hostField.requestFocus();
    }
}

