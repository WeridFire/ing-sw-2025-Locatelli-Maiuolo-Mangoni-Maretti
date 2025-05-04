package it.polimi.ingsw.view.gui.UIs;

import it.polimi.ingsw.view.gui.managers.ClientManager;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;
import java.util.function.Consumer;

public class JoinGameUI {
    private final VBox layout;
    private final TextField gameUUIDField;
    private final Button joinGameButton;
    private final ListView<String> activeGamesList;

    public JoinGameUI(Consumer<String> onJoinGame, List<String> activeGames) {
        Label uuidLabel = new Label("Enter mission UUID:");
        uuidLabel.setFont(Font.font("Orbitron", FontWeight.BOLD, 16));
        uuidLabel.setTextFill(Color.web("#BB86FC"));
        uuidLabel.setEffect(new DropShadow(5, Color.DARKVIOLET));

        gameUUIDField = new TextField();
        gameUUIDField.setPromptText("Mission UUID");
        gameUUIDField.setStyle("-fx-background-color: #1F1B2E; -fx-text-fill: #EDEDED; -fx-prompt-text-fill: #888;");
        gameUUIDField.setMaxWidth(250);

        Label activeGamesLabel = new Label("Active Missions:");
        activeGamesLabel.setFont(Font.font("Orbitron", FontWeight.BOLD, 14));
        activeGamesLabel.setTextFill(Color.web("#BB86FC"));
        activeGamesLabel.setEffect(new DropShadow(3, Color.DARKVIOLET));

        activeGamesList = new ListView<>(FXCollections.observableArrayList(activeGames));
        activeGamesList.setMaxHeight(150);
        activeGamesList.setStyle("-fx-background-color: #1F1B2E; -fx-control-inner-background: #1F1B2E; -fx-text-fill: white;");

        activeGamesList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                gameUUIDField.setText(newVal);
            }
        });

        joinGameButton = new Button("Join Mission");
        joinGameButton.setFont(Font.font("Orbitron", FontWeight.BOLD, 14));
        joinGameButton.setTextFill(Color.WHITE);
        joinGameButton.setStyle("-fx-background-color: linear-gradient(to right, #3f51b5, #673ab7);"
                + "-fx-background-radius: 10;"
                + "-fx-padding: 8 18 8 18;");
        joinGameButton.setEffect(new DropShadow(5, Color.BLACK));

        joinGameButton.setOnAction(e -> {
            String uuid = gameUUIDField.getText().trim();
            if (!uuid.isEmpty()) {
                onJoinGame.accept(uuid);
                ClientManager.getInstance().getSceneUpdater().accept(
                        new LobbyUI(ClientManager.getInstance().getUsername()).getLayout());
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please enter a valid mission UUID.");
                alert.showAndWait();
            }
        });

        layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setPrefSize(500, 400);

        LinearGradient galaxyBackground = new LinearGradient(
                0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#0F0C29")),
                new Stop(0.5, Color.web("#302B63")),
                new Stop(1, Color.web("#24243E"))
        );
        layout.setBackground(new Background(new BackgroundFill(galaxyBackground, CornerRadii.EMPTY, null)));

        layout.getChildren().addAll(uuidLabel, gameUUIDField, activeGamesLabel, activeGamesList, joinGameButton);
    }

    public VBox getLayout() {
        return layout;
    }
}
