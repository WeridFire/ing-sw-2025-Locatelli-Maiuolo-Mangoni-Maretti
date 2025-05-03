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
            throw new PlayerAlreadyInGameException("You already are in a game.");
        }
        Game createdGame = startGame();
        games.add(createdGame);
		try {
			addPlayerToGame(username, createdGame.getId(), connectionUUID);
		} catch (GameNotFoundException e) {
			throw new RuntimeException(e.getMessage());
		}
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
        startGame(createdGame);
        try {
            addPlayerToGame(savedGameState.getGameLeader(), createdGame.getId(), connectionUUID);
        } catch (GameNotFoundException e) {
            throw new RuntimeException(e.getMessage());
        }
        return createdGame;
    }


    /**
     * Adds a player into an existing game.
     * @param username The username the player wants to join with.
     * @param gameId The game UUID, must exist.
     * @param connectionUUID The connection UUID.
     * @return The joined game
     * @throws PlayerAlreadyInGameException The username is already present in the game.
     * @throws GameNotFoundException The game does not exist.
     */
    public Game addPlayerToGame(String username, UUID gameId, UUID connectionUUID) throws PlayerAlreadyInGameException, GameNotFoundException {
        Game target = getGame(gameId);
        if(target == null){
            throw new GameNotFoundException(gameId);
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
        }else{
            throw new PlayerAlreadyInGameException("This username is already in the game.");
        }
    }

    public Player getPlayerByConnection(UUID clientUUID){
        Game playerGame = findGameByClientUUID(clientUUID);
        if(playerGame == null){
            return null;
        }
        return playerGame.getGameData().getPlayers().stream()
                .filter((player) -> player.getConnectionUUID() == clientUUID)
                .findFirst()
                .orElse(null);
    }

    public Game findGameByClientUUID(UUID clientUUID) {
        return GamesHandler.getInstance().getGames()
                .stream()
                .filter(game -> {
                    Set<UUID> playerUUIDs = game.getGameData().getPlayers()
                            .stream()
                            .map(Player::getConnectionUUID)
                            .collect(Collectors.toSet());
                    return playerUUIDs.contains(clientUUID);
                })
                .findFirst().orElse(null);
    }

    public ArrayList<Game> getGames() {
        return games;
    }

}
