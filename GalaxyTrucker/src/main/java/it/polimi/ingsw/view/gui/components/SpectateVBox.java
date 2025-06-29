package it.polimi.ingsw.view.gui.components;

import it.polimi.ingsw.controller.states.CommonState;
import it.polimi.ingsw.controller.states.LobbyState;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.view.gui.managers.ClientManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;

public class SpectateVBox extends VBox {

    private final Button leftButton;
    private final Label visitedPlayerLabel;
    private final Button rightButton;
    private ArrayList<Player> players;
    private int playerIndex;
    private String playerName;

    public SpectateVBox() {
        players = new ArrayList<>(LobbyState.getPlayers());

        playerName = CommonState.getPlayer().getUsername();
        playerIndex = players.indexOf(CommonState.getPlayer());

        leftButton = new Button("<");
        leftButton.setOnMouseClicked(e -> {
            if (playerIndex > 0){
                playerIndex--;
                checkButtonsAvailability();
                checkVisitedPlayerLabel();
                performSpectateCommand();
            }

        });

        visitedPlayerLabel = new Label(playerName);

        rightButton = new Button(">");
        rightButton.setOnMouseClicked(e -> {
            if (playerIndex < players.size() - 1){
                playerIndex++;
                checkButtonsAvailability();
                checkVisitedPlayerLabel();
                performSpectateCommand();
            }
        });

        visitedPlayerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        HBox topRow = new HBox(10, leftButton, visitedPlayerLabel, rightButton);
        topRow.setAlignment(Pos.CENTER);

        this.setSpacing(15);
        this.setAlignment(Pos.CENTER);
        this.setPadding(new Insets(10));
        this.getChildren().addAll(topRow);
        checkButtonsAvailability();
    }

    private void checkButtonsAvailability(){
        if(playerIndex >= players.size()-1){
            rightButton.setDisable(true);
            leftButton.setDisable(false);
            return;
        }

        if (playerIndex <= 0){
            rightButton.setDisable(false);
            leftButton.setDisable(true);
            return;
        }

        rightButton.setDisable(false);
        leftButton.setDisable(false);
    }

    private void checkVisitedPlayerLabel(){
        setVisitedPlayerLabel(players.get(playerIndex).getUsername());
    }

    public void setVisitedPlayerLabel(String name) {
        visitedPlayerLabel.setText(name);
    }

    public void performSpectateCommand(){
        Platform.runLater(() -> {
            ClientManager.getInstance().simulateCommand("spectate", players.get(playerIndex).getUsername());
        });
    }
}