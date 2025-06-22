package it.polimi.ingsw;

import it.polimi.ingsw.game.Game;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.game.exceptions.ColorAlreadyInUseException;
import it.polimi.ingsw.game.exceptions.GameAlreadyRunningException;
import it.polimi.ingsw.game.exceptions.PlayerAlreadyInGameException;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.shipboard.tiles.MainCabinTile;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.UUID;

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
     * @param desiredColor The color this player wants to use for the game
     * @return The created game.
     * @throws PlayerAlreadyInGameException The player is in another game.
     */
    public Game createGame(String username, UUID connectionUUID, MainCabinTile.Color desiredColor)
            throws PlayerAlreadyInGameException {
        if(findGameByClientUUID(connectionUUID) != null){
            throw new PlayerAlreadyInGameException(username);
        }
        Game createdGame = startGame();
        games.add(createdGame);

        try {
            createdGame.addPlayer(username, connectionUUID, desiredColor);
        } catch (GameAlreadyRunningException | ColorAlreadyInUseException e) {
            throw new RuntimeException(e);  // should never happen -> runtime exception
        }
        return createdGame;
	}


    /**
     * Command to resume an existing game. Takes as argument the game state and builds a game in the gamehandler. Then
     * makes the player that wanted to resume it connect as the game leader.
     * @return the created game object.
     */
    public Game resumeGame(GameData savedGameState, UUID connectionUUID) throws PlayerAlreadyInGameException,
            GameAlreadyRunningException {
        Game createdGame = new Game(savedGameState);
        if(games.stream().anyMatch((g) -> g.getId().equals(savedGameState.getGameId()))){
            throw new GameAlreadyRunningException(createdGame.getId());
        }
        games.add(createdGame);
        try {
            createdGame.addPlayer(savedGameState.getGameLeader(), connectionUUID, null);
        } catch (ColorAlreadyInUseException e) {
            throw new RuntimeException(e);  // should never happen -> runtime exception
        }
        startGame(createdGame);

        return createdGame;
    }

    public Player getPlayerByConnection(UUID clientUUID){
        Game playerGame = findGameByClientUUID(clientUUID);
        if(playerGame == null){
            return null;
        }
        return playerGame.getGameData().getPlayer(player -> player.isConnected() && player.getConnectionUUID() == clientUUID);
    }

    public Game findGameByClientUUID(UUID clientUUID) {
        return getGames().stream()
                .filter(game ->
                        game.getGameData().getPlayer(p ->
                                p.isConnected() &&
                                p.getConnectionUUID().equals(clientUUID)) != null)
                .findFirst().orElse(null);
    }

    public ArrayList<Game> getGames() {
        return games;
    }

}
