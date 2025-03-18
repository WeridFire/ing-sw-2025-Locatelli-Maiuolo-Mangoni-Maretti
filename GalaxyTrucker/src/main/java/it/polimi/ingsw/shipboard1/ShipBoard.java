package src.main.java.it.polimi.ingsw.shipboard1;

import src.main.java.it.polimi.ingsw.enums.CargoType;
import src.main.java.it.polimi.ingsw.enums.Direction;
import src.main.java.it.polimi.ingsw.enums.GameLevel;
import src.main.java.it.polimi.ingsw.shipboard1.exceptions.NoTileFoundException;
import src.main.java.it.polimi.ingsw.shipboard1.exceptions.OutOfBuildingAreaException;
import src.main.java.it.polimi.ingsw.shipboard1.exceptions.TileAlreadyPresentException;
import src.main.java.it.polimi.ingsw.shipboard1.tiles.Tile;
import src.main.java.it.polimi.ingsw.shipboard1.tiles.content.ILoadableItem;
import src.main.java.it.polimi.ingsw.shipboard1.tiles.exceptions.NotEnoughItemsException;
import src.main.java.it.polimi.ingsw.shipboard1.tiles.exceptions.UnsupportedLoadableItemException;
import src.main.java.it.polimi.ingsw.util.BoardCoordinates;
import src.main.java.it.polimi.ingsw.util.ContrabandCalculator;
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
     * Retrieves a set of all tiles currently placed on the board, mapped to their coordinates.
     * The returned set is unmodifiable to prevent external modifications.
     *
     * @return An unmodifiable set of map entries, each containing a coordinate and the associated tile.
     */
    public Set<Map.Entry<Coordinates, Tile>> getTilesOnBoard() {
        return Set.copyOf(board.entrySet());
    }

    /**
     * Retrieves the set of coordinates that are currently occupied by tiles on the board.
     * The returned set is unmodifiable to prevent external modifications.
     *
     * @return An unmodifiable set of coordinates where tiles are placed.
     */
    public Set<Coordinates> getOccupiedCoordinates() {
        return Set.copyOf(board.keySet());
    }

    /**
     * Retrieves a set of all the tiles currently placed on the board.
     * The returned set is unmodifiable to prevent external modifications.
     *
     * @return An unmodifiable set of all and only the tiles placed onto the shipboard.
     */
    public Set<Tile> getTiles() {
        return Set.copyOf(board.values());
    }

    /**
     * Retrieves only the tiles placed in the provided coordinates.
     * If a coordinate value is not associated with a tile (no tile found) it is simply not considered.
     *
     * @param coordinates The coordinates to check to retrieve tiles.
     * @return All and only the tiles placed onto the shipboard which share the position with the provided coordinates.
     */
    public Set<Tile> getTiles(Set<Coordinates> coordinates) {
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
     * Retrieves a set of mapping of neighboring tiles relative to the given coordinates.
     * <p>
     * This method checks all adjacent coordinates and attempts to retrieve the tiles at those positions.
     * If a tile is not found or is outside the valid building area, that direction is associated to a null tile.
     * </p>
     *
     * @param coordinates The coordinates for which to retrieve neighboring tiles.
     * @return A {@code HashMap} where:
     * <ul>
     *     <li>keys are directions</li>
     *     <li>values are the corresponding tiles (or {@code null} if there is no tile in that place).</li>
     * </ul>
     */
    public Set<Map.Entry<Direction, Tile>> getNeighborTiles(Coordinates coordinates) {
        HashMap<Direction, Tile> neighborTiles = new HashMap<>();

        for (Map.Entry<Direction, Coordinates> entry : coordinates.neighbors()) {
            Direction direction = entry.getKey();
            Coordinates coords = entry.getValue();

            try {
                neighborTiles.put(direction, getTile(coords));
            } catch (NoTileFoundException | OutOfBuildingAreaException e) {
                neighborTiles.put(direction, null);
            }
        }

        return neighborTiles.entrySet();
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



    /**
     * Removes the most valuable contraband cargo from the ship.
     * The removal prioritizes the highest-value contraband items when the given quantity is reached.
     *
     * @param quantity The number of cargo items to remove.
     * @throws IllegalArgumentException if {@code quantity <= 0}
     */
    public void removeMostValuableCargo(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero: " + quantity + " provided.");
        }

        // priority queue to store cargo sorted by value in ascending order (head is the minimum)
        PriorityQueue<Map.Entry<Coordinates, CargoType>> priorityQueue = new PriorityQueue<>(quantity + 1,
                (e1, e2)
                        -> ContrabandCalculator.ascendingContrabandComparator.compare(e1.getValue(), e2.getValue()));
        int minimumContrabandValue = 0;

        // iterate over all the tiles
        for (Map.Entry<Coordinates, Tile> tilesOnBoard : getTilesOnBoard()) {
            Coordinates coordinates = tilesOnBoard.getKey();
            Tile tile = tilesOnBoard.getValue();

            // retrieve contraband cargo loaded onto this tile (in descending order of contraband value)
            PriorityQueue<ILoadableItem> cargo =
                    tile.getContent().getContrabandMostValuableItems(quantity, minimumContrabandValue);

            while (!cargo.isEmpty()) {
                CargoType mostValuableCargo = (CargoType) cargo.poll();
                priorityQueue.offer(Map.entry(coordinates, mostValuableCargo));

                // keep the size within quantity by removing the least valuable item (head)
                if (priorityQueue.size() > quantity) {
                    priorityQueue.poll();
                    // update minimum contraband value to optimize next iteration
                    minimumContrabandValue = ContrabandCalculator.getContrabandValue(priorityQueue.peek().getValue());
                    /* interrupt cargo parsing if this element is already "worse" (in cargo value)
                    of the minimum removed, since cargo is sorted in descending order and
                    no next value will ever be strictly better than the minimum (until next cargo in another tile)
                    */
                    if (ContrabandCalculator.getContrabandValue(mostValuableCargo) <= minimumContrabandValue) {
                        break;
                    }
                }
            }

            // stop if the minimum value is already the maximum possible
            if (minimumContrabandValue == ContrabandCalculator.maxCargoValue) {
                break;
            }
        }

        // remove cargo from ship
        while (!priorityQueue.isEmpty()) {
            Map.Entry<Coordinates, CargoType> cargoToRemove = priorityQueue.poll();
            try {
                getTile(cargoToRemove.getKey()).getContent().removeCargo(cargoToRemove.getValue());
            } catch (NotEnoughItemsException | UnsupportedLoadableItemException | OutOfBuildingAreaException |
                     NoTileFoundException e) {
                // should never happen, but just in case...
                System.err.println("Failed to remove cargo " + cargoToRemove.getValue() +
                        " at " + cargoToRemove.getKey() + ": " + e.getMessage());
            }
        }

        // need to notify the change in the content for the ship
        notifyListeners();  // maybe it's possible to notify only for subsets, in this case: only load distribution
    }

}

