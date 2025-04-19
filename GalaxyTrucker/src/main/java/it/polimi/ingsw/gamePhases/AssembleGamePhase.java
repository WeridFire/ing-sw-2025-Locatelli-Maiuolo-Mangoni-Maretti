package it.polimi.ingsw.gamePhases;

import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.gamePhases.exceptions.CommandNotAllowedException;
import it.polimi.ingsw.gamePhases.exceptions.TimerIsAlreadyRunningException;
import it.polimi.ingsw.network.GameServer;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.util.GameLevelStandards;


import java.rmi.RemoteException;
import java.util.UUID;

public class AssembleGamePhase extends PlayableGamePhase {

    private static final long timerMilliseconds = 3600;  // 60 seconds
    transient private final Object timerLock = new Object();

    private int howManyTimerRotationsLeft;
    private boolean timerRunning;

    /**
     * Constructs a new PlayableGamePhase.
     *
     * @param gameId        The unique identifier of the game.
     * @param gameData      The game data.
     */
    public AssembleGamePhase(UUID gameId, GameData gameData) {
        super(gameId, GamePhaseType.ASSEMBLE, gameData);

        timerRunning = false;
        howManyTimerRotationsLeft = GameLevelStandards.getTimerSpacesCount(gameData.getLevel());
    }

    public void playLoop() throws RemoteException, InterruptedException {

        if(gameData.getCurrentGamePhase() != this){
            throw new  RuntimeException("Trying to run a game phase which is not active on the game.");
        }

        while (howManyTimerRotationsLeft > 1) {
            GameServer.getInstance().broadcastUpdate(gameData.getGameId());
            timerRunning = true;
            Thread.sleep(timerMilliseconds);  // Wait for a full hourglass time
            timerRunning = false;

            // wait for a player to restart the timer
            synchronized (timerLock) {
                timerLock.wait();
            }
            howManyTimerRotationsLeft -= 1;
        }

        // if the hourglass is in its stop place (keep this 'if' to not lose the TESTFLIGHT case)
        if (howManyTimerRotationsLeft == 1) {
            // wait at most a total hourglass time (or get notified earlier by notifyAllPlayersEndedAssembly)
            synchronized (timerLock) {
                timerLock.wait(timerMilliseconds);
            }
            // if here can be because time ended: delegated to the caller to force all the players to end assembly
            howManyTimerRotationsLeft = 0;
        }
        else {  // forced to wait all the players
            synchronized (timerLock) {
                timerLock.wait();
            }
        }
    }

    @Override
    public void startTimer(Player p) throws TimerIsAlreadyRunningException, CommandNotAllowedException {
        if (timerRunning) {
            throw new TimerIsAlreadyRunningException("You must wait for the hourglass to finish before flipping it.");
        }
        if(gameData.getLevel() == GameLevel.TESTFLIGHT){
            throw new CommandNotAllowedException("start timer", "Time constraints are not present in a TESTFLIGHT game!");
        }
        if(howManyTimerRotationsLeft == 1 && !p.getShipBoard().isEndedAssembly()){
            throw new CommandNotAllowedException("start timer", "You must have first finished assembling the ship before performing the last flip.");
        }
        synchronized (timerLock) {
            timerLock.notifyAll();
        }
    }

    /**
     * Notify that all the players in the game ended assembly
     */
    public void notifyAllPlayersEndedAssembly() {
        if (howManyTimerRotationsLeft > 1) {
            return;  // invalid invocation of the function, but does not affect the state
            // like: mhh you are kidding me, the players did not reach the last hourglass flip yet
        }
        synchronized (timerLock) {
            timerLock.notifyAll();
        }
    }

}
