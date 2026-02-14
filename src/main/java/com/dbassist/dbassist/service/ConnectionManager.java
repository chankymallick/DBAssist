package com.dbassist.dbassist.service;

import com.dbassist.dbassist.model.DatabaseConnection;
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Base64;

/**
 * Manages the list of database connections with file-based persistence
 */
public class ConnectionManager {

    private static ConnectionManager instance;
    private List<DatabaseConnection> connections;
    private static final String CONNECTIONS_FILE = System.getProperty("user.home") + "/.dbassist/connections.dat";

    private ConnectionManager() {
        connections = new ArrayList<>();
        loadConnections();
    }

    public static ConnectionManager getInstance() {
        if (instance == null) {
            instance = new ConnectionManager();
        }
        return instance;
    }

    /**
     * Add a new connection
     */
    public void addConnection(DatabaseConnection connection) {
        connections.add(connection);
        saveConnections();
    }

    /**
     * Remove a connection
     */
    public void removeConnection(DatabaseConnection connection) {
        connections.remove(connection);
        saveConnections();
    }

    /**
     * Get all connections
     */
    public List<DatabaseConnection> getAllConnections() {
        return new ArrayList<>(connections);
    }

    /**
     * Find connection by name
     */
    public DatabaseConnection getConnectionByName(String name) {
        return connections.stream()
                .filter(c -> c.getConnectionName().equals(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * Load connections from storage
     */
    private void loadConnections() {
        try {
            File file = new File(CONNECTIONS_FILE);
            if (!file.exists()) {
                System.out.println("No saved connections found.");
                return;
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    DatabaseConnection conn = deserializeConnection(line);
                    if (conn != null) {
                        connections.add(conn);
                    }
                }
            }
            System.out.println("Loaded " + connections.size() + " connections from storage.");
        } catch (IOException e) {
            System.err.println("Error loading connections: " + e.getMessage());
        }
    }

    /**
     * Save connections to storage
     */
    private void saveConnections() {
        try {
            // Create directory if it doesn't exist
            File file = new File(CONNECTIONS_FILE);
            file.getParentFile().mkdirs();

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                for (DatabaseConnection conn : connections) {
                    String serialized = serializeConnection(conn);
                    writer.write(serialized);
                    writer.newLine();
                }
            }
            System.out.println("Saved " + connections.size() + " connections to storage.");
        } catch (IOException e) {
            System.err.println("Error saving connections: " + e.getMessage());
        }
    }

    /**
     * Serialize connection to string (simple format)
     */
    private String serializeConnection(DatabaseConnection conn) {
        // Format: name|type|host|port|dbname|username|password|savePassword
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

    /**
     * Deserialize connection from string
     */
    private DatabaseConnection deserializeConnection(String data) {
        try {
            String[] parts = data.split("\\|");
            if (parts.length < 8) return null;

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
        } catch (Exception e) {
            System.err.println("Error deserializing connection: " + e.getMessage());
            return null;
        }
    }

    /**
     * Encode string to Base64
     */
    private String encode(String str) {
        if (str == null) return "";
        return Base64.getEncoder().encodeToString(str.getBytes());
    }

    /**
     * Decode string from Base64
     */
    private String decode(String str) {
        if (str == null || str.isEmpty()) return "";
        return new String(Base64.getDecoder().decode(str));
    }

    /**
     * Clear all connections
     */
    public void clearAll() {
        connections.clear();
        saveConnections();
    }

    /**
     * Get connection count
     */
    public int getConnectionCount() {
        return connections.size();
    }
}

