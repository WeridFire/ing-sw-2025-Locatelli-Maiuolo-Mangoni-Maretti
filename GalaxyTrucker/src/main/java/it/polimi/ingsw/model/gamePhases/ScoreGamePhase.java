package it.polimi.ingsw.model.gamePhases;

import it.polimi.ingsw.enums.AnchorPoint;
import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.model.game.GameData;
import it.polimi.ingsw.controller.commandsProcessors.exceptions.CommandNotAllowedException;
import it.polimi.ingsw.model.gamePhases.exceptions.TimerIsAlreadyRunningException;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.playerInput.PIRs.PIRDelay;
import it.polimi.ingsw.util.Default;
import it.polimi.ingsw.util.ScoreCalculator;
import it.polimi.ingsw.view.cli.ANSI;
import it.polimi.ingsw.view.cli.CLIFrame;
import it.polimi.ingsw.view.cli.ICLIPrintable;
import it.polimi.ingsw.util.GameLevelStandards;
import java.io.Serializable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ScoreGamePhase extends PlayableGamePhase implements ICLIPrintable, Serializable {

    public ScoreGamePhase(GameData gameData) {
        super(GamePhaseType.ENDGAME, gameData);
    }

    @Override
    public void playLoop() throws InterruptedException {
        notifyScoresToPlayers();
    }

    private void notifyScoresToPlayers() throws InterruptedException {
        CLIFrame scoresFrame = getCLIRepresentation();
        gameData.getPIRHandler().broadcastPIR(
                gameData.getPlayers(Player::isConnected),
                (player, pirHandler) -> {
                    PIRDelay pirDelay = new PIRDelay(
                            player,
                            10,
                            "GG to all, match is over. You will be sent to the menu in 10 seconds...",
                            scoresFrame);
                    pirHandler.setAndRunTurn(pirDelay);
                });
    }

    /**
     * Starts a timer for a specific player, if applicable in this phase.
     */
    @Override
    public void startTimer(Player p) throws TimerIsAlreadyRunningException, CommandNotAllowedException {
        throw new CommandNotAllowedException("startTimer","No timer is allowed during the score screen phase.");
    }

    private Predicate<Player> calculateGiveBestLookingShipboardAward() {
        //finds the count of the least exposed connectors
        int leastExposedConnectors = Integer.MAX_VALUE;
        for(Player p: gameData.getPlayers()) {
            int tempExposedConnectors = p.getShipBoard().getExposedConnectorsCount();
            if (leastExposedConnectors > tempExposedConnectors) {
                leastExposedConnectors = tempExposedConnectors;
            }
        }
        int finalLeastExposedConnectors = leastExposedConnectors;
        return (p -> !p.isEndedFlight()
                && p.getShipBoard().getExposedConnectorsCount() == finalLeastExposedConnectors);
    }

    /**
     * Used to calculate scores at the end of a match
     * @return sorted map (descending) of players and their scores
     */
    public Map<Player, Float> calculateScores(){

        float score;
        Map<Player, Float> playerScores = new HashMap<>();

        Predicate<Player> giveBestLookingShipboardAward = calculateGiveBestLookingShipboardAward();

        //calculates points
        int i = 0;
        List<Integer> finishOrderRewards = GameLevelStandards.getFinishOrderRewards(gameData.getLevel());
        int awardForBestLookingShip = GameLevelStandards.getAwardForBestLookingShip(gameData.getLevel());
        for(Player p: gameData.getPlayers())
        {
            //points for the goods and repairs
            score = ScoreCalculator.calculateScore(p);

            //adds points for the positions
            if(!p.isEndedFlight()){
                score += finishOrderRewards.get(i);
            }

            //adds points for the best ship
            if(giveBestLookingShipboardAward.test(p)){
                score += awardForBestLookingShip;
            }

            playerScores.put(p, score);

            i++;
        }

        //sorts the players
        return playerScores.entrySet().stream()
                .sorted(Map.Entry.<Player, Float>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    /**
     *
     * @return the CLI representation of the leaderboard
     */
    @Override
    public CLIFrame getCLIRepresentation() {

        Map<Player, Float> scores = calculateScores(); // Get sorted scores
        CLIFrame frameLeaderboard = new CLIFrame(ANSI.BLUE + "LEADERBOARD");

        int rank = 0;
        Float prevScore = null;
        int sameRankCount = 0;

        CLIFrame framePlayers = new CLIFrame();
        CLIFrame frameScores = new CLIFrame();
        for (Map.Entry<Player, Float> entry : scores.entrySet()) {
            float score = entry.getValue();

            // Handle tied ranks
            // If score is different from previous, update rank
            if (prevScore != null && score == prevScore) {
                sameRankCount++;    // Increment tie counter
            } else {
                rank += 1 + sameRankCount;  // Apply accumulated ties
                sameRankCount = 0;      // Reset counter
            }

            prevScore = score;

            framePlayers = framePlayers.merge(
                    new CLIFrame(ANSI.WHITE + rank + ". " + ANSI.GREEN + entry.getKey().getUsername()),
                    AnchorPoint.BOTTOM_LEFT, AnchorPoint.TOP_LEFT
            );

            frameScores = frameScores.merge(new CLIFrame(ANSI.YELLOW + score), Direction.SOUTH);
        }

        return frameLeaderboard.merge(
                framePlayers.merge(frameScores, Direction.EAST, 2),
                Direction.SOUTH, 1
        );
    }
}
