# Connection Storage Details

## 📍 Storage Location

### File Path
Connection details are stored in a **file-based system** at:

**Windows:**
```
C:\Users\[YourUsername]\.dbassist\connections.dat
```

**Linux/Mac:**
```
/home/[yourusername]/.dbassist/connections.dat
```

### Code Reference
The file path is defined in `ConnectionManager.java`:
```java
private static final String CONNECTIONS_FILE = 
    System.getProperty("user.home") + "/.dbassist/connections.dat";
```

This uses `System.getProperty("user.home")` which automatically resolves to:
- Windows: `C:\Users\YourUsername`
- Linux: `/home/yourusername`
- Mac: `/Users/yourusername`

## 📦 Storage Format

### File Structure
The file stores one connection per line in a **pipe-separated format** with **Base64 encoding**:

```
connectionName|databaseType|host|port|databaseName|username|password|savePassword
```

Each field is Base64 encoded for basic obfuscation.

### Example (Decoded):
```
NewSQLserver|SQL Server|localhost|1433|NEWDB|sa|password|true
```

### Example (Encoded - actual file content):
```
TmV3U1FMc2VydmVyU1FMIFNlcnZlcg==|bG9jYWxob3N0|MTQzMw==|TkVXREI=|c2E=|cGFzc3dvcmQ=|true
```

## 🔐 Security Information

### Current Implementation
- **Encoding Method:** Base64
- **Security Level:** ⚠️ **BASIC OBFUSCATION ONLY**
- **Encryption:** ❌ NOT ENCRYPTED

### What Base64 Does
- Converts text to encoded string (e.g., "password" → "cGFzc3dvcmQ=")
- **NOT secure encryption** - easily reversible
- Only prevents casual viewing

### Security Warnings
⚠️ **IMPORTANT SECURITY NOTES:**

1. **Passwords are NOT encrypted** - They are only Base64 encoded
2. Anyone with file access can decode the passwords
3. File permissions are important:
   - Windows: Only your user should have access
   - Linux/Mac: File should be chmod 600 (read/write for owner only)

### Decoding Example
You can decode Base64 easily:
```java
String encoded = "cGFzc3dvcmQ=";
String decoded = new String(Base64.getDecoder().decode(encoded));
// Result: "password"
```

## 💾 Storage Operations

### When Connections Are Saved
1. **On Add** - When user clicks "Save Connection"
   ```java
   ConnectionManager.getInstance().addConnection(connection);
   → saveConnections() is called automatically
   ```

2. **On Remove** - When connection is deleted
   ```java
   ConnectionManager.getInstance().removeConnection(connection);
   → saveConnections() is called automatically
   ```

### When Connections Are Loaded
1. **On Application Startup**
   ```java
   ConnectionManager constructor
   → loadConnections() is called
   → Reads from file
   → Deserializes all connections
   → Populates in-memory list
   ```

## 📄 File Management

### Directory Creation
The `.dbassist` directory is created automatically if it doesn't exist:
```java
File file = new File(CONNECTIONS_FILE);
file.getParentFile().mkdirs();  // Creates directory
```

### File Format Details

#### Serialization (Writing)
```java
private String serializeConnection(DatabaseConnection conn) {
    StringBuilder sb = new StringBuilder();
    sb.append(encode(conn.getConnectionName())).append("|");
    sb.append(encode(conn.getDatabaseType())).append("|");
    sb.append(encode(conn.getHost())).append("|");
    sb.append(encode(conn.getPort())).append("|");
    sb.append(encode(conn.getDatabaseName())).append("|");
    sb.append(encode(conn.getUsername())).append("|");
    
    // Only save password if savePassword is true
    if (conn.isSavePassword() && conn.getPassword() != null) {
        sb.append(encode(conn.getPassword()));
    }
    sb.append("|");
    sb.append(conn.isSavePassword());
    
    return sb.toString();
}
```

