module com.innovatewithomer.bizora {

    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires org.xerial.sqlitejdbc;

    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;

    requires org.junit.jupiter.api;

    opens com.innovatewithomer.bizora
            to javafx.fxml;

    opens com.innovatewithomer.bizora.controller
            to javafx.fxml;

    opens com.innovatewithomer.bizora.model
            to javafx.base;

    exports com.innovatewithomer.bizora;
    opens com.innovatewithomer.bizora.model.report to javafx.base;
    opens com.innovatewithomer.bizora.repository to javafx.base, org.junit.platform.commons;
}