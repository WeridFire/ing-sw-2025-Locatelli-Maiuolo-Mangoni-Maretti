package it.polimi.ingsw.view.gui.UIs;

import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.model.gamePhases.ScoreGamePhase;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.network.messages.ClientUpdate;
import it.polimi.ingsw.util.Logger;
import it.polimi.ingsw.view.View;
import it.polimi.ingsw.view.gui.managers.ClientManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.*;

public class ScoreUI implements INodeRefreshableOnUpdateUI {

    private final StackPane root = new StackPane();
    private final VBox content = new VBox(20); // increased vertical spacing

    public ScoreUI() {
        // Apply consistent dark theme styling from AdventureUI
        root.setStyle("-fx-background-color: #1a1c2c;");
        
        Label thankYouLabel = new Label("THANKS FOR PLAYING!");
        thankYouLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        thankYouLabel.setTextFill(Color.web("#4dd0e1"));
        
        // Add a stylish border around the content
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(40, 30, 40, 30));
        content.setStyle("-fx-background-color: rgba(0,0,0,0.3); -fx-border-color: #4dd0e1; " +
                         "-fx-border-width: 2; -fx-border-radius: 5; -fx-background-radius: 5;");
        content.setMaxWidth(600);
        content.getChildren().add(thankYouLabel);
        
        // Center the content in the root pane
        StackPane.setAlignment(content, Pos.CENTER);
        StackPane.setMargin(content, new Insets(20));
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
            View view = ClientManager.getInstance().getGameClient().getView();
            view.unregisterOnUpdateListener(this::refreshOnUpdate);
            Platform.runLater(() -> {
                AssembleUI.reset();
                AdventureUI.reset();
                ClientManager.getInstance().updateScene(new JoinGameUI(ClientManager.getInstance().getUsername()));
            });
        } else {
            if (update.getCurrentGame().getCurrentGamePhaseType() == GamePhaseType.ENDGAME) {
                ScoreGamePhase sgp = (ScoreGamePhase) update.getCurrentGame().getCurrentGamePhase();
                Map<Player, Float> scores = sgp.calculateScores();

                List<Map.Entry<Player, Float>> sortedScores = new ArrayList<>(scores.entrySet());
                sortedScores.sort((a, b) -> Float.compare(b.getValue(), a.getValue()));

                VBox leaderboard = new VBox(15);
                leaderboard.setAlignment(Pos.CENTER);
                leaderboard.setPadding(new Insets(15, 0, 15, 0));
                leaderboard.setStyle("-fx-background-color: rgba(0,0,0,0.2); -fx-border-color: #444; " +
                                    "-fx-border-width: 1; -fx-padding: 15; -fx-border-radius: 3; -fx-background-radius: 3;");

                // Add a header for the leaderboard
                Label leaderboardHeader = new Label("FINAL SCORES");
                leaderboardHeader.setFont(Font.font("Arial", FontWeight.BOLD, 24));
                leaderboardHeader.setTextFill(Color.web("#4dd0e1"));
                leaderboardHeader.setPadding(new Insets(0, 0, 10, 0));
                leaderboard.getChildren().add(leaderboardHeader);

                int rank = 1;
                for (Map.Entry<Player, Float> entry : sortedScores) {
                    HBox scoreRow = new HBox(15);
                    scoreRow.setAlignment(Pos.CENTER);

                    Label rankLabel = new Label("#" + rank);
                    rankLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
                    rankLabel.setTextFill(getPlayerRankColor(rank));
                    rankLabel.setMinWidth(40);

                    Label nameLabel = new Label(entry.getKey().getUsername());
                    nameLabel.setFont(Font.font("Arial", 18));
                    nameLabel.setTextFill(Color.WHITE);
                    nameLabel.setMinWidth(150);

                    Label scoreLabel = new Label(String.format("%.2f", entry.getValue()));
                    scoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
                    scoreLabel.setTextFill(Color.web("#4dd0e1"));

                    scoreRow.getChildren().addAll(rankLabel, nameLabel, scoreLabel);
                    leaderboard.getChildren().add(scoreRow);

                    rank++;
                }

            } else {
                Logger.error("Attempted to show ScoreUI during a phase different from ENDGAME.");
            }
        }
    }
    
    /**
     * Returns a color for player rank display based on position.
     * @param rank The player's rank in the game.
     * @return A Color object representing the rank color.
     */
    private Color getPlayerRankColor(int rank) {
        return switch (rank) {
            case 1 -> Color.web("#ffd700"); // Gold for 1st place
            case 2 -> Color.web("#c0c0c0"); // Silver for 2nd place
            case 3 -> Color.web("#cd7f32"); // Bronze for 3rd place
            default -> Color.web("#aaaaaa"); // Gray for other places
        };
    }
}
