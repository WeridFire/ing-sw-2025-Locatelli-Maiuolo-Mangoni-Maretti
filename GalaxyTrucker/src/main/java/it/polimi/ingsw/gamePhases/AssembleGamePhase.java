package it.polimi.ingsw.gamePhases;

import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.gamePhases.exceptions.IncorrectGamePhaseTypeException;
import it.polimi.ingsw.gamePhases.exceptions.TimerIsAlreadyRunningException;
import it.polimi.ingsw.network.GameServer;


import java.rmi.RemoteException;
import java.util.UUID;

public class AssembleGamePhase extends PlayableGamePhase {

    private int howManyTimerRotationsLeft;

    transient private final Object timerLock = new Object();

    private boolean timerRunning;

    /**
     * Constructs a new PlayableGamePhase.
     *
     * @param gameId        The unique identifier of the game.
     * @param gamePhaseType The type of the game phase.
     * @param gameData      The game data.
     */
    public AssembleGamePhase(UUID gameId, GamePhaseType gamePhaseType, GameData gameData) throws IncorrectGamePhaseTypeException{
        super(gameId, gamePhaseType, gameData);

        if (!gamePhaseType.equals(GamePhaseType.ASSEMBLE)){
            throw new IncorrectGamePhaseTypeException("Assemble gamePhase type is not coherent");
        }

        if (gameData.getLevel().equals(GameLevel.TESTFLIGHT)){
            this.howManyTimerRotationsLeft = -1;
        } else if (gameData.getLevel().equals(GameLevel.TWO)) {
            this.howManyTimerRotationsLeft = 3;
        }

        this.timerRunning = false;

    }

    public void playLoop() throws RemoteException, InterruptedException {

        gameData.setCurrentGamePhase(this);

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

    public void startTimer() throws TimerIsAlreadyRunningException {
        if (timerRunning) {
            throw new TimerIsAlreadyRunningException("You must wait for the hourglass to finish before flipping it.");
        }
        synchronized (timerLock) {
            timerLock.notifyAll();
        }
    }

}
