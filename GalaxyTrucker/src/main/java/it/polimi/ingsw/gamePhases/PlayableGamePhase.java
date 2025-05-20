package it.polimi.ingsw.gamePhases;

import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.controller.cp.exceptions.CommandNotAllowedException;
import it.polimi.ingsw.player.Player;

import java.io.Serializable;
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
     * @return The game phase type.
     */
    public GamePhaseType getGamePhaseType() {
        return gamePhaseType;
    }

    /**
     * Defines the condition to consider this game phase "expired": no longer running / available.
     * Used to update the State Machine to the next state
     * @return {@code true} if and only if this state is no longer available
     */
    public abstract boolean isExpired();

    public void start() { }
    public void update(long deltaTimeMillis) { }

    /**
     * Used to implement a generic command received by a player
     * @param sender The player who sent the command
     * @param command The command received by the game phase, to act on its own state
     * @param args The command arguments to specify any other info in the parsing of the command
     * */
    public void command(Player sender, String command, String[] args) throws CommandNotAllowedException {
        throw new CommandNotAllowedException("The command '"+command+"' is not allowed at this level of the game model");
    }
}
