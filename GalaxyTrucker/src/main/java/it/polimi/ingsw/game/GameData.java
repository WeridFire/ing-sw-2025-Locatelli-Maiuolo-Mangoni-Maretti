package it.polimi.ingsw.game;

import it.polimi.ingsw.cards.Deck;
import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.game.exceptions.PlayerAlreadyInGameException;
import it.polimi.ingsw.gamePhases.AssembleGamePhase;
import it.polimi.ingsw.gamePhases.PlayableGamePhase;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.player.exceptions.NoShipboardException;
import it.polimi.ingsw.player.exceptions.TooManyItemsInHandException;
import it.polimi.ingsw.playerInput.PIRs.PIRHandler;
import it.polimi.ingsw.shipboard.LoadableType;
import it.polimi.ingsw.shipboard.exceptions.AlreadyEndedAssemblyException;
import it.polimi.ingsw.shipboard.exceptions.ThatTileIdDoesNotExistsException;
import it.polimi.ingsw.shipboard.tiles.TileSkeleton;
import it.polimi.ingsw.util.GameLevelStandards;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents the game data.
 */
public class GameData implements Serializable {

    /**
     * The game level configuration.
     */
    private GameLevel level;

    /**
     * The current phase type of the game.
     */
    private GamePhaseType currentGamePhaseType;

    /**
     * The current playable game phase.
     */
    private PlayableGamePhase currentGamePhase;

    /**
     * List of players in the game.
     */
    private final Set<Player> players;

    private final Set<Player> deadPlayers;

    /**
     * The list of starting position for players on the route board
     */
    private List<Integer> startingPositions;

    /**
     * The related game UUID to identify it among other games
     */
    private final UUID gameId;

    /**
     * The player whose turn it is.
     */
    private final PIRHandler pirHandler;

    /**
     * Mapping of available cargo goods and their quantities.
     */
    private final Map<LoadableType, Integer> availableGoods;

    /**
     * List of game deck.
     */
    private Deck deck;

    /**
     * List of covered tiles in the game.
     */
    private final List<TileSkeleton> coveredTiles;

    /**
     * List of drawn and discarded tiles I.E. all the tiles that:
     * - are not part of a shipboard
     * - are not covered
     * - are not in hand
     * - are not reserved by anybody
     */
    private final List<TileSkeleton> uncoveredTiles;

    /**
     * Used for synchronization of moving players
     */
    private final transient Object movementLock = new Object();

    private int requiredPlayers;

    private String gameLeader;

    /**
     * Constructs a new GameData object with a default game level.
     */
    public GameData(UUID gameId) {
        this.deadPlayers = new HashSet<>();
        this.gameId = gameId;
        players = new HashSet<>();
        availableGoods = new HashMap<>();
        coveredTiles = new ArrayList<>();
        uncoveredTiles = new ArrayList<>();
        deck = null;
        pirHandler = new PIRHandler();
        setLevel(GameLevel.TESTFLIGHT);
        setCurrentGamePhaseType(GamePhaseType.LOBBY);
        setRequiredPlayers(2);
    }

    /**
     * Gets the game level.
     *
     * @return The game level.
     */
    public GameLevel getLevel() {
        return level;
    }

