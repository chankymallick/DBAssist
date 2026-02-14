module com.dbassist.dbassist {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;
    requires java.sql;

    opens com.dbassist.dbassist to javafx.fxml;
    opens com.dbassist.dbassist.connection to javafx.fxml;

    exports com.dbassist.dbassist;
    exports com.dbassist.dbassist.components;
    exports com.dbassist.dbassist.connection;
    exports com.dbassist.dbassist.model;
    exports com.dbassist.dbassist.service;
}