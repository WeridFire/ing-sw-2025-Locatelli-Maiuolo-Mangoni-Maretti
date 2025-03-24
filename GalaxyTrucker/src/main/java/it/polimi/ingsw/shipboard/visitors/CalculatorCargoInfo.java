package src.main.java.it.polimi.ingsw.shipboard.visitors;

import src.main.java.it.polimi.ingsw.shipboard.LoadableType;
import src.main.java.it.polimi.ingsw.shipboard.tiles.*;
import src.main.java.it.polimi.ingsw.shipboard.tiles.exceptions.NotFixedTileException;
import src.main.java.it.polimi.ingsw.util.Coordinates;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for gathering and analyzing cargo-related information from container tiles in a shipboard.
 * It tracks the remaining capacity, the types of items loaded, and the locations of cargo containers.
 */
public class CalculatorCargoInfo {
    /** The total remaining cargo capacity available across visited container tiles. */
    private int capacityLeft;

    /** A list of all loadable items currently stored in the visited container tiles. */
    private final List<LoadableType> totalLoadedItems;

    /** A list of coordinates representing the locations of the visited container tiles. */
    private final List<Coordinates> containerLocations;

    /**
     * Constructs an empty {@code CalculatorCargoInfo} instance, initializing lists and setting capacity to zero.
     */
    CalculatorCargoInfo() {
        totalLoadedItems = new ArrayList<>();
        containerLocations = new ArrayList<>();
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
        containerLocations.add(coordinates);
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
     * Checks whether there are at least a given quantity of a specific loadable item in the visited container tiles.
     *
     * @param item The type of loadable item to check for.
     * @param quantity The minimum quantity required.
     * @return {@code true} if there are at least {@code quantity} items of the given type, {@code false} otherwise.
     */
    public boolean hasAtLeast(LoadableType item, int quantity) {
        return totalLoadedItems.stream().filter(e -> e == item).count() >= quantity;
    }

    /**
     * Returns a list of coordinates representing the locations of the visited container tiles.
     *
     * @return A (copy of the) list containing the coordinates of all visited containers.
     */
    public List<Coordinates> getCoordinatesMask() {
        return new ArrayList<>(containerLocations);
    }
}
