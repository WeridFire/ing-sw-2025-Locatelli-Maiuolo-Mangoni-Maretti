package it.polimi.ingsw.model.gamePhases;

import it.polimi.ingsw.GamesHandler;
import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.model.game.GameData;
import it.polimi.ingsw.controller.commandsProcessors.exceptions.CommandNotAllowedException;
import it.polimi.ingsw.model.gamePhases.exceptions.TimerIsAlreadyRunningException;
import it.polimi.ingsw.network.GameServer;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.util.Default;
import it.polimi.ingsw.util.GameLevelStandards;


import java.rmi.RemoteException;
import java.util.UUID;

public class AssembleGamePhase extends PlayableGamePhase {

    private static final long timerMilliseconds = Default.HOURGLASS_SECONDS * 1000L;
    transient private final Object timerLock = new Object();
    transient private Runnable onTimerSwitchCallback;

    private final int totalTimerRotations;
    private int howManyTimerRotationsLeft;
    private boolean timerRunning;

    /**
     * Constructs a new PlayableGamePhase as {@link GamePhaseType#ASSEMBLE}.
     * Every time a timer stops or gets resumed: executes {@code onTimerSwitchCallback}.
     *
     * @param gameData The game data.
     * @param onTimerSwitchCallback The callback function to be executed every time a timer switches state.
     */
    public AssembleGamePhase(GameData gameData, Runnable onTimerSwitchCallback) {
        super(GamePhaseType.ASSEMBLE, gameData);
        totalTimerRotations = GameLevelStandards.getTimerSlotsCount(gameData.getLevel());
        howManyTimerRotationsLeft = totalTimerRotations;
        setOnTimerSwitchCallback(onTimerSwitchCallback);
        setTimerRunning(false);
    }

    /**
     * Constructs a new PlayableGamePhase as {@link GamePhaseType#ASSEMBLE}.
     * Every time a timer stops or gets resumed: does nothing.
     *
     * @param gameId        The unique identifier of the game.
     * @param gameData      The game data.
     */
    public AssembleGamePhase(UUID gameId, GameData gameData) {
        this(gameData, null);
    }

    private void setTimerRunning(boolean running) {
        timerRunning = running;
        // callback for timer ended or started
        if (onTimerSwitchCallback != null) {
            onTimerSwitchCallback.run();
        }
    }

    public void playLoop() throws RemoteException, InterruptedException {

        synchronized (gameData.getUnorderedPlayers()) {
            if(gameData.getPlayers().isEmpty()){
                GamesHandler gamesHandler = GamesHandler.getInstance();
                gamesHandler.getGames().remove(gamesHandler.getGame(this.gameId));
            }
        }

        if (gameData.getCurrentGamePhase() != this) {
            throw new  RuntimeException("Trying to run a game phase which is not active on the game.");
        }

        while (howManyTimerRotationsLeft > 1) {
            GameServer.getInstance().broadcastUpdate(GamesHandler.getInstance().getGame(gameId));
            setTimerRunning(true);
            Thread.sleep(timerMilliseconds);  // Wait for a full hourglass time
            setTimerRunning(false);

            // wait for a player to restart the timer
            synchronized (timerLock) {
                timerLock.wait();
            }
            howManyTimerRotationsLeft -= 1;
        }

        // if the hourglass is in its stop place (keep this 'if' to not lose the TESTFLIGHT case)
        if (howManyTimerRotationsLeft == 1) {

            //check if there are any players left
            synchronized (gameData.getUnorderedPlayers()) {
                if(gameData.getPlayers().isEmpty()){
                    GamesHandler gamesHandler = GamesHandler.getInstance();
                    gamesHandler.getGames().remove(gamesHandler.getGame(this.gameId));
                }
            }

            setTimerRunning(true);  // run for the last time
            // wait at most a total hourglass time (or get notified earlier by notifyAllPlayersEndedAssembly)
            synchronized (timerLock) {
                timerLock.wait(timerMilliseconds);
            }
            setTimerRunning(false);  // end for the last time
            // if here can be because time ended: delegated to the caller to force all the players to end assembly
            howManyTimerRotationsLeft = 0;
        }
        else if (howManyTimerRotationsLeft == 0) {  // TESTFLIGHT: forced to wait all the players
            synchronized (timerLock) {
                timerLock.wait();
            }
        }
        // else: arrives from previous if and notifyAllPlayersEndedAssembly has been called -> finish assembly directly
    }

    @Override
    public void startTimer(Player p) throws TimerIsAlreadyRunningException, CommandNotAllowedException {
        if (timerRunning) {
            throw new TimerIsAlreadyRunningException("You must wait for the hourglass to finish before flipping it.");
        }
        if (gameData.getLevel() == GameLevel.TESTFLIGHT) {
            throw new CommandNotAllowedException("start timer", "Time constraints are not present in a TESTFLIGHT game!");
        }
        // stop place is howManyTimerRotationsLeft == 1 -> last flip is howManyTimerRotationsLeft == 2
        if (howManyTimerRotationsLeft == 2 && !p.getShipBoard().isEndedAssembly()) {
            throw new CommandNotAllowedException("start timer",
                    "You must have first finished assembling the ship before performing the last flip.");
        }
        synchronized (timerLock) {
            timerLock.notifyAll();
        }
    }

    /**
     * Notify that all the players in the game ended assembly
     */
    public void notifyAllPlayersEndedAssembly() {
        // if all the players end assemble before the last timer reaches the end,
        // there is no need to continue turning the timer -> immediate stop
        howManyTimerRotationsLeft = 0;
        // then, default behavior: end assemble
        synchronized (timerLock) {
            timerLock.notifyAll();
        }
    }

    /**
     * Sets the callback to be executed every time a timer runs out or starts.
     * Needed when recreating this class from serialization since the runnable is not saved in serialization.
     * @param onTimerSwitchCallback the callback function.
     */
    public void setOnTimerSwitchCallback(Runnable onTimerSwitchCallback) {
        this.onTimerSwitchCallback = onTimerSwitchCallback;
    }

    /**
     * @return {@code true} if the timer is running, otherwise {@code false} if the timer expired and can be flipped
     */
    public boolean isTimerRunning() {
        return timerRunning;
    }

    /**
     * @return how many times the timer has already been restarted, if present; {@code null} otherwise.
     */
    public Integer getTimerSlotIndex() {
        if (totalTimerRotations > 0) {
            return totalTimerRotations - howManyTimerRotationsLeft;
        } else {
            return null;
        }
    }

}
