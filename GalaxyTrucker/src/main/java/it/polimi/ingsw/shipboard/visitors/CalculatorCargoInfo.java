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

    /** A map of coordinates and container tiles in these coordinates. Also empty containers are saved. */
    private final Map<Coordinates, ContainerTile> containerLocations;

    /**
     * Constructs an empty {@code CalculatorCargoInfo} instance, initializing lists and setting capacity to zero.
     */
    CalculatorCargoInfo() {
        containerLocations = new HashMap<>();
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
        containerLocations.put(coordinates, tile);
    }

    /**
     * Counts the occurrences of a specific loadable item among the visited container tiles.
     *
     * @param item The type of loadable item to count.
     * @return The number of occurrences of the specified item.
     */
    public int count(LoadableType item) {
        return containerLocations.values().stream()
                .mapToInt(c -> (int) c.getLoadedItems().stream()
                        .filter(i -> i.equals(item))
                        .count())
                .sum();
    }

    /**
     * Counts the total occurrences of any loadable item that belongs to the specified set of item types
     * among the visited container tiles.
     *
     * @param itemTypes A set of loadable item types to be counted.
     * @return The total number of occurrences of the specified item types.
     */
    public int countAll(Set<LoadableType> itemTypes) {
        return containerLocations.values().stream()
                .mapToInt(c -> (int) c.getLoadedItems().stream()
                        .filter(itemTypes::contains)
                        .count())
                .sum();
    }

    /**
     * @return A map of (coordinates -> visited container tile) entries.
     */
    public Map<Coordinates, ContainerTile> getLocations() {
        return new HashMap<>(containerLocations);
    }

    /**
     * Calculates a map with only taking in consideration the containers with at least {@code minAvailableSpace}
     * available space to store items.
     *
     * @param minAvailableSpace the target minimum available space
     * @return The calculated map of (coordinates -> visited container tile with enough available space) entries.
     */
    public Map<Coordinates, ContainerTile> getLocationsWithAvailableSpace(int minAvailableSpace) {
        Map<Coordinates, ContainerTile> result = new HashMap<>();
        for (Map.Entry<Coordinates, ContainerTile> entry : containerLocations.entrySet()) {
            if (entry.getValue().getCapacityLeft() >= minAvailableSpace) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
}
