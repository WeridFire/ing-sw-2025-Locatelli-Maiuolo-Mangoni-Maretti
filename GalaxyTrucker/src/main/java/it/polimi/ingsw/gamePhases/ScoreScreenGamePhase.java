package it.polimi.ingsw.gamePhases;

import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.gamePhases.exceptions.CommandNotAllowedException;
import it.polimi.ingsw.gamePhases.exceptions.TimerIsAlreadyRunningException;
import it.polimi.ingsw.network.exceptions.CantFindClientException;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.util.ScoreCalculator;

import java.rmi.RemoteException;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ScoreScreenGamePhase extends PlayableGamePhase{

    private final ScoreCalculator scoreCalculator;
    private Map<Player, Float> sortedScores;

    public ScoreScreenGamePhase(UUID gameId, GameData gameData) {
        super(gameId, GamePhaseType.ENDGAME, gameData);
        this.scoreCalculator = new ScoreCalculator();
    }

    @Override
    public void playLoop() throws RemoteException, CantFindClientException, InterruptedException {

        this.sortedScores = calculateScores();

    }

    /**
     * Starts a timer for a specific player, if applicable in this phase.
     */
    @Override
    public void startTimer(Player p) throws TimerIsAlreadyRunningException, CommandNotAllowedException {
        throw new CommandNotAllowedException("startTimer","No timer is allowed during the score screen phase.");
    }

    public Map<Player, Float> calculateScores(){
        //TODO: add the scores for final positions on the board (depends on the game's level)
        float score;
        Map<Player, Float> playerScores = new HashMap<>();

        for(Player p: gameData.getPlayers())
        {
           score = scoreCalculator.calculateScore(p);
           playerScores.put(p, score);
        }

        return playerScores.entrySet().stream()
                .sorted(Map.Entry.<Player, Float>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    private void printScores(Map<Player, Float> sortedScores) {
        System.out.println("Player Scores (Descending Order):");
        System.out.println("--------------------------------");

        int rank = 1;
        Float prevScore = null;
        int sameRankCount = 0;

        for (Map.Entry<Player, Float> entry : sortedScores.entrySet()) {
            float currentScore = entry.getValue();

            // If score is different from previous, update rank
            if (prevScore == null || currentScore != prevScore) {
                rank += sameRankCount;
                sameRankCount = 0;
            } else {
                sameRankCount++;
            }

            System.out.printf("%d. %s: %.2f%n",
                    rank,
                    entry.getKey().getUsername(),
                    currentScore);

            prevScore = currentScore;
        }
    }

    public Map<Player, Float> getSortedScores() {
        return sortedScores;
    }
}
