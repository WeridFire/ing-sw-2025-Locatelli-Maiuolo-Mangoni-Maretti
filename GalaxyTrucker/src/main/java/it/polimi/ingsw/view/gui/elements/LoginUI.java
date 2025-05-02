package it.polimi.ingsw.view.gui.elements;

import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.function.BiConsumer;

public class LoginUI {
    private final VBox layout;
    private final TextField usernameField;
    private final CheckBox rmiCheckBox;
    private final Button loginButton;

    public LoginUI(BiConsumer<String, Boolean> onLoginAttempt) {
        Label promptLabel = new Label("Enter your username:");
        usernameField = new TextField();
        usernameField.setPromptText("Username");

        rmiCheckBox = new CheckBox("Use RMI");

        loginButton = new Button("Login");
        loginButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            boolean useRmi = rmiCheckBox.isSelected();
            if (!username.isEmpty()) {
                onLoginAttempt.accept(username, useRmi);
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please enter a valid username.");
                alert.showAndWait();
            }
        });

        layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(promptLabel, usernameField, rmiCheckBox, loginButton);
    }

    public VBox getLayout() {
        return layout;
    }
}
