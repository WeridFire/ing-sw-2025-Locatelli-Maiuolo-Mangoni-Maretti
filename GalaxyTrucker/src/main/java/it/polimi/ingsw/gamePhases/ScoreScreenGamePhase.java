package it.polimi.ingsw.gamePhases;

import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.gamePhases.exceptions.CommandNotAllowedException;
import it.polimi.ingsw.gamePhases.exceptions.TimerIsAlreadyRunningException;
import it.polimi.ingsw.network.exceptions.CantFindClientException;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.util.ScoreCalculator;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScoreScreenGamePhase extends PlayableGamePhase{

    private ScoreCalculator scoreCalculator;

    public ScoreScreenGamePhase(UUID gameId, GameData gameData) {
        super(gameId, GamePhaseType.ENDGAME, gameData);
        this.scoreCalculator = new ScoreCalculator();
    }

    @Override
    public void playLoop() throws RemoteException, CantFindClientException, InterruptedException {

        calculateScores();
        //TODO: finire implementazione
    }

    /**
     * Starts a timer for a specific player, if applicable in this phase.
     */
    @Override
    public void startTimer(Player p) throws TimerIsAlreadyRunningException, CommandNotAllowedException {
        throw new CommandNotAllowedException("startTimer","No timer is allowed during the score screen phase.");
    }

    public void calculateScores(){
        float score;
        Map<Player, Float> playerScores = new HashMap<>();

        for(Player p: gameData.getPlayers())
        {
           score = scoreCalculator.calculateScore(p);
           playerScores.put(p, score);
        }
    }
}
