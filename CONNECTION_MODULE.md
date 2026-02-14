# Connection Module Documentation

## Overview
The Connection Module handles database connection management for DBAssist, including creating, testing, and storing database connections.

## Supported Databases
- **SQL Server** - Microsoft SQL Server (Port: 1433)
- **Oracle** - Oracle Database (Port: 1521)

## Module Structure

### Model Layer
**`DatabaseConnection.java`**
- Represents a database connection configuration
- Properties:
  - Connection Name
  - Database Type (SQL Server/Oracle)
  - Host
  - Port
  - Database Name/SID
  - Username
  - Password
  - Save Password flag

### Service Layer
**`ConnectionService.java`**
- Handles database connection operations
- Methods:
  - `testConnection()` - Test if connection is valid
  - `createConnection()` - Establish database connection
  - `buildJdbcUrl()` - Build JDBC connection string
  - `loadDriver()` - Load appropriate JDBC driver

### Controller Layer
**`NewConnectionController.java`**
- Controls the New Connection dialog
- Features:
  - Form validation
  - Auto-fill default ports
  - Test connection button
  - Save/Cancel actions

### View Layer
**`new-connection-view.fxml`**
- Professional connection form UI
- Sections:
  - Connection Details (Name, Type, Host, Port, Database)
  - Authentication (Username, Password, Save Password)
  - Action Buttons (Test, Save, Cancel)

## Usage

### Opening New Connection Dialog
```java
// From HomeController or any other controller
@FXML
protected void onNewConnection() {
    FXMLLoader loader = new FXMLLoader(
        getClass().getResource("connection/new-connection-view.fxml")
    );
    VBox page = loader.load();
    
    Stage dialogStage = new Stage();
    dialogStage.setTitle("New Database Connection");
    dialogStage.initModality(Modality.WINDOW_MODAL);
    
    Scene scene = new Scene(page);
    dialogStage.setScene(scene);
    
    NewConnectionController controller = loader.getController();
    controller.setDialogStage(dialogStage);
    
    dialogStage.showAndWait();
    
    if (controller.isSaveClicked()) {
        DatabaseConnection connection = controller.getConnection();
        // Use the connection
    }
}
```

### Testing Connection
```java
DatabaseConnection dbConn = new DatabaseConnection();
// Set connection properties...

boolean isValid = ConnectionService.testConnection(dbConn);
if (isValid) {
    System.out.println("Connection successful!");
}
```

### Creating Connection
```java
try {
    Connection conn = ConnectionService.createConnection(dbConn);
    // Use connection for queries
    conn.close();
} catch (SQLException e) {
    System.err.println("Connection failed: " + e.getMessage());
}
```

## JDBC Connection Strings

### SQL Server
```
jdbc:sqlserver://hostname:1433;databaseName=mydb;encrypt=true;trustServerCertificate=true
```

### Oracle
```
jdbc:oracle:thin:@hostname:1521:SID
```

## Dependencies

### Maven Dependencies
```xml
<!-- SQL Server JDBC Driver -->
<dependency>
    <groupId>com.microsoft.sqlserver</groupId>
    <artifactId>mssql-jdbc</artifactId>
    <version>12.4.2.jre11</version>
</dependency>

<!-- Oracle JDBC Driver -->
<dependency>
    <groupId>com.oracle.database.jdbc</groupId>
    <artifactId>ojdbc8</artifactId>
    <version>23.3.0.23.09</version>
</dependency>
```

## Form Validation

### Required Fields
- Connection Name *
- Database Type *
- Host *
- Port *
- Username *

### Optional Fields
- Database Name/SID
- Password
- Save Password

### Validation Rules
- Connection name cannot be empty
- Host must be valid hostname or IP
- Port must be numeric
- Username is required for authentication

## Features

### Auto-Configuration
- Default ports are automatically set based on database type:
  - SQL Server: 1433
  - Oracle: 1521

### Connection Testing
- "Test Connection" button validates credentials
- Shows success/failure status with colored feedback:
  - Blue: Testing...
  - Green: ✓ Connection successful
  - Red: ✗ Connection failed

### Password Security
- Password field is masked
- Optional "Save Password" checkbox
- TODO: Implement secure password storage

## Future Enhancements
- [ ] Add more database types (MySQL, PostgreSQL, MongoDB)
- [ ] Implement secure password encryption
- [ ] Connection pooling
- [ ] Connection history/favorites
- [ ] SSH tunneling support
- [ ] SSL/TLS configuration
- [ ] Advanced connection properties
- [ ] Import/Export connections
- [ ] Connection grouping/tagging

## Error Handling
- JDBC driver not found
- Invalid credentials
- Network timeout
- Database not available
- Invalid connection string

## Integration with Tree View
When a connection is saved, it's automatically added to the left panel connection tree with:
- Connection name and database type
- Expandable nodes for Tables, Views, Procedures

