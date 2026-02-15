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

                // Database connection items (parent level) - Blue-grey
                if (getTreeItem() != null && getTreeItem().getParent() != null
                    && getTreeItem().getParent().getValue() != null
                    && getTreeItem().getParent().getValue().equals("All Connections")) {
                    icon = new FontIcon(FontAwesomeSolid.DATABASE);
                    icon.setIconColor(Color.web("#546e7a")); // Blue-grey 600
                    icon.setIconSize(14);
                }
                // Tables node - Grey
                else if (item.equals("Tables")) {
                    icon = new FontIcon(FontAwesomeSolid.TABLE);
                    icon.setIconColor(Color.web("#757575")); // Grey 600
                    icon.setIconSize(13);
                }
                // Individual table items - Lighter grey
                else if (getTreeItem() != null && getTreeItem().getParent() != null
                    && "Tables".equals(getTreeItem().getParent().getValue())) {
                    icon = new FontIcon(FontAwesomeSolid.TABLE);
                    icon.setIconColor(Color.web("#9e9e9e")); // Grey 500
                    icon.setIconSize(12);
                }
                // Views node - Blue-grey
                else if (item.equals("Views")) {
                    icon = new FontIcon(FontAwesomeSolid.EYE);
                    icon.setIconColor(Color.web("#607d8b")); // Blue-grey 500
                    icon.setIconSize(13);
                }
                // Individual view items
                else if (getTreeItem() != null && getTreeItem().getParent() != null
                    && "Views".equals(getTreeItem().getParent().getValue())) {
                    icon = new FontIcon(FontAwesomeSolid.EYE);
                    icon.setIconColor(Color.web("#90a4ae")); // Blue-grey 300
                    icon.setIconSize(12);
                }
                // Stored Procedures node - Steel blue
                else if (item.contains("Procedures") || item.contains("Stored Procedures")) {
                    icon = new FontIcon(FontAwesomeSolid.COG);
                    icon.setIconColor(Color.web("#78909c")); // Blue-grey 400
                    icon.setIconSize(13);
                }
                // Individual procedure items
                else if (getTreeItem() != null && getTreeItem().getParent() != null
                    && "Stored Procedures".equals(getTreeItem().getParent().getValue())) {
                    icon = new FontIcon(FontAwesomeSolid.COG);
                    icon.setIconColor(Color.web("#90a4ae")); // Blue-grey 300
                    icon.setIconSize(12);
                }
                // Functions node - Slate grey
                else if (item.equals("Functions")) {
                    icon = new FontIcon(FontAwesomeSolid.WRENCH);
                    icon.setIconColor(Color.web("#607d8b")); // Blue-grey 500
                    icon.setIconSize(13);
                }
                // Individual function items
                else if (getTreeItem() != null && getTreeItem().getParent() != null
                    && "Functions".equals(getTreeItem().getParent().getValue())) {
                    icon = new FontIcon(FontAwesomeSolid.WRENCH);
                    icon.setIconColor(Color.web("#90a4ae")); // Blue-grey 300
                    icon.setIconSize(12);
                }
                // Column items - Soft blue
                else if (item.contains("(") && item.contains(")")) {
                    icon = new FontIcon(FontAwesomeSolid.COLUMNS);
                    icon.setIconColor(Color.web("#90caf9")); // Light blue 300
                    icon.setIconSize(11);
                }

                return icon;
            }
        };
    }
}

