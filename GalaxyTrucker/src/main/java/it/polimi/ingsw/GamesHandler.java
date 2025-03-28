package src.main.java.it.polimi.ingsw;

import src.main.java.it.polimi.ingsw.game.Game;
import src.main.java.it.polimi.ingsw.game.exceptions.PlayerAlreadyInGameException;
import src.main.java.it.polimi.ingsw.player.Player;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Singleton class for managing game creation and retrieval.
 * Maintains a list of games and provides methods to create new games
 * and retrieve them by a unique identifier.
 */
public class GamesHandler {

    /**
     * The single instance of {@code GamesHandler}.
     */
    private static GamesHandler instance;

    /**
     * List of games managed by the instance.
     */
    private final ArrayList<Game> games;

    /**
     * Private constructor to prevent external instantiation.
     * Initializes the list of games.
     */
    private GamesHandler() {
        games = new ArrayList<>();
    }

    /**
     * Returns the singleton instance of {@code GamesHandler}.
     * Ensures thread safety using synchronized access.
     *
     * @return the single instance of {@code GamesHandler}
     */
    public static synchronized GamesHandler getInstance() {
        if (instance == null) {
            instance = new GamesHandler();
        }
        return instance;
    }

    /**
     * Creates a new game, adds it to the list, and returns it.
     *
     * @return the newly created {@code Game} instance
     */
    public Game newGame() {
        Game game = new Game();
        games.add(game);
        return game;
    }

    /**
     * Retrieves a game by its unique identifier.
     *
     * @param id the {@code UUID} of the game to retrieve
     * @return the corresponding {@code Game} if found, otherwise {@code null}
     */
    public Game getGame(UUID id) {
        for (Game game : games) {
            if (game.getId().equals(id)) {
                return game;
            }
        }
        return null;
    }

    public Game addPlayerToGame(String username, UUID gameId, UUID connectionUUID){
        Game target = getGame(gameId);
        if(target == null){
            target = newGame();
            return addPlayerToGame(username, target.getId(), connectionUUID);
        }
        Set<String> usernames = target.getGameData().getPlayers().stream()
                                                            .map(Player::getUsername)
                                                            .collect(Collectors.toSet());
        if(!usernames.contains(username)){
            try {
                target.addPlayer(new Player(username, connectionUUID));
                return target;
            } catch (PlayerAlreadyInGameException e) {
                throw new RuntimeException(e);  // should never happen (already checked username is not in usernames) -> runtime error
            }
        }
        return null;
    }

    public ArrayList<Game> getGames() {
        return games;
    }
}
