package com.dbassist.dbassist.service;

import java.util.ArrayList;
import java.util.List;

/**
 * Event manager for connection changes
 * Notifies listeners when connections are added, removed, or modified
 */
public class ConnectionEventManager {

    private static ConnectionEventManager instance;
    private List<ConnectionChangeListener> listeners;

    private ConnectionEventManager() {
        listeners = new ArrayList<>();
    }

    public static ConnectionEventManager getInstance() {
        if (instance == null) {
            instance = new ConnectionEventManager();
        }
        return instance;
    }

    /**
     * Register a listener for connection changes
     */
    public void addListener(ConnectionChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Remove a listener
     */
    public void removeListener(ConnectionChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notify all listeners that a connection was added
     */
    public void notifyConnectionAdded(String connectionName) {
        System.out.println("Broadcasting connection added event: " + connectionName);
        for (ConnectionChangeListener listener : new ArrayList<>(listeners)) {
            try {
                listener.onConnectionAdded(connectionName);
            } catch (Exception e) {
                System.err.println("Error notifying listener: " + e.getMessage());
            }
        }
    }

    /**
     * Notify all listeners that a connection was removed
     */
    public void notifyConnectionRemoved(String connectionName) {
        System.out.println("Broadcasting connection removed event: " + connectionName);
        for (ConnectionChangeListener listener : new ArrayList<>(listeners)) {
            try {
                listener.onConnectionRemoved(connectionName);
            } catch (Exception e) {
                System.err.println("Error notifying listener: " + e.getMessage());
            }
        }
    }

    /**
     * Notify all listeners that a connection was updated
     */
    public void notifyConnectionUpdated(String connectionName) {
        System.out.println("Broadcasting connection updated event: " + connectionName);
        for (ConnectionChangeListener listener : new ArrayList<>(listeners)) {
            try {
                listener.onConnectionUpdated(connectionName);
            } catch (Exception e) {
                System.err.println("Error notifying listener: " + e.getMessage());
            }
        }
    }

    /**
     * Interface for connection change listeners
     */
    public interface ConnectionChangeListener {
        void onConnectionAdded(String connectionName);
        void onConnectionRemoved(String connectionName);
        void onConnectionUpdated(String connectionName);
    }
}

