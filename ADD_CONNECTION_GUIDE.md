# Add Database Connection - User Guide

## How to Add a New Database Connection

### Step 1: Open New Connection Dialog
1. Click the **"Get Started"** button on the "New Connection" card in the home page, OR
2. Click the **"+"** button in the left Connections panel

### Step 2: Fill Connection Details

#### Required Fields (marked with *)
1. **Connection Name** - A friendly name for your connection
   - Example: "Production SQL Server", "Dev Oracle DB"
   
2. **Database Type** - Select from dropdown
   - SQL Server (Port: 1433)
   - Oracle (Port: 1521)
   - *Port auto-fills based on database type*

3. **Host** - Server hostname or IP address
   - Examples: `localhost`, `192.168.1.100`, `db.mycompany.com`

4. **Port** - Database server port
   - Auto-filled based on database type
   - SQL Server: 1433
   - Oracle: 1521

5. **Username** - Database authentication username

#### Optional Fields
1. **Database Name / SID**
   - SQL Server: Database name (e.g., "MyDatabase")
   - Oracle: SID (e.g., "ORCL")
   - Leave empty to connect to server without specific database

2. **Password** - User password for authentication

3. **Save Password** - Checkbox to save password securely
   - ✓ Checked: Password is saved (Base64 encoded)
   - ✗ Unchecked: You'll need to enter password each time

### Step 3: Test Connection
1. Click **"Test Connection"** button
2. Wait for result:
   - 🔵 Blue "Testing connection..." - In progress
   - ✅ Green "✓ Connection successful!" - Success
   - ❌ Red "✗ Connection failed!" - Failed

**Note:** Test connection validates:
- Network connectivity to server
- Credentials are correct
- Database/SID exists (if provided)
- JDBC driver is working

### Step 4: Save Connection
1. After successful test (or without testing)
2. Click **"Save Connection"** button
3. Connection is:
   - Added to ConnectionManager
   - Saved to disk (`~/.dbassist/connections.dat`)
   - Added to left panel tree view
   - Persisted for future sessions

### Step 5: View in Tree
- New connection appears in left panel
- Expandable tree structure shows:
  - 📁 Tables (placeholder)
  - 📁 Views (placeholder)
  - 📁 Procedures (placeholder)

## Connection Details

### SQL Server Connection String Format
```
jdbc:sqlserver://hostname:1433;databaseName=MyDB;encrypt=true;trustServerCertificate=true
```

### Oracle Connection String Format
```
jdbc:oracle:thin:@hostname:1521:ORCL
```

## Example Configurations

### SQL Server Example
```
Connection Name: Production SQL Server
Database Type: SQL Server
Host: sql.production.com
Port: 1433
Database Name: CustomerDB
Username: app_user
Password: ********
Save Password: ✓
```

### Oracle Example
```
Connection Name: Dev Oracle
Database Type: Oracle
Host: 192.168.1.50
Port: 1521
Database Name/SID: TESTDB
Username: system
Password: ********
Save Password: ✓
```

## Troubleshooting

### Connection Failed - Common Issues

1. **Server Not Reachable**
   - Check if server is running
   - Verify hostname/IP is correct
   - Check firewall settings
   - Ping the server: `ping hostname`

2. **Authentication Failed**
   - Verify username and password
   - Check if user has access to database
   - Ensure account is not locked

3. **Database Not Found**
   - Verify database name/SID is correct
   - Check if database exists on server
   - User may not have access to that database

4. **Port Issue**
   - Verify correct port number
   - Check if port is open: `telnet hostname port`
   - Firewall may be blocking the port

5. **JDBC Driver Issue**
   - Ensure Maven downloaded JDBC drivers
   - Check `pom.xml` has correct driver versions
   - Run `mvn clean install` to refresh dependencies

### Connection Timeout
- Default timeout: 10 seconds
- If server is slow, connection may timeout
- Check network speed and server load

## Security Notes

### Password Storage
- Passwords are Base64 encoded (NOT encrypted)
- Stored in `~/.dbassist/connections.dat`
- **Warning:** This is basic obfuscation, not secure encryption
- Future: Will implement proper encryption

### Best Practices
1. Use read-only accounts when possible
2. Don't save passwords for production databases
3. Use environment-specific connections
4. Regularly rotate passwords
5. Keep `.dbassist` folder secure

## File Storage Location

### Windows
```
C:\Users\YourUsername\.dbassist\connections.dat
```

### Linux/Mac
```
/home/yourusername/.dbassist/connections.dat
```

## Connection Management

### Loading Connections
- Connections are loaded automatically on application start
- From `ConnectionManager.getInstance()`
- Parses `connections.dat` file

### Adding Connections
- `ConnectionManager.addConnection(conn)`
- Automatically saves to file
- Updates tree view

### Removing Connections
- Right-click connection (future feature)
- `ConnectionManager.removeConnection(conn)`

### Editing Connections
- Double-click to edit (future feature)
- Currently: delete and recreate

## Future Features (Roadmap)

- [ ] Edit existing connections
- [ ] Delete connections from tree
- [ ] Connection groups/folders
- [ ] Recent connections list
- [ ] Connection favorites/pins
- [ ] Import/Export connections
- [ ] Encrypted password storage
- [ ] SSH tunnel support
- [ ] SSL/TLS configuration
- [ ] Connection pooling
- [ ] Multiple database selection
- [ ] Connection history log

## API Usage (For Developers)

### Programmatically Add Connection
```java
DatabaseConnection conn = new DatabaseConnection();
conn.setConnectionName("My Connection");
conn.setDatabaseType("SQL Server");
conn.setHost("localhost");
conn.setPort("1433");
conn.setDatabaseName("TestDB");
conn.setUsername("sa");
conn.setPassword("password");
conn.setSavePassword(true);

ConnectionManager.getInstance().addConnection(conn);
```

### Test Connection
```java
boolean success = ConnectionService.testConnection(conn);
if (success) {
    System.out.println("Connected!");
}
```

### Get All Connections
```java
List<DatabaseConnection> connections = 
    ConnectionManager.getInstance().getAllConnections();
```

---

**Need Help?** Check the main README.md or CONNECTION_MODULE.md for more details.

