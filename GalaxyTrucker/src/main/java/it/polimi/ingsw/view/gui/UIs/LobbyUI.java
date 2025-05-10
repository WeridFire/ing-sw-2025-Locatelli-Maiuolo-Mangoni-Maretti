package it.polimi.ingsw.view.gui.UIs;

import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.network.GameClient;
import it.polimi.ingsw.network.messages.ClientUpdate;
import it.polimi.ingsw.view.gui.managers.ClientManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class LobbyUI implements INodeRefreshableOnUpdateUI {
    private final VBox layout;
    private ComboBox<Integer> playerCountBox;
    private ComboBox<GameLevel> gameLevelBox;

    public LobbyUI(String username) {
        Label statusLabel = new Label("Logged as: " + username);

        GameData gameData = ClientManager.getInstance()
                .getLastUpdate()
                .getCurrentGame();

        if (gameData == null) {
            throw new IllegalStateException("Game not initialized");
        }

        boolean canChangeSettings = gameData.getGameLeader().equals(username);

        layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);

        // ComboBox per numero di giocatori
        playerCountBox = new ComboBox<>(FXCollections.observableArrayList(2, 3, 4));
        playerCountBox.setPromptText("Number of players");
        playerCountBox.setValue(gameData.getRequiredPlayers());

        // ComboBox per livello di gioco
        gameLevelBox = new ComboBox<>(FXCollections.observableArrayList(GameLevel.values()));
        gameLevelBox.setPromptText("Select game level");
        gameLevelBox.setValue(gameData.getLevel());

        // Disabilita le ComboBox se non sei il game leader
        playerCountBox.setDisable(!canChangeSettings);
        gameLevelBox.setDisable(!canChangeSettings);

        if (canChangeSettings) {
            setupListeners();
        }

        layout.getChildren().addAll(
                new Label("Game Settings:"),
                playerCountBox,
                gameLevelBox,
                statusLabel
        );
    }

    private void setupListeners() {
        playerCountBox.setOnAction(_ ->
                ClientManager.getInstance().simulateCommand("settings",
                        "minplayers", getSelectedPlayerCount().toString())
        );
        gameLevelBox.setOnAction(_ ->
                ClientManager.getInstance().simulateCommand("settings",
                        "level", getSelectedGameLevel().toString())
        );
    }

    public VBox getLayout() {
        return layout;
    }

    public Integer getSelectedPlayerCount() {
        return playerCountBox.getValue();
    }

    public GameLevel getSelectedGameLevel() {
        return gameLevelBox.getValue();
    }

    @Override
    public void refreshOnUpdate(ClientUpdate update) {
        GameData game = update.getCurrentGame();

        if (game.getCurrentGamePhaseType().equals(GamePhaseType.ASSEMBLE)){
            Platform.runLater(() -> {
                ClientManager.getInstance().updateScene(new AssembleUI());
            });
            return;
        }

        boolean canChangeSettings = update.isGameLeader();

        Platform.runLater(() -> {
            // Aggiorna i valori delle comboBox
            playerCountBox.setDisable(false);
            gameLevelBox.setDisable(false);

            playerCountBox.setValue(game.getRequiredPlayers());
            gameLevelBox.setValue(game.getLevel());

            // Riabilita/disabilita secondo canChangeSettings
            playerCountBox.setDisable(!canChangeSettings);
            gameLevelBox.setDisable(!canChangeSettings);
        });
    }
}
