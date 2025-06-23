package it.polimi.ingsw.gamePhases;

import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.controller.commandsProcessors.exceptions.CommandNotAllowedException;
import it.polimi.ingsw.gamePhases.exceptions.TimerIsAlreadyRunningException;
import it.polimi.ingsw.network.exceptions.CantFindClientException;
import it.polimi.ingsw.player.Player;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.UUID;

/**
 * Represents an abstract playable game phase.
 * Defines the structure for different game phases that can be played.
 */
public abstract class PlayableGamePhase implements Serializable {

    /** The type of the game phase. */
    protected final GamePhaseType gamePhaseType;

    /** The unique identifier of the game. */
    protected final UUID gameId;

    /** The game data associated with this phase. */
    protected final GameData gameData;

    /**
     * Constructs a new PlayableGamePhase.
     *
     * @param gamePhaseType The type of the game phase.
     * @param gameData The game data.
     */
    public PlayableGamePhase(GamePhaseType gamePhaseType, GameData gameData) {
        this.gamePhaseType = gamePhaseType;
        this.gameData = gameData;
        gameId = gameData.getGameId();
    }

    /**
     * Gets the type of the game phase.
     *
     * @return The game phase type.
     */
    public GamePhaseType getGamePhaseType() {
        return gamePhaseType;
    }

    /**
     * Defines the main gameplay loop for this phase.
     * To be implemented by subclasses.
     */
    public abstract void playLoop() throws RemoteException, CantFindClientException, InterruptedException;

    /**
     * Used to implement starting timer logic
     * @param p The player who flipped the timer.
     * */
    public void startTimer(Player p) throws TimerIsAlreadyRunningException, CommandNotAllowedException {
        throw new CommandNotAllowedException(
                "startTimer",
                "The timer is not available for an phase of type " + getGamePhaseType()
        );
    }
}
