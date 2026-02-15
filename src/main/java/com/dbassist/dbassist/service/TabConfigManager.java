package com.dbassist.dbassist.service;

import com.dbassist.dbassist.model.DataTabConfig;
import java.io.*;
import java.util.*;

/**
 * Manages data tab configurations with persistence
 */
public class TabConfigManager {

    private static TabConfigManager instance;
    private List<DataTabConfig> tabConfigs;
    private static final String TABS_FILE = System.getProperty("user.home") + "/.dbassist/tabs.dat";

    private TabConfigManager() {
        tabConfigs = new ArrayList<>();
        loadTabConfigs();
    }

    public static TabConfigManager getInstance() {
        if (instance == null) {
            instance = new TabConfigManager();
        }
        return instance;
    }

    public void addTabConfig(DataTabConfig config) {
        tabConfigs.add(config);
        saveTabConfigs();
    }

    public void removeTabConfig(String tabId) {
        tabConfigs.removeIf(tab -> tab.getTabId().equals(tabId));
        saveTabConfigs();
    }

    public void updateTabConfig(DataTabConfig config) {
        for (int i = 0; i < tabConfigs.size(); i++) {
            if (tabConfigs.get(i).getTabId().equals(config.getTabId())) {
                tabConfigs.set(i, config);
                saveTabConfigs();
                return;
            }
        }
    }

    public List<DataTabConfig> getAllTabConfigs() {
        return new ArrayList<>(tabConfigs);
    }

    public DataTabConfig getTabConfig(String tabId) {
        return tabConfigs.stream()
                .filter(tab -> tab.getTabId().equals(tabId))
                .findFirst()
                .orElse(null);
    }

    private void loadTabConfigs() {
        try {
            File file = new File(TABS_FILE);
            if (!file.exists()) {
                System.out.println("No saved tabs found.");
                return;
            }

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    DataTabConfig config = deserializeTabConfig(line);
                    if (config != null) {
                        tabConfigs.add(config);
                    }
                }
            }
            System.out.println("Loaded " + tabConfigs.size() + " tabs from storage.");
        } catch (IOException e) {
            System.err.println("Error loading tabs: " + e.getMessage());
        }
    }

    private void saveTabConfigs() {
        try {
            File file = new File(TABS_FILE);
            file.getParentFile().mkdirs();

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                for (DataTabConfig config : tabConfigs) {
                    String serialized = serializeTabConfig(config);
                    writer.write(serialized);
                    writer.newLine();
                }
            }
            System.out.println("Saved " + tabConfigs.size() + " tabs to storage.");
        } catch (IOException e) {
            System.err.println("Error saving tabs: " + e.getMessage());
        }
    }

    private String serializeTabConfig(DataTabConfig config) {
        StringBuilder sb = new StringBuilder();
        sb.append(encode(config.getTabId())).append("|");
        sb.append(encode(config.getConnectionName())).append("|");
        sb.append(encode(config.getTableName())).append("|");
        sb.append(config.getMaxRows()).append("|");

        // Serialize filters
        if (config.getColumnFilters().isEmpty()) {
            sb.append("NOFILTERS");
        } else {
            StringBuilder filters = new StringBuilder();
            for (Map.Entry<String, String> entry : config.getColumnFilters().entrySet()) {
                filters.append(encode(entry.getKey())).append("=")
                       .append(encode(entry.getValue())).append(";");
            }
            sb.append(encode(filters.toString()));
        }

        sb.append("|");

        // Serialize column visibility
        if (config.getColumnVisibility().isEmpty()) {
            sb.append("NOVISIBILITY");
        } else {
            StringBuilder visibility = new StringBuilder();
            for (Map.Entry<String, Boolean> entry : config.getColumnVisibility().entrySet()) {
                visibility.append(encode(entry.getKey())).append("=")
                         .append(entry.getValue()).append(";");
            }
            sb.append(encode(visibility.toString()));
        }

        return sb.toString();
    }

    private DataTabConfig deserializeTabConfig(String data) {
        try {
            String[] parts = data.split("\\|");
            if (parts.length < 5) return null;

            DataTabConfig config = new DataTabConfig();
            config.setTabId(decode(parts[0]));
            config.setConnectionName(decode(parts[1]));
            config.setTableName(decode(parts[2]));
            config.setMaxRows(Integer.parseInt(parts[3]));

            // Deserialize filters
            String filtersStr = decode(parts[4]);
            if (!"NOFILTERS".equals(filtersStr) && !filtersStr.isEmpty()) {
                String[] filterPairs = filtersStr.split(";");
                for (String pair : filterPairs) {
                    if (!pair.isEmpty()) {
                        String[] kv = pair.split("=");
                        if (kv.length == 2) {
                            config.addFilter(decode(kv[0]), decode(kv[1]));
                        }
                    }
                }
            }

            // Deserialize column visibility (if present in data)
            if (parts.length >= 6) {
                String visibilityStr = decode(parts[5]);
                if (!"NOVISIBILITY".equals(visibilityStr) && !visibilityStr.isEmpty()) {
                    String[] visibilityPairs = visibilityStr.split(";");
                    for (String pair : visibilityPairs) {
                        if (!pair.isEmpty()) {
                            String[] kv = pair.split("=");
                            if (kv.length == 2) {
                                config.setColumnVisible(decode(kv[0]), Boolean.parseBoolean(kv[1]));
                            }
                        }
                    }
                }
            }

            return config;
        } catch (Exception e) {
            System.err.println("Error deserializing tab config: " + e.getMessage());
            return null;
        }
    }

    private String encode(String str) {
        if (str == null) return "";
        return Base64.getEncoder().encodeToString(str.getBytes());
    }

    private String decode(String str) {
        if (str == null || str.isEmpty()) return "";
        return new String(Base64.getDecoder().decode(str));
    }

    public void clearAll() {
        tabConfigs.clear();
        saveTabConfigs();
    }
}

