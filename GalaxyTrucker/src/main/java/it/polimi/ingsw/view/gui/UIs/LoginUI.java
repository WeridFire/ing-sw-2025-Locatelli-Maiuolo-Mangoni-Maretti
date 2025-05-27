package it.polimi.ingsw.view.gui.UIs;

import it.polimi.ingsw.view.gui.managers.ClientManager;
import it.polimi.ingsw.view.gui.utils.AlertUtils;
import javafx.geometry.Pos;
import javafx.scene.control.*;
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
        // avoid spaces in the username
        usernameField.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getText().equals(" ")) change.setText("");
            return change;
        }));

        rmiCheckBox = new CheckBox("Use RMI");

        loginButton = new Button("Login");
        loginButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            boolean useRmi = rmiCheckBox.isSelected();
            if (!username.isEmpty()) {
                onLoginAttempt.accept(username, useRmi);
            } else {
                AlertUtils.showWarning("Empty Username", "Please enter a valid username.");
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
