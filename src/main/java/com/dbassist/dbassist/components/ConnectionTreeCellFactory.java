package com.dbassist.dbassist.components;

import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;

public class ConnectionTreeCellFactory implements Callback<TreeView<String>, TreeCell<String>> {

    @Override
    public TreeCell<String> call(TreeView<String> param) {
        return new TreeCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);

                    // Add icons based on item type
                    FontIcon icon = getIconForItem(item);
                    if (icon != null) {
                        setGraphic(icon);
                    } else {
                        setGraphic(null);
                    }
                }
            }

            private FontIcon getIconForItem(String item) {
                if (item == null) return null;

                FontIcon icon = null;

                // Database connection items (parent level)
                if (getTreeItem() != null && getTreeItem().getParent() != null
                    && getTreeItem().getParent().getValue() != null
                    && getTreeItem().getParent().getValue().equals("All Connections")) {
                    icon = new FontIcon(FontAwesomeSolid.DATABASE);
                    icon.setIconColor(Color.web("#3498db"));
                    icon.setIconSize(14);
                }
                // Tables node
                else if (item.equals("Tables")) {
                    icon = new FontIcon(FontAwesomeSolid.TABLE);
                    icon.setIconColor(Color.web("#27ae60"));
                    icon.setIconSize(13);
                }
                // Individual table items (children of Tables node)
                else if (getTreeItem() != null && getTreeItem().getParent() != null
                    && "Tables".equals(getTreeItem().getParent().getValue())) {
                    icon = new FontIcon(FontAwesomeSolid.TABLE);
                    icon.setIconColor(Color.web("#16a085"));
                    icon.setIconSize(12);
                }
                // Views node
                else if (item.equals("Views")) {
                    icon = new FontIcon(FontAwesomeSolid.EYE);
                    icon.setIconColor(Color.web("#9b59b6"));
                    icon.setIconSize(13);
                }
                // Individual view items
                else if (getTreeItem() != null && getTreeItem().getParent() != null
                    && "Views".equals(getTreeItem().getParent().getValue())) {
                    icon = new FontIcon(FontAwesomeSolid.EYE);
                    icon.setIconColor(Color.web("#8e44ad"));
                    icon.setIconSize(12);
                }
                // Stored Procedures node
                else if (item.contains("Procedures") || item.contains("Stored Procedures")) {
                    icon = new FontIcon(FontAwesomeSolid.COG);
                    icon.setIconColor(Color.web("#e67e22"));
                    icon.setIconSize(13);
                }
                // Individual procedure items
                else if (getTreeItem() != null && getTreeItem().getParent() != null
                    && "Stored Procedures".equals(getTreeItem().getParent().getValue())) {
                    icon = new FontIcon(FontAwesomeSolid.COG);
                    icon.setIconColor(Color.web("#d35400"));
                    icon.setIconSize(12);
                }
                // Functions node
                else if (item.equals("Functions")) {
                    icon = new FontIcon(FontAwesomeSolid.WRENCH);
                    icon.setIconColor(Color.web("#e74c3c"));
                    icon.setIconSize(13);
                }
                // Individual function items
                else if (getTreeItem() != null && getTreeItem().getParent() != null
                    && "Functions".equals(getTreeItem().getParent().getValue())) {
                    icon = new FontIcon(FontAwesomeSolid.WRENCH);
                    icon.setIconColor(Color.web("#c0392b"));
                    icon.setIconSize(12);
                }
                // Column items (children of table items)
                else if (item.contains("(") && item.contains(")")) {
                    icon = new FontIcon(FontAwesomeSolid.COLUMNS);
                    icon.setIconColor(Color.web("#3498db"));
                    icon.setIconSize(11);
                }

                return icon;
            }
        };
    }
}

