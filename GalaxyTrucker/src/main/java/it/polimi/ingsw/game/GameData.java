package src.main.java.it.polimi.ingsw.game;

import src.main.java.it.polimi.ingsw.TilesFactory;
import src.main.java.it.polimi.ingsw.cards.Deck;
import src.main.java.it.polimi.ingsw.enums.GameLevel;
import src.main.java.it.polimi.ingsw.enums.GamePhaseType;
import src.main.java.it.polimi.ingsw.game.exceptions.PlayerAlreadyInGameException;
import src.main.java.it.polimi.ingsw.game.exceptions.PlayerNotInGameException;
import src.main.java.it.polimi.ingsw.gamePhases.PlayableGamePhase;
import src.main.java.it.polimi.ingsw.player.Player;
import src.main.java.it.polimi.ingsw.shipboard.LoadableType;
import src.main.java.it.polimi.ingsw.shipboard.SideType;
import src.main.java.it.polimi.ingsw.shipboard.tiles.TileSkeleton;
import src.main.java.it.polimi.ingsw.shipboard.ShipBoard;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents the game data.
 */
public class GameData {

    /** The game level configuration. */
    private final GameLevel level;

    /** The current phase type of the game. */
    private GamePhaseType currentGamePhaseType;

    /** The current playable game phase. */
    private PlayableGamePhase currentGamePhase;

    /** List of players in the game. */
    private final Set<Player> players;

    /** Number of positions in 1 lap */
    private int lapSize;

    /** The player whose turn it is. */
    private Player turn;

    /** Mapping of available cargo goods and their quantities. */
    private HashMap<LoadableType, Integer> availableGoods;

    /** List of game deck. */
    private Deck deck;

    /** List of covered tiles in the game. */
    private ArrayList<TileSkeleton<SideType>> coveredTiles;

    /**
     * Constructs a new GameData object with the specified game level.
     *
     * @param level The game level.
     */
    public GameData(GameLevel level) {
        this.level = level;
        this.players = new HashSet<>();
        this.availableGoods = new HashMap<>();
        this.coveredTiles = new ArrayList<TileSkeleton<SideType>>();
        this.deck = null;
    }

    /**
     * Gets the game level.
     *
     * @return The game level.
     */
    public GameLevel getLevel() {
        return level;
    }

    /**
     * Gets the current game phase type.
     *
     * @return The current game phase type.
     */
    public GamePhaseType getCurrentGamePhaseType() {
        return currentGamePhaseType;
    }

    /**
     * Gets the current game phase.
     *
     * @return The current playable game phase.
     */
    public PlayableGamePhase getCurrentGamePhase() {
        return currentGamePhase;
    }

    /**
     * Gets the list of players in the game.
     *
     * @return The list of players.
     */
    public List<Player> getPlayers() {
        return players.stream()
                        .sorted(Comparator.comparingInt(Player::getPosition))
                        .toList();
    }

    /**
     * Gets the player whose turn it is.
     *
     * @return The current turn player.
     */
    public Player getTurn() {
        return turn;
    }

    /**
     * Gets the list of game deck.
     *
     * @return The list of deck.
     */
    public Deck getDeck() {
        return deck;
    }

    /**
     * Gets the list of covered tiles.
     *
     * @return The list of covered tiles.
     */
    public ArrayList<TileSkeleton<SideType>> getCoveredTiles() {
        return coveredTiles;
    }

    /**
     * Gets the quantity of a specific cargo type.
     *
     * @param LoadableType The type of cargo.
     * @return The quantity of the specified cargo type.
     */
    public Integer getCargo(LoadableType LoadableType) {
        return availableGoods.get(LoadableType);
    }

    /**
     * Sets the current game phase and updates the game phase type.
     *
     * @param currentGamePhase The new current game phase.
     */
    public void setCurrentGamePhase(PlayableGamePhase currentGamePhase) {
        this.currentGamePhase = currentGamePhase;
        this.currentGamePhaseType = currentGamePhase.getGamePhaseType();
    }

    /**
     * Sets the list of game deck.
     *
     * @param deck The new list of deck.
     */
    public void setDeck(Deck deck) {
        this.deck = deck;
    }

    /**
     * Sets the list of covered tiles.
     *
     * @param coveredTiles The new list of covered tiles.
     */
    public void setCoveredTiles(ArrayList<TileSkeleton<SideType>> coveredTiles) {
        this.coveredTiles = coveredTiles;
    }


    /**
     * Adds a player to the game.
     *
     * @param player The player to add.
     * @throws PlayerAlreadyInGameException If the player is already in the game.
     */
    public void addPlayer(Player player) throws PlayerAlreadyInGameException {
        if(players.stream()
                    .map(Player::getUsername)
                    .collect(Collectors.toSet())
                    .contains(player.getUsername())) {
            throw new PlayerAlreadyInGameException("Player with this username is already present.");
        }
        players.add(player);
    }


    /**
     * Sets the current turn player.
     *
     * @param turn The player whose turn it is.
     */
    private void setTurn(Player turn) {
        this.turn = turn;
    }

    /**
     * Initializes default game settings, including covered tiles and deck.
     */
    public void initDefaults(){
        this.coveredTiles = initDefaultTiles();
        //this.deck = initDefaultCards();
    }

    /**
     * Initializes default covered tiles.
     *
     * @return A list of default covered tiles.
     */
    private ArrayList<TileSkeleton<SideType>> initDefaultTiles(){
        return new ArrayList<>(TilesFactory.createPileTiles());
    }

    /**
     * Moves a player forward on the board, accounting for players he may pass
     * @param playerToMove player that is going to move
     * @param steps number of steps the player is moving
     */
    public void movePlayerForward(Player playerToMove, int steps) {

        // HashSet for fast position lookup
        Set<Integer> occupiedPositions = new HashSet<>();
        for (Player player : players) {
            if (!player.getUsername().equals(playerToMove.getUsername())) {
                occupiedPositions.add(player.getPosition());
            }
        }

        int newPosition = playerToMove.getPosition();
        int stepsLeft = steps;

        while (stepsLeft > 0) {
            newPosition++; // Move one step back
            stepsLeft--;   // Decrease steps left

            // Add a step if a player is passed
            if (occupiedPositions.contains(newPosition)) {
                stepsLeft++;
            }
        }

        playerToMove.setPosition(newPosition);
    }

    /**
     * Moves a player backwards on the board, accounting for players he may pass
     * @param playerToMove player that is going to move
     * @param steps number of steps the player is moving
     */
    public void movePlayerBackward(Player playerToMove, int steps) {

        // HashSet for fast position lookup
        Set<Integer> occupiedPositions = new HashSet<>();
        for (Player player : players) {
            if (!player.getUsername().equals(playerToMove.getUsername())) {
                occupiedPositions.add(player.getPosition());
            }
        }

        int newPosition = playerToMove.getPosition();
        int stepsLeft = steps;

        while (stepsLeft > 0) {
            newPosition--; // Move one step back
            stepsLeft--;   // Decrease steps left

            // Add a step if a player is passed
            if (occupiedPositions.contains(newPosition)) {
                stepsLeft++;
            }
        }

        playerToMove.setPosition(newPosition);
    }
}