#### Deserialization (Reading)
```java
private DatabaseConnection deserializeConnection(String data) {
    String[] parts = data.split("\\|");
    
    DatabaseConnection conn = new DatabaseConnection();
    conn.setConnectionName(decode(parts[0]));
    conn.setDatabaseType(decode(parts[1]));
    conn.setHost(decode(parts[2]));
    conn.setPort(decode(parts[3]));
    conn.setDatabaseName(decode(parts[4]));
    conn.setUsername(decode(parts[5]));
    
    if (!parts[6].isEmpty()) {
        conn.setPassword(decode(parts[6]));
    }
    
    conn.setSavePassword(Boolean.parseBoolean(parts[7]));
    
    return conn;
}
```

## 🔍 View Your Stored Connections

### Windows PowerShell
```powershell
# View file location
echo $env:USERPROFILE\.dbassist\connections.dat

# View file contents
Get-Content $env:USERPROFILE\.dbassist\connections.dat

# View with formatting
Get-Content $env:USERPROFILE\.dbassist\connections.dat | ForEach-Object {
    Write-Host "Connection: $_"
}
```

### Linux/Mac Terminal
```bash
# View file location
echo ~/.dbassist/connections.dat

# View file contents
cat ~/.dbassist/connections.dat

# View with line numbers
nl ~/.dbassist/connections.dat
```

### Decode a Stored Password (Example)
**PowerShell:**
```powershell
$encoded = "cGFzc3dvcmQ="
[System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String($encoded))
```

**Linux/Mac:**
```bash
echo "cGFzc3dvcmQ=" | base64 -d
```

## 📊 Current Storage Status

Based on your recent test run, your system has:
- **File:** `C:\Users\[YourUsername]\.dbassist\connections.dat`
- **Size:** 85 bytes
- **Connections:** 1 connection saved
- **Connection Name:** "NewSQLserver"
- **Database Type:** SQL Server
- **Host:** localhost
- **Port:** 1433
- **Database:** NEWDB
- **Username:** sa

## 🛡️ Recommended Security Practices

### Current Setup (Basic)
```
✅ File stored in user's home directory
✅ Not in application directory (portable across sessions)
✅ Base64 encoding (prevents casual viewing)
❌ Not encrypted
❌ File permissions not explicitly set
```

### Recommended Improvements (Future)

1. **Use Proper Encryption**
   - AES-256 encryption for passwords
   - Use Windows Credential Manager (Windows)
   - Use Keychain (Mac)
   - Use Secret Service API (Linux)

2. **Set File Permissions**
   ```java
   // Set file to read/write for owner only
   Files.setPosixFilePermissions(path, 
       PosixFilePermissions.fromString("rw-------"));
   ```

3. **Add Master Password**
   - Encrypt connections file with master password
   - User enters password on app start

4. **Use Environment Variables**
   - Store passwords as environment variables
   - Reference in connection, not store directly

## 📋 Summary

| Aspect | Details |
|--------|---------|
| **Storage Type** | File-based (plain text file) |
| **Location** | `~/.dbassist/connections.dat` |
| **Format** | Pipe-separated Base64 encoded values |
| **Encoding** | Base64 (NOT encryption) |
| **Password Storage** | Optional (user checkbox) |
| **Security Level** | ⚠️ Basic obfuscation only |
| **Auto-load** | ✅ Yes, on app startup |
| **Auto-save** | ✅ Yes, on add/remove |
| **Persistence** | ✅ Across app restarts |

## 🚨 Important Notes

1. **Password Safety**: If you save passwords, they are stored in Base64 (easily decoded)
2. **File Access**: Anyone with access to your user folder can read this file
3. **Backup**: You can backup this file to transfer connections between machines
4. **Delete**: Delete this file to clear all saved connections

## 🔄 Backup/Restore Connections

### Backup
```powershell
# Windows
Copy-Item "$env:USERPROFILE\.dbassist\connections.dat" "D:\Backup\connections.dat"
```

### Restore
```powershell
# Windows
Copy-Item "D:\Backup\connections.dat" "$env:USERPROFILE\.dbassist\connections.dat"
```

---

**Location:** `System.getProperty("user.home") + "/.dbassist/connections.dat"`

**On Your System:** Check `C:\Users\[YourUsername]\.dbassist\connections.dat`

