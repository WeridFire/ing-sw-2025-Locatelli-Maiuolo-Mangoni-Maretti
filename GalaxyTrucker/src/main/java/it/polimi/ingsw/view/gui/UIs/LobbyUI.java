package it.polimi.ingsw.view.gui.UIs;

import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.network.GameClient;
import it.polimi.ingsw.view.gui.managers.ClientManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.rmi.RemoteException;

public class LobbyUI {
    private final VBox layout;
    private ComboBox<Integer> playerCountBox;
    private ComboBox<GameLevel> gameLevelBox;
    private final GameClient gameClient = ClientManager.getInstance().getGameClient();

    public LobbyUI(String username) {


        Label statusLabel = new Label("Logged as: " + username);

        GameData gameData = ClientManager.getInstance()
                .getLastUpdate()
                .getCurrentGame();

        if (gameData == null) {
            throw new IllegalStateException("Game not initialized");
        }

        boolean showSettings = gameData.getGameLeader().equals(username);

        layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);

        // ComboBox per numero di giocatori
        playerCountBox = new ComboBox<>(FXCollections.observableArrayList(2, 3, 4));
        playerCountBox.setPromptText("Number of players");
        playerCountBox.setValue(3);

        // ComboBox per livello di gioco
        gameLevelBox = new ComboBox<>(FXCollections.observableArrayList(GameLevel.values()));
        gameLevelBox.setPromptText("Select game level");
        gameLevelBox.setValue(GameLevel.TESTFLIGHT);

        // Disabilita le ComboBox se non sei il game leader
        playerCountBox.setDisable(!showSettings);
        gameLevelBox.setDisable(!showSettings);

        if (showSettings) {
            setupListeners();
        }

        //get setting update every second
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                    setLastUpdateSettings(showSettings);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();

        layout.getChildren().addAll(
                new Label("Game Settings:"),
                playerCountBox,
                gameLevelBox,
                statusLabel
        );
    }

    private void setupListeners() {
        playerCountBox.setOnAction(event -> onChange());
        gameLevelBox.setOnAction(event -> onChange());
    }

    private void onChange() {
        System.out.println("Updated Settings");
        try {
            gameClient.getClient().getServer().updateGameSettings(
                    gameClient.getClient(),
                    getSelectedGameLevel(),
                    getSelectedPlayerCount()
            );
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
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

    public void setLastUpdateSettings(boolean showSettings) {
        try {
            ClientManager.getInstance().getGameClient().getServer().ping(gameClient.getClient());
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        GameData game = ClientManager.getInstance().getLastUpdate().getCurrentGame();

        Platform.runLater(() -> {
            // Aggiorna i valori delle comboBox
            playerCountBox.setDisable(false);
            gameLevelBox.setDisable(false);

            playerCountBox.setValue(game.getRequiredPlayers());
            gameLevelBox.setValue(game.getLevel());

            // Riabilita/disabilita secondo showSettings
            playerCountBox.setDisable(!showSettings);
            gameLevelBox.setDisable(!showSettings);
        });

    }
}
