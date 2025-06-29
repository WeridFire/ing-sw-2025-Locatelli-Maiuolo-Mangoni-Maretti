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

/**
 * User interface for the game lobby where players wait before the game starts.
 * Displays the current number of players and allows the game leader to configure
 * game settings such as player count and difficulty level.
 * Automatically transitions to the AssembleUI when the game phase changes.
 * Implements INodeRefreshableOnUpdateUI to update based on server state changes.
 */
public class LobbyUI implements INodeRefreshableOnUpdateUI {
    /**
     * The main layout container for all UI components.
     */
    private final VBox layout;
    /**
     * ComboBox for selecting the required number of players.
     */
    private ComboBox<Integer> playerCountBox;
    /**
     * ComboBox for selecting the game difficulty level.
     */
    private ComboBox<GameLevel> gameLevelBox;
    /**
     * Label displaying the current number of players in the lobby.
     */
    private Label statusLabel;

    /**
     * Constructs the lobby UI with player count display and game settings.
     * Only the game leader can modify settings; other players see read-only controls.
     * @throws IllegalStateException if the game data is not properly initialized.
     */
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
        VBox.setVgrow(layout, Priority.ALWAYS);

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

        //ping for rejoin
        try{
            Platform.runLater(() -> {
                ClientManager.getInstance().simulateCommand("ping");
            });
        }catch(Exception e){}
    }

    /**
     * Sets up event listeners for the settings ComboBoxes.
     * Only called if the current user is the game leader.
     */
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

    /**
     * Returns the main layout container for this UI.
     * @return The VBox containing all UI components.
     */
    public VBox getLayout() {
        return layout;
    }

    /**
     * Gets the currently selected player count from the ComboBox.
     * @return The selected number of players, or null if none selected.
     */
    public Integer getSelectedPlayerCount() {
        return playerCountBox.getValue();
    }

    /**
     * Gets the currently selected game level from the ComboBox.
     * @return The selected GameLevel, or null if none selected.
     */
    public GameLevel getSelectedGameLevel() {
        return gameLevelBox.getValue();
    }

    /**
     * Refreshes the UI components based on a new client update from the server.
     * Updates player count display, game settings, and transitions to AssembleUI
     * when the game phase changes to ASSEMBLE.
     * @param update The ClientUpdate containing the new game state.
     */
    @Override
    public void refreshOnUpdate(ClientUpdate update) {
        GameData game = update.getCurrentGame();
        if (game == null) {
            AlertUtils.showError("Game Not Found",
                    "The current game was not found, please restart the application");
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

            try{
                if (CommonState.isCurrentPhase(GamePhaseType.ASSEMBLE)) {
                    ClientManager.getInstance().updateScene(AssembleUI.getInstance());
                } else if (CommonState.isCurrentPhase(GamePhaseType.ADVENTURE)) {
                    ClientManager.getInstance().updateScene(AdventureUI.getInstance());
                }
            }catch(Exception e){}
        });
    }
}
