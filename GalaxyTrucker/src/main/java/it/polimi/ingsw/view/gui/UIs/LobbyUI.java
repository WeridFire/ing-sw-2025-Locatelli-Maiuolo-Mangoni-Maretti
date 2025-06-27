package it.polimi.ingsw.view.gui.UIs;

import it.polimi.ingsw.controller.states.CommonState;
import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.model.game.GameData;
import it.polimi.ingsw.network.messages.ClientUpdate;
import it.polimi.ingsw.view.gui.managers.ClientManager;
import it.polimi.ingsw.view.gui.utils.AlertUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class LobbyUI implements INodeRefreshableOnUpdateUI {
    private final VBox layout;
    private ComboBox<Integer> playerCountBox;
    private ComboBox<GameLevel> gameLevelBox;
    private Label statusLabel;

    public LobbyUI() {
        ClientManager clientManager = ClientManager.getInstance();
        String username = clientManager.getUsername();
        GameData gameData = clientManager.getLastUpdate().getCurrentGame();

        if (gameData == null) {
            throw new IllegalStateException("Game not initialized");
        }

        boolean canChangeSettings = gameData.getGameLeader().equals(username);

        Label headerLabel = new Label("GAME LOBBY");
        headerLabel.getStyleClass().add("header-label");

        statusLabel = new Label("Pilots in lobby: " + gameData.getPlayers().size());
        statusLabel.getStyleClass().add("label");

        Label settingsLabel = new Label("Mission Parameters");
        settingsLabel.getStyleClass().add("label");

        layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.getStyleClass().add("root");
        VBox.setVgrow(layout, Priority.ALWAYS); // <--- AGGIUNGI QUESTA LINEA

        playerCountBox = new ComboBox<>(FXCollections.observableArrayList(2, 3, 4));
        playerCountBox.setPromptText("Crew Size");
        playerCountBox.setValue(gameData.getRequiredPlayers());
        playerCountBox.getStyleClass().add("combo-box");

        gameLevelBox = new ComboBox<>(FXCollections.observableArrayList(GameLevel.LEVELS_TO_PLAY));
        gameLevelBox.setPromptText("Danger Level");
        gameLevelBox.setValue(gameData.getLevel());
        gameLevelBox.getStyleClass().add("combo-box");

        playerCountBox.setDisable(!canChangeSettings);
        gameLevelBox.setDisable(!canChangeSettings);

        if (canChangeSettings) {
            setupListeners();
        }

        layout.getChildren().addAll(
                headerLabel,
                statusLabel,
                settingsLabel,
                playerCountBox,
                gameLevelBox
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
        if (game == null) {
            AlertUtils.showError("Game Not Found",
                    "The current game was not found, please restart the application");
            return;
        }

        if (CommonState.isCurrentPhase(GamePhaseType.ASSEMBLE)){
            Platform.runLater(() -> ClientManager.getInstance().updateScene(AssembleUI.getInstance()));
            return;
        }

        boolean canChangeSettings = update.isGameLeader();

        Platform.runLater(() -> {
            statusLabel.setText("Pilots in lobby: " +
                    game.getPlayers().size());

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
