module com.fmad.galaxytrucker {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;

    opens com.fmad.galaxytrucker to javafx.fxml;
    exports com.fmad.galaxytrucker;
}