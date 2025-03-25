package src.main.java.it.polimi.ingsw.shipboard.visitors;

import src.main.java.it.polimi.ingsw.shipboard.LoadableType;
import src.main.java.it.polimi.ingsw.shipboard.tiles.*;
import src.main.java.it.polimi.ingsw.shipboard.tiles.exceptions.NotFixedTileException;
import src.main.java.it.polimi.ingsw.util.Coordinates;

import java.util.*;

/**
 * This class is responsible for gathering and analyzing cargo-related information from container tiles in a shipboard.
 * It tracks the remaining capacity, the types of items loaded, and the locations of cargo containers.
 */
public class CalculatorCargoInfo {
    /** The total remaining cargo capacity available across visited container tiles. */
    private int capacityLeft;

    /** A list of all loadable items currently stored in the visited container tiles. */
    private final List<LoadableType> totalLoadedItems;

    /** A map of coordinates and items contained in these coordinates. */
    private final Map<Coordinates, List<LoadableType>> containerLocations;

    /**
     * Constructs an empty {@code CalculatorCargoInfo} instance, initializing lists and setting capacity to zero.
     */
    CalculatorCargoInfo() {
        totalLoadedItems = new ArrayList<>();
        containerLocations = new HashMap<>();
        capacityLeft = 0;
    }

    /**
     * Visits a {@code ContainerTile} to retrieve its cargo-related information.
     * The method updates the total capacity left, the list of loaded items, and the container's location.
     *
     * @param tile The {@code ContainerTile} being analyzed.
     * @throws NotFixedTileException If the tile is not fixed in a position.
     * @implSpec {@code protected} to access only from this package.
     */
    protected void visit(ContainerTile tile) throws NotFixedTileException {
        Coordinates coordinates = tile.getCoordinates();

        capacityLeft += tile.getCapacityLeft();
        totalLoadedItems.addAll(tile.getLoadedItems());
        containerLocations.put(coordinates, tile.getLoadedItems());
    }

    /**
     * Returns the total remaining cargo capacity across all visited container tiles.
     *
     * @return The available cargo capacity.
     */
    public int getCapacityLeft() {
        return capacityLeft;
    }

    /**
     * Counts the occurrences of a specific loadable item among the visited container tiles.
     *
     * @param item The type of loadable item to count.
     * @return The number of occurrences of the specified item.
     */
    public int count(LoadableType item) {
        return (int) totalLoadedItems.stream().filter(e -> e == item).count();
    }

    /**
     * Counts the total occurrences of any loadable item that belongs to the specified set of item types
     * among the visited container tiles.
     *
     * @param itemTypes A set of loadable item types to be counted.
     * @return The total number of occurrences of the specified item types.
     */
    public int countAll(Set<LoadableType> itemTypes) {
        return (int) totalLoadedItems.stream().filter(itemTypes::contains).count();
    }

    /**
     * Returns a map of coordinates and items contained at these coordinates.
     *
     * @return A (copy of the) map of coordinates and items contained at these coordinates.
     */
    public Map<Coordinates, List<LoadableType>> getCoordinatesMask() {
        return new HashMap<>(containerLocations);
    }
}
