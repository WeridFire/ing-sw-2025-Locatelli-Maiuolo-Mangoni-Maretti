package it.polimi.ingsw.view.gui.UIs;

import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.text.TextAlignment;

import java.util.function.BiConsumer;

public class LoginUI {
    private final VBox layout;
    private final TextField usernameField;
    private final CheckBox rmiCheckBox;
    private final Button loginButton;

    public LoginUI(BiConsumer<String, Boolean> onLoginAttempt) {
        Label promptLabel = new Label("Enter your username to launch:");
        promptLabel.setFont(Font.font("Orbitron", FontWeight.BOLD, 18));
        promptLabel.setTextFill(Color.web("#BB86FC"));
        promptLabel.setTextAlignment(TextAlignment.CENTER);
        promptLabel.setEffect(new DropShadow(10, Color.DARKVIOLET));

        usernameField = new TextField();
        usernameField.setPromptText("Astronaut ID");
        usernameField.setStyle("-fx-background-color: #1F1B2E; -fx-text-fill: #EDEDED; -fx-prompt-text-fill: #888;");
        usernameField.setMaxWidth(250);

        rmiCheckBox = new CheckBox("Use RMI (Remote Module Interface)");
        rmiCheckBox.setTextFill(Color.LIGHTGRAY);
        rmiCheckBox.setStyle("-fx-font-size: 13px;");

        loginButton = new Button("Launch");
        loginButton.setFont(Font.font("Orbitron", FontWeight.BOLD, 14));
        loginButton.setTextFill(Color.WHITE);
        loginButton.setStyle("-fx-background-color: linear-gradient(to right, #3f51b5, #673ab7);"
                + "-fx-background-radius: 10;"
                + "-fx-padding: 10 20 10 20;");
        loginButton.setEffect(new InnerShadow(5, Color.BLACK));

        loginButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            boolean useRmi = rmiCheckBox.isSelected();
            if (!username.isEmpty()) {
                onLoginAttempt.accept(username, useRmi);
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "🛰️ Please enter a valid Astronaut ID.");
                alert.showAndWait();
            }
        });

        layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);

        // Background: gradient simile a una galassia
        LinearGradient galaxyBackground = new LinearGradient(
                0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#0F0C29")),
                new Stop(0.5, Color.web("#302B63")),
                new Stop(1, Color.web("#24243E"))
        );
        layout.setBackground(new Background(new BackgroundFill(galaxyBackground, CornerRadii.EMPTY, null)));

        layout.getChildren().addAll(promptLabel, usernameField, rmiCheckBox, loginButton);
    }

    public VBox getLayout() {
        return layout;
    }
}

