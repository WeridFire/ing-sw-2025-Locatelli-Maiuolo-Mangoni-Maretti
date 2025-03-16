package src.main.java.it.polimi.ingsw.game;

import src.main.java.it.polimi.ingsw.TilesFactory;
import src.main.java.it.polimi.ingsw.cards.Card;
import src.main.java.it.polimi.ingsw.enums.CargoType;
import src.main.java.it.polimi.ingsw.enums.GameLevel;
import src.main.java.it.polimi.ingsw.enums.GamePhaseType;
import src.main.java.it.polimi.ingsw.game.exceptions.PlayerAlreadyInGameException;
import src.main.java.it.polimi.ingsw.game.exceptions.PlayerNotInGameException;
import src.main.java.it.polimi.ingsw.gamePhases.PlayableGamePhase;
import src.main.java.it.polimi.ingsw.player.Player;
import src.main.java.it.polimi.ingsw.shipboard.ShipBoard;
import src.main.java.it.polimi.ingsw.shipboard.tiles.Tile;

import java.util.ArrayList;
import java.util.HashMap;

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
    private ArrayList<Player> players;

    /** The player whose turn it is. */
    private Player turn;

    /** Mapping of players to their respective ship boards. */
    private HashMap<Player, ShipBoard> shipFromPlayer;

    /** Mapping of available cargo goods and their quantities. */
    private HashMap<CargoType, Integer> availableGoods;

    /** List of game cards. */
    private ArrayList<Card> cards;

    /** List of covered tiles in the game. */
    private ArrayList<Tile> coveredTiles;

    /**
     * Constructs a new GameData object with the specified game level.
     *
     * @param level The game level.
     */
    public GameData(GameLevel level) {
        this.level = level;
        this.players = new ArrayList<>();
        this.shipFromPlayer = new HashMap<>();
        this.availableGoods = new HashMap<>();
        this.coveredTiles = new ArrayList<>();
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
    public ArrayList<Player> getPlayers() {
        return players;
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
     * Gets the list of game cards.
     *
     * @return The list of cards.
     */
    public ArrayList<Card> getCards() {
        return cards;
    }

    /**
     * Gets the list of covered tiles.
     *
     * @return The list of covered tiles.
     */
    public ArrayList<Tile> getCoveredTiles() {
        return coveredTiles;
    }

    /**
     * Gets the ship board of a specific player.
     *
     * @param player The player whose ship board is requested.
     * @return The ship board of the specified player.
     */
    public ShipBoard getPlayerShipBoard(Player player) {
        return shipFromPlayer.get(player);
    }

    /**
     * Gets the quantity of a specific cargo type.
     *
     * @param cargoType The type of cargo.
     * @return The quantity of the specified cargo type.
     */
    public Integer getCargo(CargoType cargoType) {
        return availableGoods.get(cargoType);
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
     * Sets the list of game cards.
     *
     * @param cards The new list of cards.
     */
    public void setCards(ArrayList<Card> cards) {
        this.cards = cards;
    }

    /**
     * Sets the list of covered tiles.
     *
     * @param coveredTiles The new list of covered tiles.
     */
    public void setCoveredTiles(ArrayList<Tile> coveredTiles) {
        this.coveredTiles = coveredTiles;
    }

    /**
     * Sets the list of players in the game.
     *
     * @param players The new list of players.
     */
    public void setPlayers(ArrayList<Player> players) {
        this.players = players;
    }

    /**
     * Adds a player to the game.
     *
     * @param player The player to add.
     * @throws PlayerAlreadyInGameException If the player is already in the game.
     */
    public void addPlayer(Player player) throws PlayerAlreadyInGameException {
        if(players.contains(player)) {
            throw new PlayerAlreadyInGameException("Player already added to the game");
        }
        players.add(player);
    }

    /**
     * Removes a player from the game.
     *
     * @param player The player to remove.
     * @throws PlayerNotInGameException If the player is not in the game.
     */
    public void removePlayer(Player player) {
        if (!players.contains(player)) {
            throw new PlayerNotInGameException("Player not in the game");
        }
        players.remove(player);
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
     * Initializes default game settings, including covered tiles and cards.
     */
    public void initDefaults(){
        this.coveredTiles = initDefaultTiles();
        this.cards = initDefaultCards();
    }

    /**
     * Initializes default game cards.
     *
     * @return A list of default game cards.
     */
    private ArrayList<Card> initDefaultCards(){
        // TBD waiting factory
        return new ArrayList<>();
    }

    /**
     * Initializes default covered tiles.
     *
     * @return A list of default covered tiles.
     */
    private ArrayList<Tile> initDefaultTiles(){
        return new ArrayList<>(TilesFactory.createPileTiles());
    }
}
