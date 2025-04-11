package it.polimi.ingsw.gamePhases;

import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.gamePhases.exceptions.CommandNotAllowedException;
import it.polimi.ingsw.gamePhases.exceptions.IncorrectGamePhaseTypeException;
import it.polimi.ingsw.gamePhases.exceptions.TimerIsAlreadyRunningException;
import it.polimi.ingsw.network.GameServer;
import it.polimi.ingsw.player.Player;


import java.rmi.RemoteException;
import java.util.UUID;

public class AssembleGamePhase extends PlayableGamePhase {

    private int howManyTimerRotationsLeft = 3;

    transient private final Object timerLock = new Object();

    private boolean timerRunning;

    /**
     * Constructs a new PlayableGamePhase.
     *
     * @param gameId        The unique identifier of the game.
     * @param gamePhaseType The type of the game phase.
     * @param gameData      The game data.
     */
    public AssembleGamePhase(UUID gameId, GameData gameData) {
        super(gameId, GamePhaseType.ASSEMBLE, gameData);

        this.timerRunning = false;

    }

    public void playLoop() throws RemoteException, InterruptedException {

        if(gameData.getCurrentGamePhase() != this){
            throw new  RuntimeException("Trying to run a game phase which is not active on the game.");
        }

        if(gameData.getLevel() == GameLevel.TESTFLIGHT){
            //For a testflight we need to wait for everyone to finish. There is no time limit.
            long pendingAssemblies = gameData.getPlayers()
                    .stream()
                    .filter(p -> !p.getShipBoard().isEndedAssembly()).count();
            while(pendingAssemblies > 0){
                synchronized (timerLock) {
                    timerLock.wait();
                }
                pendingAssemblies = gameData.getPlayers()
                        .stream()
                        .filter(p -> !p.getShipBoard().isEndedAssembly()).count();
            }
        }else{
            //For a normal flight we can proceed with the normal loop.
            while(howManyTimerRotationsLeft > 0) {
                GameServer.getInstance().broadcastUpdate(gameData.getGameId());
                timerRunning = true;
                Thread thread = new Thread(() -> {
                    try {
                        Thread.sleep(30000); // Il thread dorme per 30 secondi
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
                thread.join();
                timerRunning = false;

                // wait that a player starts the timer
                synchronized (timerLock) {
                    timerLock.wait();
                }
                howManyTimerRotationsLeft -= 1;
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

}
