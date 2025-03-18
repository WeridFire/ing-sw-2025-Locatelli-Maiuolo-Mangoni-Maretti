package src.main.java.it.polimi.ingsw.shipboard1;

import src.main.java.it.polimi.ingsw.enums.GameLevel;
import src.main.java.it.polimi.ingsw.shipboard1.exceptions.NoTileFoundException;
import src.main.java.it.polimi.ingsw.shipboard1.exceptions.OutOfBuildingAreaException;
import src.main.java.it.polimi.ingsw.shipboard1.exceptions.TileAlreadyPresentException;
import src.main.java.it.polimi.ingsw.shipboard1.tiles.Tile;
import src.main.java.it.polimi.ingsw.util.BoardCoordinates;
import src.main.java.it.polimi.ingsw.util.Coordinates;

import java.util.*;


/**
 * Represents a ship board where tiles can be placed according to the game level's constraints.
 * It provides methods to retrieve and update tiles while ensuring the validity of placements.
 * <p>
 * The board also maintains an instance of {@link ShipStatistics} to automatically track relevant statistics,
 * like different power sources, crew members, and goods value.
 * Additionally, it supports an observer pattern to notify listeners when the board state changes.
 */
public class ShipBoard {

    /** The game level, which defines the board's constraints. */
    private final GameLevel level;

    /** A mapping between coordinates and the corresponding tiles placed on the board.
     * Ensures that values are never null.
     */
    private final HashMap<Coordinates, Tile> board;

    /** The statistics tracker for this ship board. */
    private final ShipStatistics statistics;

    /** A list of listeners observing changes to the board. */
    private final List<ShipBoardListener> listeners = new ArrayList<>();

    /**
     * Constructs a new ship board for the given game level.
     * Initializes an empty board and its corresponding statistics tracker.
     *
     * @param level The game level defining the board's constraints.
     */
    public ShipBoard(GameLevel level) {
        this.level = level;
        board = new HashMap<>();
        statistics = new ShipStatistics(this);
    }

    /**
     * Adds a listener that will be notified when the board state changes.
     *
     * @param listener The listener to add.
     */
    public void addListener(ShipBoardListener listener) {
        listeners.add(listener);
    }

    /**
     * Notifies all registered listeners that the board state has been updated.
     */
    private void notifyListeners() {
        for (ShipBoardListener listener : listeners) {
            listener.onShipBoardUpdated();
        }
    }

    /**
     * Retrieves the statistics tracker for this board.
     *
     * @return The {@link ShipStatistics} instance associated with this board.
     */
    public ShipStatistics getStatistics() {
        return statistics;
    }

    /**
     * Retrieves the set of coordinates that are currently occupied by tiles on the board.
     * The returned set is unmodifiable to prevent external modifications.
     *
     * @return An unmodifiable set of coordinates where tiles are placed.
     */
    public Set<Coordinates> getOccupiedCoordinates() {
        return Collections.unmodifiableSet(board.keySet());
    }

    /**
     * Retrieves a set of all tiles currently placed on the board, mapped to their coordinates.
     * The returned set is unmodifiable to prevent external modifications.
     *
     * @return An unmodifiable set of map entries, each containing a coordinate and the associated tile.
     */
    public Set<Map.Entry<Coordinates, Tile>> getTilesOnBoard() {
        return Collections.unmodifiableSet(board.entrySet());
    }


    /**
     * Retrieves only the tiles placed in the provided coordinates.
     * If a coordinate value is not associated with a tile (no tile found) it is simply not considered.
     *
     * @param coordinates The coordinates to check to retrieve tiles.
     * @return All and only the tiles placed onto the shipboard which share the position with the provided coordinates.
     */
    public Set<Tile> getPlacedTiles(Set<Coordinates> coordinates) {
        Set<Tile> placedTiles = new HashSet<>();
        for (Coordinates coordinate : coordinates) {
            try {
                placedTiles.add(getTile(coordinate));
            } catch (OutOfBuildingAreaException | NoTileFoundException e) {
                // do nothing: ok
            }
        }
        return placedTiles;
    }

    /**
     * Retrieves the tile located at the given coordinates.
     *
     * @param coordinates The coordinates of the tile to retrieve.
     * @return The tile at the specified coordinates.
     * @throws OutOfBuildingAreaException If the coordinates are outside the valid building area.
     * @throws NoTileFoundException If no tile is found at the given coordinates.
     */
    public Tile getTile(Coordinates coordinates) throws OutOfBuildingAreaException, NoTileFoundException {
        if (!BoardCoordinates.isOnBoard(level, coordinates)) {
            throw new OutOfBuildingAreaException(level, coordinates);
        }
        Tile result = board.get(coordinates);
        if (result == null) {
            throw new NoTileFoundException(coordinates);
        }
        return result;
    }

    /**
     * Places a tile at the specified coordinates on the board.
     *
     * @param tile The tile to place.
     * @param coordinates The coordinates where the tile should be placed.
     * @throws OutOfBuildingAreaException If the coordinates are outside the valid building area.
     * @throws TileAlreadyPresentException If there is already a tile at the specified coordinates.
     * @throws IllegalArgumentException If the provided tile is null.
     */
    public void setTile(Tile tile, Coordinates coordinates) throws OutOfBuildingAreaException,
            TileAlreadyPresentException, IllegalArgumentException {
        if (tile == null) {
            throw new IllegalArgumentException("Tile cannot be null");
        }
        if (!BoardCoordinates.isOnBoard(level, coordinates)) {
            throw new OutOfBuildingAreaException(level, coordinates);
        }
        if (board.containsKey(coordinates)) {
            throw new TileAlreadyPresentException(coordinates, board.get(coordinates));
        }
        board.put(coordinates, tile);
    }

}

