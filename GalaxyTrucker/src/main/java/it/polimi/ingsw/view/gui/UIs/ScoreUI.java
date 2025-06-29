package it.polimi.ingsw.view.gui.UIs;

import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.model.gamePhases.ScoreGamePhase;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.network.messages.ClientUpdate;
import it.polimi.ingsw.util.Logger;
import it.polimi.ingsw.view.gui.managers.ClientManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.*;

public class ScoreUI implements INodeRefreshableOnUpdateUI {

    private final StackPane root = new StackPane();
    private final VBox content = new VBox(15); // vertical spacing

    public ScoreUI() {
        Label thankYouLabel = new Label("THANKS FOR PLAYING!");
        thankYouLabel.setFont(Font.font("Arial", 28));
        thankYouLabel.setTextFill(Color.web("#333"));
        thankYouLabel.setStyle("-fx-font-weight: bold;");

        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(40, 20, 40, 20));
        content.getChildren().add(thankYouLabel);

        root.setStyle("-fx-background-color: linear-gradient(to bottom, #ffffff, #e0e0e0);");
        root.getChildren().add(content);

        refreshOnUpdate(ClientManager.getInstance().getLastUpdate());
    }

    @Override
    public Node getLayout() {
        return root;
    }

    @Override
    public void refreshOnUpdate(ClientUpdate update) {
        if (update.getCurrentGame() == null) {
            Platform.runLater(() -> ClientManager.getInstance().createOrJoinGame(ClientManager.getInstance().getUsername()));
        } else {
            if (update.getCurrentGame().getCurrentGamePhaseType() == GamePhaseType.ENDGAME) {
                ScoreGamePhase sgp = (ScoreGamePhase) update.getCurrentGame().getCurrentGamePhase();
                Map<Player, Float> scores = sgp.calculateScores();

                List<Map.Entry<Player, Float>> sortedScores = new ArrayList<>(scores.entrySet());
                sortedScores.sort((a, b) -> Float.compare(b.getValue(), a.getValue()));

                VBox leaderboard = new VBox(10);
                leaderboard.setAlignment(Pos.CENTER);

                int rank = 1;
                for (Map.Entry<Player, Float> entry : sortedScores) {
                    Label scoreLabel = new Label("#" + rank++ + " - " + entry.getKey().getUsername() + ": " + String.format("%.2f", entry.getValue()));
                    scoreLabel.setFont(Font.font("Arial", 18));
                    scoreLabel.setTextFill(Color.web("#222"));
                    leaderboard.getChildren().add(scoreLabel);
                }

                Label infoLabel = new Label("You will be brought back to the main menu in 10 seconds.");
                infoLabel.setFont(Font.font("Arial", 14));
                infoLabel.setTextFill(Color.DARKRED);
                infoLabel.setStyle("-fx-padding: 20px 0 0 0;");

                Platform.runLater(() -> {
                    content.getChildren().removeIf(node -> node != content.getChildren().get(0)); // keep only title
                    content.getChildren().addAll(leaderboard, infoLabel);
                });

            } else {
                Logger.error("Attempted to show ScoreUI during a phase different from ENDGAME.");
            }
        }
    }
}
