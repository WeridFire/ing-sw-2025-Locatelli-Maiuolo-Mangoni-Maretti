package it.polimi.ingsw.view.gui.UIs;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.effect.DropShadow;

public class LobbyUI {
    private final VBox layout;

    public LobbyUI(String username) {
        Label statusLabel = new Label("Welcome Commander " + username + "!");
        statusLabel.setFont(Font.font("Orbitron", FontWeight.BOLD, 20));
        statusLabel.setTextFill(Color.web("#BB86FC"));
        statusLabel.setEffect(new DropShadow(8, Color.DARKVIOLET));

        layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setPrefSize(400, 300);

        // Background spaziale
        LinearGradient galaxyBackground = new LinearGradient(
                0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#0F0C29")),
                new Stop(0.5, Color.web("#302B63")),
                new Stop(1, Color.web("#24243E"))
        );
        layout.setBackground(new Background(new BackgroundFill(galaxyBackground, CornerRadii.EMPTY, null)));

        layout.getChildren().add(statusLabel);
    }

    public VBox getLayout() {
        return layout;
    }
}