    public void setLevel(GameLevel level) {
        this.level = level;

        // wrap in ArrayList constructor to let it be modifiable
        startingPositions = new ArrayList<>(GameLevelStandards.getFlightBoardParkingLots(level));
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
     * Gets the list of players in the game, sorted by route order (first is the leader).
     *
     * @return The list of players.
     */
    public List<Player> getPlayers() {
        return players.stream()
                .sorted(Comparator.comparingInt(Player::getOrder))
                .toList();
    }

    /**
     *
     * @return The unordered, original reference to the players object. Useful for locks.
     */
    public Set<Player> getUnorderedPlayers(){
        return players;
    }

    /**
     * Gets the player whose turn it is.
     *
     * @return The current turn player.
     */
    public PIRHandler getPIRHandler() {
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
    public List<TileSkeleton> getUncoveredTiles() {
        return uncoveredTiles;
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
        this.coveredTiles.clear();
        if (coveredTiles != null) {
            this.coveredTiles.addAll(coveredTiles);
        }
    }


    /**
     * Adds a player to the game. If the player was the first to be added, they will be considered
     * the game leader, and will be assigned to the field. Game leader can change game settings.
     *
     * @param player The player to add.
     * @throws PlayerAlreadyInGameException If the player is already in the game.
     */
    protected void addPlayer(Player player) throws PlayerAlreadyInGameException {
        if (players.stream()
                .map(Player::getUsername)
                .collect(Collectors.toSet())
                .contains(player.getUsername())) {
            throw new PlayerAlreadyInGameException("Player with this username is already present.");
        }
        synchronized (players){
            players.add(player);
            if (players.size() == 1) {
                this.gameLeader = player.getUsername();
            }

            if(players.size() >= requiredPlayers){
                //Awake main thread for starting game.
                players.notifyAll();
            }
        }

    }

    /**
     * Moves a player on the board, accounting for players they may pass.
     *
     * @param playerToMove player that is going to move
     * @param steps        number of steps the player is moving
     * @param forward      true if moving forward, false if moving backward
     */
    private void movePlayer(Player playerToMove, int steps, boolean forward) {

        synchronized (movementLock) {

            Set<Integer> occupiedPositions = new HashSet<>();
            List<Player> currentOrder = getPlayers();
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
    }

    /**
     * Moves a player forward on the board.
     *
     * @param playerToMove player that is going to move
     * @param steps        number of steps the player is moving
     */
    public void movePlayerForward(Player playerToMove, int steps) {
        movePlayer(playerToMove, steps, true);
    }

    /**
     * Moves a player backward on the board.
     *
     * @param playerToMove player that is going to move
     * @param steps        number of steps the player is moving
     */
    public void movePlayerBackward(Player playerToMove, int steps) {
        movePlayer(playerToMove, steps, false);
    }

    /**
     * Sets the current game phase type.
     *
     * @param currentGamePhaseType the current phase of the game to set
     */
    public void setCurrentGamePhaseType(GamePhaseType currentGamePhaseType) {
        this.currentGamePhaseType = currentGamePhaseType;
    }

    /**
     * Gets the number of players required to start the game.
     *
     * @return the required number of players
     */
    public int getRequiredPlayers() {
        return requiredPlayers;
    }

    /**
     * Sets the number of required players to start the game.
     * The value must be between 2 and 4 (inclusive).
     *
     * @param requiredPlayers the number of required players
     */
    public void setRequiredPlayers(int requiredPlayers) {
        if (requiredPlayers > 4 || requiredPlayers < 2) {
            return;
        }
        this.requiredPlayers = requiredPlayers;
        synchronized (players){
            if(players.size() >= requiredPlayers){
                //Awake main thread for starting game.
                players.notifyAll();
            }
        }
    }

    /**
     * Gets the unique identifier of the game.
     *
     * @return the game's UUID
     */
    public UUID getGameId() {
        return gameId;
    }

    /**
     * Gets the username or identifier of the game leader.<br>
     * <b>NOTE</b>: the game leader is not the leader in flight board! It's the one who created the game.
     * To get the leader in flight board use {@code getPlayers().getFirst()}
     *
     * @return the game leader's name
     */
    public String getGameLeader() {
        return gameLeader;
    }

    /**
     * Retrieves and removes a tile from the drawn tiles list based on its ID.
     *
     * @param id the ID of the tile to retrieve
     * @return the tile with the specified ID
     * @throws ThatTileIdDoesNotExistsException if no tile with the specified ID exists in the drawn tiles
     */
    public TileSkeleton getTileWithId(Integer id) throws ThatTileIdDoesNotExistsException {
        for (TileSkeleton t : getUncoveredTiles()) {
            if (t.getTileId() == id) {
                uncoveredTiles.remove(t);
                return t;
            }
        }
        throw new ThatTileIdDoesNotExistsException("that tile id doesn't exist.");
    }

    /**
     * Sets the drawnTiles Deck (used for testing)
     * @param uncoveredTiles the deck to set
     */
    public void setUncoveredTiles(List<TileSkeleton> uncoveredTiles) {
        this.uncoveredTiles.clear();
        if (uncoveredTiles != null) {
            this.uncoveredTiles.addAll(uncoveredTiles);
        }
    }

    /**
     * Process the end of the assembly phase for a specific player,
     * handling internally the "shipboard assembled" signal to the player.
     * It also manages to update the game assembly phase accordingly to how many players still need to finish assembly
     * and in which level is the game being played.
     * @param player the player which ended assembly
     * @param force if the player is forced to end assembly (e.g. no more time)
     * @throws IllegalArgumentException if {@code player} does not belong to this game data.
     * @throws TooManyItemsInHandException only if {@code player} is holding something in hand AND {@code force == false}.
     * @requires to be called during {@link GamePhaseType#ASSEMBLE}
     */
    public void endAssembly(Player player, boolean force) throws AlreadyEndedAssemblyException, NoShipboardException,
            TooManyItemsInHandException {
        if (!players.contains(player)) {
            throw new IllegalArgumentException("Player '" + player.getUsername() + "' is not in this game");
        }
        // handle player management
        if (force) {
            player.forceEndAssembly(startingPositions.removeFirst());
        } else {
            player.endAssembly(startingPositions.removeFirst());
        }
        // handle game management
        for (Player p : players) {
            if (p.getPosition() == null) {
                return;
            }
        }
        // if here: no player left to finish assembly (<===> all players have a position on the route-board)
        // thanks to precondition it's possible to cast the current game phase
        ((AssembleGamePhase) getCurrentGamePhase()).notifyAllPlayersEndedAssembly();
    }

    public boolean isAssemblyTimerRunning() {
        if (getCurrentGamePhaseType() != GamePhaseType.ASSEMBLE) {
            return false;
        }
        // else: in assemble can cast current game phase
        return ((AssembleGamePhase) getCurrentGamePhase()).isTimerRunning();
    }

    public Integer getAssemblyTimerSlotIndex() {
        if (getCurrentGamePhaseType() != GamePhaseType.ASSEMBLE) {
            return null;
        }
        // else: in assemble can cast current game phase
        return ((AssembleGamePhase) getCurrentGamePhase()).getAssemblyTimerSlotIndex();
    }

    public void endFlight(Player p){
        if (!players.contains(p)) return;

        p.setDead(true);

        players.remove(p);
        deadPlayers.add(p);
    }
}