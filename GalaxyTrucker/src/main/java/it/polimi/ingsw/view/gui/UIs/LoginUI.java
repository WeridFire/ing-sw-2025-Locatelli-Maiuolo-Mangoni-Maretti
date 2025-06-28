package it.polimi.ingsw.view.gui.UIs;

import it.polimi.ingsw.util.Default;
import it.polimi.ingsw.view.gui.managers.ClientManager;
import it.polimi.ingsw.view.gui.utils.AlertUtils;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.BiConsumer;

public class LoginUI {
    private final VBox layout;
    private final TextField usernameField;
    private final CheckBox rmiCheckBox;
    private final TextField serverNameField;
    private final TextField serverPortField;
    private final Button loginButton;

    private String previousServerPortValueRMI = String.valueOf(Default.RMI_PORT);
    private String previousServerPortValueSocket = String.valueOf(Default.SOCKET_PORT);

    public LoginUI() {
        Label headerLabel = new Label("GALAXY TRUCKER");
        headerLabel.getStyleClass().add("header-label");

        Label promptLabel = new Label("Enter your call sign:");
        promptLabel.getStyleClass().add("label");

        // create components
        usernameField = new TextField();
        rmiCheckBox = new CheckBox("Use RMI Hyper-Relay");
        serverNameField = new TextField();
        serverPortField = new TextField();
        loginButton = new Button("Engage");

        // setup components

        usernameField.setPromptText("Call Sign");
        usernameField.getStyleClass().add("text-field");
        usernameField.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getText().equals(" ")) change.setText("");
            return change;
        }));

        rmiCheckBox.getStyleClass().add("check-box");
        rmiCheckBox.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
            if (isNowSelected) {
                previousServerPortValueSocket = serverPortField.getText();
                serverPortField.setText(previousServerPortValueRMI);
            } else {
                previousServerPortValueRMI = serverPortField.getText();
                serverPortField.setText(previousServerPortValueSocket);
            }
        });

        serverNameField.setPromptText("Intergalactic Host Name");
        serverNameField.getStyleClass().add("text-field");
        serverNameField.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getText().equals(" ")) change.setText("");
            return change;
        }));

        serverPortField.setPromptText("Wormhole Server Port");
        serverPortField.getStyleClass().add("text-field");
        serverPortField.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*")) {
                return change;
            }
            return null;
        }));

        loginButton.getStyleClass().add("button");
        loginButton.setOnAction(_ -> attemptLogin());

        // setup default values
        rmiCheckBox.setSelected(Default.USE_RMI);
        serverNameField.setText(Default.HOST);
        serverPortField.setText(Default.USE_RMI ? previousServerPortValueRMI : previousServerPortValueSocket);

        // construct layout

        layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(headerLabel, promptLabel,
                usernameField, rmiCheckBox, serverNameField, serverPortField,
                loginButton);
        layout.getStyleClass().add("root");
        VBox.setVgrow(layout, Priority.ALWAYS);
    }

    public VBox getLayout() {
        return layout;
    }

    private void attemptLogin() {
        // retrieve
        String username = usernameField.getText().trim();
        String hostname = serverNameField.getText().trim();
        String portText = serverPortField.getText().trim();
        boolean useRmi = rmiCheckBox.isSelected();

        // basic validation
        if (username.isEmpty()) {
            AlertUtils.showWarning("Invalid Call Sign", "Please enter a valid call sign.");
            return;
        }

        if (hostname.isEmpty()) {
            AlertUtils.showWarning("Invalid Host", "Please enter a valid server hostname.");
            return;
        }

        // port validation
        int port;
        try {
            port = Integer.parseInt(portText);
            if (port < 1 || port > 65535) {
                AlertUtils.showWarning("Invalid Port", "Please enter a port between 1 and 65535.");
                return;
            }
        } catch (NumberFormatException ex) {
            AlertUtils.showWarning("Invalid Port", "Port must be a numeric value.");
            return;
        }

        // perform connection
        ClientManager.getInstance().attemptConnection(username, useRmi, hostname, port);
    }
}
