package it.polimi.ingsw.view.gui.UIs;

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
    private final Button loginButton;

    public LoginUI(BiConsumer<String, Boolean> onLoginAttempt) {
        Label headerLabel = new Label("GALAXY TRUCKER");
        headerLabel.getStyleClass().add("header-label");

        Label promptLabel = new Label("Enter your call sign:");
        promptLabel.getStyleClass().add("label");

        usernameField = new TextField();
        usernameField.setPromptText("Call Sign");
        usernameField.getStyleClass().add("text-field");
        usernameField.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getText().equals(" ")) change.setText("");
            return change;
        }));

        rmiCheckBox = new CheckBox("Use RMI Hyper-Relay");
        rmiCheckBox.getStyleClass().add("check-box");

        loginButton = new Button("Engage");
        loginButton.getStyleClass().add("button");
        loginButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            boolean useRmi = rmiCheckBox.isSelected();
            if (!username.isEmpty()) {
                onLoginAttempt.accept(username, useRmi);
            } else {
                AlertUtils.showWarning("Invalid Call Sign", "Please enter a valid call sign.");
            }
        });

        layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(headerLabel, promptLabel, usernameField, rmiCheckBox, loginButton);
        layout.getStyleClass().add("root");
        VBox.setVgrow(layout, Priority.ALWAYS); // <--- AGGIUNGI QUESTA LINEA
    }

    public VBox getLayout() {
        return layout;
    }
}
