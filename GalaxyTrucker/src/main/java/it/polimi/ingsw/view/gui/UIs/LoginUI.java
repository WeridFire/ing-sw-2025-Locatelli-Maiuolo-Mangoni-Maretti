package it.polimi.ingsw.view.gui.UIs;

import it.polimi.ingsw.util.Default;
import it.polimi.ingsw.view.gui.managers.ClientManager;
import it.polimi.ingsw.view.gui.utils.AlertUtils;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.function.BiConsumer;

/**
 * User interface for the login screen where users enter their credentials and connection settings.
 * This class provides input fields for username, server hostname, port, and connection type (RMI/Socket).
 * It validates user input and initiates the connection process through the ClientManager.
 */
public class LoginUI {
    /**
     * The main layout container for all UI components.
     */
    private final VBox layout;
    /**
     * Text field for entering the username/call sign.
     */
    private final TextField usernameField;
    /**
     * Checkbox to toggle between RMI and Socket connection types.
     */
    private final CheckBox rmiCheckBox;
    /**
     * Text field for entering the server hostname.
     */
    private final TextField serverNameField;
    /**
     * Text field for entering the server port number.
     */
    private final TextField serverPortField;
    /**
     * Button to initiate the login/connection process.
     */
    private final Button loginButton;

    /**
     * Stores the previously entered RMI port value for restoration when switching connection types.
     */
    private String previousServerPortValueRMI = String.valueOf(Default.RMI_PORT);
    /**
     * Stores the previously entered Socket port value for restoration when switching connection types.
     */
    private String previousServerPortValueSocket = String.valueOf(Default.SOCKET_PORT);

    /**
     * Constructs the login UI with all necessary components and event handlers.
     * Sets up form validation, default values, and styling.
     */
    public LoginUI() {
        Label headerLabel = new Label("GALAXY TRUCKER");
        headerLabel.getStyleClass().add("header-label");

        Label promptLabel = new Label("Enter your call sign:");
        promptLabel.getStyleClass().add("label");

        // create components
        usernameField = new TextField();
        rmiCheckBox = new CheckBox("Use RMI Hyper-Relay");
        serverNameField = new TextField();
        serverNameField.setMaxWidth(200);
        serverPortField = new TextField();
        serverPortField.setMaxWidth(200);
        loginButton = new Button("Engage");

        // setup components

        usernameField.setPromptText("Call Sign");
        usernameField.getStyleClass().add("text-field");
        usernameField.setMaxWidth(200);
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

    /**
     * Returns the main layout container for this UI.
     * @return The VBox containing all UI components.
     */
    public VBox getLayout() {
        return layout;
    }

    /**
     * Validates user input and attempts to establish a connection to the server.
     * Performs validation on username, hostname, and port before delegating to ClientManager.
     * Shows appropriate error messages for invalid input.
     */
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
