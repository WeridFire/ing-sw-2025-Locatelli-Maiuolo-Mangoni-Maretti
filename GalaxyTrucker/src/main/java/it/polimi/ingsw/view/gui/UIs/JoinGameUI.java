package it.polimi.ingsw.view.gui.UIs;

import it.polimi.ingsw.controller.states.MenuState;
import it.polimi.ingsw.network.messages.ClientUpdate;
import it.polimi.ingsw.view.gui.managers.ClientManager;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import java.util.List;
import java.util.function.Consumer;

public class JoinGameUI implements INodeRefreshableOnUpdateUI {
    private final VBox layout;
    private final TextField gameUUIDField;
    private final Button joinGameButton;
    private final ListView<String> activeGamesList;

    public JoinGameUI(Consumer<String> onJoinGame, List<String> activeGames) {
        Label uuidLabel = new Label("Enter game UUID:");
        gameUUIDField = new TextField();
        gameUUIDField.setPromptText("Game UUID");

        Label activeGamesLabel = new Label("Active Games:");
        activeGamesList = new ListView<>(FXCollections.observableArrayList(activeGames));
        activeGamesList.setMaxHeight(150);

        activeGamesList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                gameUUIDField.setText(newVal);
            }
        });

        joinGameButton = new Button("Join Game");
        joinGameButton.setOnAction(e -> {
            String uuid = gameUUIDField.getText().trim();
            if (!uuid.isEmpty()) {
                onJoinGame.accept(uuid);

                ClientManager.getInstance().updateScene(new LobbyUI(ClientManager.getInstance().getUsername()));

            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please enter a valid game UUID.");
                alert.showAndWait();
            }
        });

        layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(uuidLabel, gameUUIDField, activeGamesLabel, activeGamesList, joinGameButton);
    }

    public VBox getLayout() {
        return layout;
    }

    private void refreshActiveGamesList(List<String> activeGames) {
        activeGamesList.getItems().setAll(activeGames);
    }

    @Override
    public void refreshOnUpdate(ClientUpdate update) {
        refreshActiveGamesList(MenuState.getActiveGamesUUID());
    }
}