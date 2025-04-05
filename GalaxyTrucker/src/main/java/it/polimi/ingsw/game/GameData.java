package it.polimi.ingsw.game;

import it.polimi.ingsw.cards.Deck;
import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.game.exceptions.DrawTileException;
import it.polimi.ingsw.game.exceptions.PlayerAlreadyInGameException;
import it.polimi.ingsw.gamePhases.PlayableGamePhase;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.player.exceptions.AlreadyHaveTileInHandException;
import it.polimi.ingsw.playerInput.PIRs.PIRHandler;
import it.polimi.ingsw.shipboard.LoadableType;
import it.polimi.ingsw.shipboard.SideType;
import it.polimi.ingsw.shipboard.tiles.Tile;
import it.polimi.ingsw.shipboard.tiles.TileSkeleton;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents the game data.
 */
public class GameData implements Serializable {

    /** The game level configuration. */
    private GameLevel level;

    /** The current phase type of the game. */
    private GamePhaseType currentGamePhaseType;

    /** The current playable game phase. */
    private PlayableGamePhase currentGamePhase;

    /** List of players in the game. */
    private final Set<Player> players;

    /** Number of positions in 1 lap */
    private int lapSize;

    private UUID gameId;

    /** The player whose turn it is.
    private Player currentPlayerTurn;
     */

    transient private PIRHandler pirHandler;

    /** Mapping of available cargo goods and their quantities. */
    private Map<LoadableType, Integer> availableGoods;

    /** List of game deck. */
    private Deck deck;

    /** List of covered tiles in the game. */
    private List<TileSkeleton> coveredTiles;

    /** List of drawn tiles I.E. all the tiles that:
     *  - are not part of a shipboard
     *  - are not covered
     *  - are not in hand
     *  - are not reserved by anybody
     */
    private List<TileSkeleton> drawnTiles;

    private int requiredPlayers;

    private String gameLeader;

    /**
     * Constructs a new GameData object with a default game level.
     */
    public GameData(UUID gameId) {
        this.players = new HashSet<>();
        this.availableGoods = new HashMap<>();
        this.coveredTiles = new ArrayList<>();
        this.deck = null;
        this.gameId = gameId;
        this.pirHandler = new PIRHandler();
        this.level = GameLevel.TESTFLIGHT;
        this.setCurrentGamePhaseType(GamePhaseType.LOBBY);
        this.setRequiredPlayers(4);
    }

    /**
     * Gets the game level.
     *
     * @return The game level.
     */
    public GameLevel getLevel() {
        return level;
    }

    public void setLevel(GameLevel level){
        this.level = level;
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
    public PIRHandler getPIRHandler(){
        return pirHandler;
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
    public List<TileSkeleton> getCoveredTiles() {
        return coveredTiles;
    }

    /**
     * Gets the list of uncovered tiles.
     *
     * @return The list of uncovered tiles.
     */
    public List<TileSkeleton> getDrawnTiles() {
        return drawnTiles;
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
    public void setCoveredTiles(List<TileSkeleton> coveredTiles) {
        this.coveredTiles = coveredTiles;
    }


    /**
     * Adds a player to the game. If the player was the first to be added, they will be considered
     * the game leader, and will be assigned to the field. Game leader can change game settings.
     *
     * @param player The player to add.
     * @throws PlayerAlreadyInGameException If the player is already in the game.
     */
    protected void addPlayer(Player player) throws PlayerAlreadyInGameException {
        if(players.stream()
                    .map(Player::getUsername)
                    .collect(Collectors.toSet())
                    .contains(player.getUsername())) {
            throw new PlayerAlreadyInGameException("Player with this username is already present.");
        }
        players.add(player);
        if(players.size() == 1){
            this.gameLeader = player.getUsername();
        }
    }

    /**
     * Moves a player on the board, accounting for players they may pass.
     * @param playerToMove player that is going to move
     * @param steps number of steps the player is moving
     * @param forward true if moving forward, false if moving backward
     */
    private void movePlayer(Player playerToMove, int steps, boolean forward) {

        // HashSet for fast position lookup
        Set<Integer> occupiedPositions = new HashSet<>();
        List<Player> currentOrder = getPlayers();
        //TODO: SYNCHRONIZE! WE WANT TO MOVE ONLY 1 PLAYER AT A TIME SO SYNC ON THE OBJECT
        for (Player player : currentOrder) {
            if (!player.getUsername().equals(playerToMove.getUsername())) {
                occupiedPositions.add(player.getPosition());
            }
        }

        int newPosition = playerToMove.getPosition();
        int stepsLeft = steps;

        while (stepsLeft > 0) {
            newPosition += (forward ? 1 : -1); // Move forward or backward
            stepsLeft--; // Decrease steps left

            // Add a step if a player is passed
            if (occupiedPositions.contains(newPosition)) {
                stepsLeft++;
            }
        }
        playerToMove.setPosition(newPosition);
    }

    /**
     * Moves a player forward on the board.
     * @param playerToMove player that is going to move
     * @param steps number of steps the player is moving
     */
    public void movePlayerForward(Player playerToMove, int steps) {
        movePlayer(playerToMove, steps, true);
    }

    /**
     * Moves a player backward on the board.
     * @param playerToMove player that is going to move
     * @param steps number of steps the player is moving
     */
    public void movePlayerBackward(Player playerToMove, int steps) {
        movePlayer(playerToMove, steps, false);
    }

    public void setCurrentGamePhaseType(GamePhaseType currentGamePhaseType) {
        this.currentGamePhaseType = currentGamePhaseType;
    }

    public int getRequiredPlayers() {
        return requiredPlayers;
    }

    public void setRequiredPlayers(int requiredPlayers) {
        if(requiredPlayers > 4 || requiredPlayers < 2){
            return;
        }
        this.requiredPlayers = requiredPlayers;
    }

    public void setLapSize(int lapSize) {
        this.lapSize = lapSize;
    }

    public UUID getGameId() {
        return gameId;
    }

    public String getGameLeader() {
        return gameLeader;
    }
}
