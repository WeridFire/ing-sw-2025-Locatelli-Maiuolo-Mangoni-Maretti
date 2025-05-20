package it.polimi.ingsw.gamePhases;

import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.controller.cp.exceptions.CommandNotAllowedException;
import it.polimi.ingsw.gamePhases.exceptions.TimerIsAlreadyRunningException;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.util.GameLevelStandards;


public class AssembleGamePhase extends PlayableGamePhase {

    // TODO: reset timerMilliseconds = 60 * 1000L. This is for debug purposes
    private static final long timerMilliseconds = 5 * 1000L;  // 60 seconds
    transient private final Runnable onTimerSwitchCallback;

    private final int totalTimerRotations;
    private int howManyTimerRotationsLeft;
    private long runningMillis = 0;
    private boolean expired = false;

    /**
     * Constructs a new PlayableGamePhase as {@link GamePhaseType#ASSEMBLE}.
     * Every time a timer stops or gets resumed: executes {@code onTimerSwitchCallback}.
     *
     * @param gameData The game data.
     * @param onTimerSwitchCallback The callback function to be executed every time a timer switches state.
     */
    public AssembleGamePhase(GameData gameData, Runnable onTimerSwitchCallback) {
        super(GamePhaseType.ASSEMBLE, gameData);
        this.onTimerSwitchCallback = onTimerSwitchCallback;

        totalTimerRotations = GameLevelStandards.getTimerSlotsCount(gameData.getLevel());
        howManyTimerRotationsLeft = totalTimerRotations;
        setTimerRunning(false);
    }

    private void setTimerRunning(boolean running) {
        runningMillis = running ? timerMilliseconds : 0;
        // callback for timer ended or started
        if (onTimerSwitchCallback != null) {
            onTimerSwitchCallback.run();
        }
    }

    private boolean isWaitingAll() {
        return totalTimerRotations == 0;
    }

    public void start() {
        if (howManyTimerRotationsLeft > 1) {
            setTimerRunning(true);
        }
    }

    public void update(long deltaTimeMillis) {
        // 2 behaviors: 1. waiting all...
        if (isWaitingAll()) {
            if (gameData.getPlayer(p -> p.getPosition() == null) == null) {
                expired = true;
            }
            return;
        }
        // ... or 2. waiting the end of the last timer
        if (isTimerRunning()) {
            runningMillis -= deltaTimeMillis;
            if (runningMillis <= 0) {
                setTimerRunning(false);
            }
        }
        if (howManyTimerRotationsLeft == 0) {
            expired = true;
        }
    }

    @Override
    public boolean isExpired() {
        return expired;
    }

    private void toggleTimer() {
        howManyTimerRotationsLeft -= 1;
        if (howManyTimerRotationsLeft > 0) {
            setTimerRunning(true);
        }
    }

    /**
     * {@inheritDoc}
     * These are the possible commands:
     * 1. "timer" -> to toggle timer. Needs {@code sender != null}, and to be the player that tried to flip the timer.
     * 2. "expire" -> to end this phase forcefully.
     */
    @Override
    public void command(Player sender, String command, String[] args) throws CommandNotAllowedException {
        switch (command) {
            case "timer" -> {
                if (isTimerRunning()) {
                    throw new TimerIsAlreadyRunningException("You must wait for the hourglass to finish before flipping it.");
                }
                if (gameData.getLevel() == GameLevel.TESTFLIGHT) {
                    throw new CommandNotAllowedException("start timer", "Time constraints are not present in a TESTFLIGHT game!");
                }
                // stop place is howManyTimerRotationsLeft == 1 -> last flip is howManyTimerRotationsLeft == 2
                if (howManyTimerRotationsLeft == 2 && !sender.getShipBoard().isEndedAssembly()) {
                    throw new CommandNotAllowedException("start timer",
                            "You must have first finished assembling the ship before performing the last flip.");
                }
                // if here: ok to toggle timer
                toggleTimer();
            }

            case "expire" -> expired = true;

            case null, default -> super.command(sender, command, args);
        }
    }

    /**
     * @return {@code true} if the timer is running, otherwise {@code false} if the timer expired and can be flipped
     */
    public boolean isTimerRunning() {
        return runningMillis > 0;
    }

    /**
     * @return how many times the timer has already been restarted, if present; {@code null} otherwise.
     */
    public Integer getAssemblyTimerSlotIndex() {
        if (totalTimerRotations > 0) {
            return totalTimerRotations - howManyTimerRotationsLeft;
        } else {
            return null;
        }
    }

}
