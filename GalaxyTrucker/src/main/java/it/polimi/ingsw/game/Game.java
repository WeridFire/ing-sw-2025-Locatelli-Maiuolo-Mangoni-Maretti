package src.main.java.it.polimi.ingsw.game;

import src.main.java.it.polimi.ingsw.player.Player;
import src.main.java.it.polimi.ingsw.timer.Timer;

import java.util.UUID;

/**
 * Represents a game instance with a unique identifier, game data, and a timer.
 */
public class Game {

    /**
     * Unique identifier for the game.
     */
    private final UUID id;

    /**
     * The game data associated with this game instance.
     */
    private GameData gameData;

    /**
     * Timer for managing game-related events.
     */
    private final Timer timer;

    /**
     * Creates a new game instance with a randomly generated UUID and a Timer.
     */
    public Game() {
        this.id = UUID.randomUUID();
        this.timer = new Timer();
    }

    /**
     * Returns the unique identifier of this game.
     *
     * @return the {@code UUID} of the game
     */
    public UUID getId() {
        return id;
    }

    /**
     * Returns the timer of this game.
     *
     * @return the {@code Timer} of the game
     */
    public Timer getTimer() {
        return timer;
    }

    /**
     * Returns the game data of this game.
     * @return the {@code GameData} of the game.
     */
    public GameData getGameData() {return gameData;}

    /**
     * Starts and manages the game loop.
     * <p>
     * This method is currently a placeholder and needs to be implemented.
     * </p>
     */
    public void gameLoop() {
        // TBD (To Be Determined)
    }

    /**
     * Loads game data into the current game instance.
     *
     * @param gameData the game data to load
     * @return {@code true} if the game data was successfully loaded, {@code false} if the provided data is {@code null}
     */
    public boolean loadGameData(GameData gameData) {
        if (gameData == null) return false;

        this.gameData = gameData;
        return true;
    }

    public Player getPlayerByConnection(UUID connection){
        return gameData.getPlayers().stream()
                                    .filter((player) -> player.getConnectionUUID() == connection)
                                    .findFirst().orElse(null);
    }



}
