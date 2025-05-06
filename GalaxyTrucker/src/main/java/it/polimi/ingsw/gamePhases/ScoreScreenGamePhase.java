package it.polimi.ingsw.gamePhases;

import it.polimi.ingsw.enums.AnchorPoint;
import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.gamePhases.exceptions.CommandNotAllowedException;
import it.polimi.ingsw.gamePhases.exceptions.TimerIsAlreadyRunningException;
import it.polimi.ingsw.network.exceptions.CantFindClientException;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.util.ScoreCalculator;
import it.polimi.ingsw.view.cli.ANSI;
import it.polimi.ingsw.view.cli.CLIFrame;
import it.polimi.ingsw.view.cli.ICLIPrintable;
import it.polimi.ingsw.util.GameLevelStandards;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.*;
import java.util.stream.Collectors;

public class ScoreScreenGamePhase extends PlayableGamePhase implements ICLIPrintable, Serializable {

    private final ScoreCalculator scoreCalculator;

    public ScoreScreenGamePhase(UUID gameId, GameData gameData) {
        super(gameId, GamePhaseType.ENDGAME, gameData);
        this.scoreCalculator = new ScoreCalculator();
    }

    @Override
    public void playLoop() throws RemoteException, CantFindClientException, InterruptedException {

    }

    /**
     * Starts a timer for a specific player, if applicable in this phase.
     */
    @Override
    public void startTimer(Player p) throws TimerIsAlreadyRunningException, CommandNotAllowedException {
        throw new CommandNotAllowedException("startTimer","No timer is allowed during the score screen phase.");
    }

    /**
     * Used to calculate scores at the end of a match
     * @return sorted map (descending) of players and their scores
     */
    public Map<Player, Float> calculateScores(){

        float score;
        Map<Player, Float> playerScores = new HashMap<>();

        //finds the count of the least exposed connectors
        Integer leastExposedConnectors = null;
        for(Player p: gameData.getPlayers())
        {
            int tempExposedConnectors = p.getShipBoard().getExposedConnectorsCount();
            if(leastExposedConnectors == null || leastExposedConnectors > tempExposedConnectors)
            {
                leastExposedConnectors = tempExposedConnectors;
            }
        }

        //calculates points
        int i = 0;
        for(Player p: gameData.getPlayers())
        {
            i++;

            //points for the goods and repairs
           score = scoreCalculator.calculateScore(p);

           //adds points for the positions
           if(!p.isEndedFlight()){
               score += GameLevelStandards.getFinishOrderRewards(gameData.getLevel()).get(i);
           }

           //adds points for the best ship
           if(p.getShipBoard().getExposedConnectorsCount() == leastExposedConnectors){
               score += GameLevelStandards.getAwardForBestLookingShip(gameData.getLevel());
           }
           playerScores.put(p, score);
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

    //Used in testing
    public void printScores(Map<Player, Float> sortedScores) {

        System.out.println("Player Scores (Descending Order):");
        System.out.println("--------------------------------");

        int rank = 1;
        Float prevScore = null;
        int sameRankCount = 0;

        for (Map.Entry<Player, Float> entry : sortedScores.entrySet()) {
            float currentScore = entry.getValue();

            // If score is different from previous, update rank
            if (prevScore != null && currentScore == prevScore) {
                sameRankCount++;    // Increment tie counter
            } else {
                rank += 1 + sameRankCount;  // Apply accumulated ties
                sameRankCount = 0;      // Reset counter
            }

            System.out.printf("%d. %s: %.2f%n",
                    rank,
                    entry.getKey().getUsername(),
                    currentScore);

            prevScore = currentScore;
        }
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
