package it.polimi.ingsw;

import it.polimi.ingsw.game.Game;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.game.exceptions.GameAlreadyRunningException;
import it.polimi.ingsw.game.exceptions.GameNotFoundException;
import it.polimi.ingsw.game.exceptions.PlayerAlreadyInGameException;
import it.polimi.ingsw.player.Player;

import java.rmi.RemoteException;
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
    public Game startGame() {
        Game game = new Game();
        return startGame(game);
    }


    /**
     * Creates a new game, adds it to the list, starts it on a separate thread, and returns it.
     * @param game The game to start.
     * @return The game object passed.
     */
    public Game startGame(Game game) {
        //Instantiate a thread that handles that specific game.
        Thread t = new Thread(() -> {
            try {
                game.gameLoop();
            } catch (InterruptedException e) {
                System.out.println("Interrupted game: " + game.getId());
            }catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            finally {
                // once the game has ended due to any circumstance, remove it from the games list
                games.remove(game);
            }
        });
        game.setGameThread(t); //setting it also starts it.
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


    /**
     * Creates a game and adds the player into the game. Makes sure that the player is not already in another game.
     * @param username The username the player wants to join with.
     * @param connectionUUID The connection id of the player
     * @return The created game.
     * @throws PlayerAlreadyInGameException The player is in another game.
     */
    public Game createGame(String username, UUID connectionUUID) throws PlayerAlreadyInGameException {
        if(findGameByClientUUID(connectionUUID) != null){
            throw new PlayerAlreadyInGameException(username);
        }
        Game createdGame = startGame();
        games.add(createdGame);

        createdGame.addPlayer(username, connectionUUID);
        return createdGame;
	}


    /**
     * Command to start a new game. Makes the gamehandler basically "forcestart" the saved gamestate, and automatically
     * connects the player that is resuming to it, as a leader.
     * @param savedGameState The saved game state to load.
     * @param connectionUUID The connection fo the player that sent the command.
     * @return The newly game object.
     */
    public Game resumeGame(GameData savedGameState, UUID connectionUUID) throws PlayerAlreadyInGameException, GameAlreadyRunningException {
        Game createdGame = new Game(savedGameState);
        if(games.stream().anyMatch((g) -> g.getId().equals(savedGameState.getGameId()))){
            throw new GameAlreadyRunningException(createdGame.getId());
        }
        games.add(createdGame);
        startGame(createdGame);

        createdGame.addPlayer(savedGameState.getGameLeader(), connectionUUID);

        return createdGame;
    }

    public Player getPlayerByConnection(UUID clientUUID){
        Game playerGame = findGameByClientUUID(clientUUID);
        if(playerGame == null){
            return null;
        }
        return playerGame.getGameData().getPlayer(player -> player.getConnectionUUID() == clientUUID);
    }

    public Game findGameByClientUUID(UUID clientUUID) {
        return GamesHandler.getInstance().getGames().stream()
                .filter(game -> game.getGameData().getPlayer(p -> p.getConnectionUUID() == clientUUID) != null)
                .findFirst().orElse(null);
    }

    public ArrayList<Game> getGames() {
        return games;
    }

}
