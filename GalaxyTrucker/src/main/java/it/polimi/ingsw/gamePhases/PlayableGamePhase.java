package it.polimi.ingsw.gamePhases;

import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.network.exceptions.CantFindClientException;

import java.rmi.RemoteException;
import java.util.UUID;

/**
 * Represents an abstract playable game phase.
 * Defines the structure for different game phases that can be played.
 */
public abstract class PlayableGamePhase {

    /** The type of the game phase. */
    GamePhaseType gamePhaseType;


    /** The unique identifier of the game. */
    UUID gameId;

    /** The game data associated with this phase. */
    GameData gameData;

    /**
     * Constructs a new PlayableGamePhase.
     *
     * @param gameId The unique identifier of the game.
     * @param gamePhaseType The type of the game phase.
     * @param gameData The game data.
     */
    public PlayableGamePhase(UUID gameId, GamePhaseType gamePhaseType, GameData gameData) {
        this.gameId = gameId;
        this.gamePhaseType = gamePhaseType;
        this.gameData = gameData;
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
}
