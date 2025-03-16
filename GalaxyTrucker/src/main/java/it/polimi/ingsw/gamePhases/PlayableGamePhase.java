package src.main.java.it.polimi.ingsw.gamePhases;

import src.main.java.it.polimi.ingsw.enums.GamePhaseType;
import src.main.java.it.polimi.ingsw.enums.GameState;
import src.main.java.it.polimi.ingsw.game.GameData;
import src.main.java.it.polimi.ingsw.timer.TimerBehavior;

import java.util.UUID;

/**
 * Represents an abstract playable game phase.
 * Defines the structure for different game phases that can be played.
 */
public abstract class PlayableGamePhase implements TimerBehavior {

    /** The type of the game phase. */
    GamePhaseType gamePhaseType;

    /** The current state of the game. */
    GameState gameState;

    /** The unique identifier of the game. */
    UUID gameId;

    /** The game data associated with this phase. */
    GameData gameData;

    /**
     * Constructs a new PlayableGamePhase.
     *
     * @param gameId The unique identifier of the game.
     * @param gamePhaseType The type of the game phase.
     * @param gameState The current state of the game.
     * @param gameData The game data.
     */
    public PlayableGamePhase(UUID gameId, GamePhaseType gamePhaseType, GameState gameState, GameData gameData) {
        this.gameId = gameId;
        this.gamePhaseType = gamePhaseType;
        this.gameState = gameState;
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
    public abstract void playLoop();
}
