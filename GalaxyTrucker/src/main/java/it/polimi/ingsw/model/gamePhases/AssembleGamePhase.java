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
    private boolean autoTimerSequence;

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

    /**
     * Constructs a new PlayableGamePhase as {@link GamePhaseType#ASSEMBLE} by resuming a previous
     * {@link AssembleGamePhase}.
     * Every time a timer stops or gets resumed: executes {@code onTimerSwitchCallback}.
     *
     * @param toResume the previous {@link AssembleGamePhase} to resume.
     * @param onTimerSwitchCallback The callback function to be executed every time a timer switches state.
     */
    public AssembleGamePhase(AssembleGamePhase toResume, Runnable onTimerSwitchCallback) {
        super(GamePhaseType.ASSEMBLE, toResume.gameData);
        totalTimerRotations = GameLevelStandards.getTimerSlotsCount(gameData.getLevel());
        howManyTimerRotationsLeft = toResume.howManyTimerRotationsLeft;
        setOnTimerSwitchCallback(onTimerSwitchCallback);
        setTimerRunning(false);
    }

    private void setTimerRunning(boolean running) {
        timerRunning = running;
        // callback for timer ended or started
        if (onTimerSwitchCallback != null) {
            onTimerSwitchCallback.run();
        }
    }

    @Override
    public void setAutoTimerSequence(boolean autoTimerSequence) {
        this.autoTimerSequence = autoTimerSequence;
        // If enabling auto sequence and timer isn't running, trigger the next timer
        if (autoTimerSequence && !timerRunning && howManyTimerRotationsLeft > 0) {
            synchronized (timerLock) {
                timerLock.notifyAll();
            }
        }
    }

    public void playLoop() throws RemoteException, InterruptedException {
        if (gameData.getCurrentGamePhase() != this) {
            throw new RuntimeException("Trying to run a game phase which is not active on the game.");
        }

        while (howManyTimerRotationsLeft > 1) {
            GameServer.getInstance().broadcastUpdate(GamesHandler.getInstance().getGame(gameId));
            setTimerRunning(true);

            synchronized (timerLock) {
                timerLock.wait(timerMilliseconds);
            }
            if (howManyTimerRotationsLeft <= 0) {
                return;
            }

            setTimerRunning(false);

            if (!autoTimerSequence) {
                // Manual mode: wait for player input
                synchronized (timerLock) {
                    timerLock.wait();
                }
            } else {
                // Auto mode: immediately proceed to next timer
                Thread.sleep(100); // Small delay between timers
            }

            howManyTimerRotationsLeft -= 1;
        }

        // Last timer handling
        if (howManyTimerRotationsLeft == 1) {
            setTimerRunning(true);
            synchronized (timerLock) {
                timerLock.wait(timerMilliseconds);
            }
            setTimerRunning(false);
            howManyTimerRotationsLeft = 0;
        }
        else if (howManyTimerRotationsLeft == 0) {
            synchronized (timerLock) {
                timerLock.wait();
            }
        }
    }

    @Override
    public void startTimer(Player p) throws TimerIsAlreadyRunningException, CommandNotAllowedException {
        if (autoTimerSequence) {
            throw new CommandNotAllowedException("start timer", "Timer is in automatic sequence mode");
        }
        if (timerRunning) {
            throw new TimerIsAlreadyRunningException("You must wait for the hourglass to finish before flipping it.");
        }
        if (gameData.getLevel() == GameLevel.TESTFLIGHT) {
            throw new CommandNotAllowedException("start timer", "Time constraints are not present in a TESTFLIGHT game!");
        }
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
