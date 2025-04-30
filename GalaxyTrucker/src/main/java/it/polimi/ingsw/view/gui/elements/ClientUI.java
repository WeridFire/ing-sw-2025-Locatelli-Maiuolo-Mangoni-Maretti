package it.polimi.ingsw.view.gui.elements;

import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public class ClientUI {
    private final VBox layout;

    public ClientUI(Consumer<String> onUsernameConfirmed) {
        Label promptLabel = new Label("Enter your username:");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        Button confirmButton = new Button("Confirm");
        confirmButton.setOnAction(_ -> {
            String username = usernameField.getText().trim();
            if (!username.isEmpty()) {
                onUsernameConfirmed.accept(username);
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please enter a valid username.");
                alert.showAndWait();
            }
        });

        layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(promptLabel, usernameField, confirmButton);
    }

    public VBox getLayout() {
        return layout;
    }
}
