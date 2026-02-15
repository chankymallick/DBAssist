package com.dbassist.dbassist.service;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Manages persistence of SQL Worksheet content
 */
public class WorksheetManager {

    private static WorksheetManager instance;
    private static final String WORKSHEETS_DIR = System.getProperty("user.home") + "/.dbassist/worksheets/";
    private Map<String, String> worksheetCache; // worksheetId -> content

    private WorksheetManager() {
        worksheetCache = new HashMap<>();
        ensureDirectoryExists();
        loadAllWorksheets();
    }

    public static WorksheetManager getInstance() {
        if (instance == null) {
            instance = new WorksheetManager();
        }
        return instance;
    }

    private void ensureDirectoryExists() {
        try {
            File dir = new File(WORKSHEETS_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
                System.out.println("Created worksheets directory: " + WORKSHEETS_DIR);
            }
        } catch (Exception e) {
            System.err.println("Error creating worksheets directory: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load all existing worksheets from disk
     */
    private void loadAllWorksheets() {
        try {
            File dir = new File(WORKSHEETS_DIR);
            File[] files = dir.listFiles((d, name) -> name.endsWith(".sql"));

            if (files != null) {
                for (File file : files) {
                    String worksheetId = file.getName().replace(".sql", "");
                    String content = readWorksheetFile(file);
                    worksheetCache.put(worksheetId, content);
                }
                System.out.println("Loaded " + worksheetCache.size() + " worksheets from disk");
            }
        } catch (Exception e) {
            System.err.println("Error loading worksheets: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Generate unique worksheet ID for a connection
     */
    public String generateWorksheetId(String connectionName) {
        // Use connection name + timestamp for uniqueness
        String sanitized = connectionName.replaceAll("[^a-zA-Z0-9]", "_");
        return sanitized + "_" + System.currentTimeMillis();
    }

    /**
     * Save worksheet content (called on every text change)
     */
    public void saveWorksheet(String worksheetId, String content) {
        if (worksheetId == null || worksheetId.trim().isEmpty()) {
            return;
        }

        try {
            // Update cache
            worksheetCache.put(worksheetId, content);

            // Save to disk asynchronously
            String filePath = WORKSHEETS_DIR + worksheetId + ".sql";
            new Thread(() -> {
                try {
                    Files.write(Paths.get(filePath), content.getBytes("UTF-8"));
                    // System.out.println("Saved worksheet: " + worksheetId); // Too verbose
                } catch (IOException e) {
                    System.err.println("Error saving worksheet " + worksheetId + ": " + e.getMessage());
                }
            }).start();

        } catch (Exception e) {
            System.err.println("Error in saveWorksheet: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load worksheet content by ID
     */
    public String loadWorksheet(String worksheetId) {
        if (worksheetId == null || worksheetId.trim().isEmpty()) {
            return "";
        }

        // Check cache first
        if (worksheetCache.containsKey(worksheetId)) {
            return worksheetCache.get(worksheetId);
        }

        // Load from disk if not in cache
        try {
            String filePath = WORKSHEETS_DIR + worksheetId + ".sql";
            File file = new File(filePath);

            if (file.exists()) {
                String content = readWorksheetFile(file);
                worksheetCache.put(worksheetId, content);
                return content;
            }
        } catch (Exception e) {
            System.err.println("Error loading worksheet " + worksheetId + ": " + e.getMessage());
        }

        return ""; // Return empty if not found
    }

    /**
     * Delete worksheet
     */
    public void deleteWorksheet(String worksheetId) {
        if (worksheetId == null || worksheetId.trim().isEmpty()) {
            return;
        }

        try {
            // Remove from cache
            worksheetCache.remove(worksheetId);

            // Delete file
            String filePath = WORKSHEETS_DIR + worksheetId + ".sql";
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
                System.out.println("Deleted worksheet: " + worksheetId);
            }
        } catch (Exception e) {
            System.err.println("Error deleting worksheet " + worksheetId + ": " + e.getMessage());
        }
    }

    /**
     * Check if worksheet exists
     */
    public boolean worksheetExists(String worksheetId) {
        if (worksheetId == null || worksheetId.trim().isEmpty()) {
            return false;
        }

        // Check cache
        if (worksheetCache.containsKey(worksheetId)) {
            return true;
        }

        // Check disk
        String filePath = WORKSHEETS_DIR + worksheetId + ".sql";
        return new File(filePath).exists();
    }

    /**
     * Get all worksheet IDs for a connection
     */
    public List<String> getWorksheetsForConnection(String connectionName) {
        List<String> worksheets = new ArrayList<>();
        String prefix = connectionName.replaceAll("[^a-zA-Z0-9]", "_");

        for (String worksheetId : worksheetCache.keySet()) {
            if (worksheetId.startsWith(prefix)) {
                worksheets.add(worksheetId);
            }
        }

        return worksheets;
    }

    /**
     * Clear all worksheets (for cleanup/testing)
     */
    public void clearAll() {
        try {
            File dir = new File(WORKSHEETS_DIR);
            File[] files = dir.listFiles((d, name) -> name.endsWith(".sql"));

            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }

            worksheetCache.clear();
            System.out.println("Cleared all worksheets");
        } catch (Exception e) {
            System.err.println("Error clearing worksheets: " + e.getMessage());
        }
    }

    /**
     * Read worksheet file content
     */
    private String readWorksheetFile(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    /**
     * Get statistics
     */
    public int getWorksheetCount() {
        return worksheetCache.size();
    }

    /**
     * Get total disk usage
     */
    public long getTotalDiskUsage() {
        long total = 0;
        try {
            File dir = new File(WORKSHEETS_DIR);
            File[] files = dir.listFiles((d, name) -> name.endsWith(".sql"));

            if (files != null) {
                for (File file : files) {
                    total += file.length();
                }
            }
        } catch (Exception e) {
            System.err.println("Error calculating disk usage: " + e.getMessage());
        }
        return total;
    }
}

