package it.polimi.ingsw.model.shipboard.visitors;

import it.polimi.ingsw.model.shipboard.LoadableType;
import it.polimi.ingsw.model.shipboard.tiles.ContainerTile;
import it.polimi.ingsw.model.shipboard.tiles.exceptions.NotFixedTileException;
import it.polimi.ingsw.util.Coordinates;

import java.io.Serializable;
import java.util.*;

/**
 * This class is responsible for gathering and analyzing cargo-related information from container tiles in a shipboard.
 * It tracks the remaining capacity, the types of items loaded, and the locations of cargo containers.
 */
public class CalculatorCargoInfo<ContainerType extends ContainerTile> implements Serializable {

    /** A map of coordinates and container tiles in these coordinates. Also empty containers are saved. */
    private final Map<Coordinates, ContainerType> containerLocations;

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
    protected void visit(ContainerType tile) throws NotFixedTileException {
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

    public List<LoadableType> getAllLoadedItems() {
        return containerLocations.values().stream()
                .flatMap(c -> c.getLoadedItems().stream())
                .toList();
    }


    /**
     * @return A map of (coordinates -> visited container tile) entries.
     */
    public Map<Coordinates, ContainerType> getLocations() {
        return new HashMap<>(containerLocations);
    }

    /**
     * Calculates a map taking in consideration just the containers with at least {@code minAvailableSpace}
     * available space to store items.
     *
     * @param minAvailableSpace the target minimum available space
     * @return The calculated map of (coordinates -> visited container tile with enough available space) entries.
     * @throws IllegalArgumentException If {@code minAvailableSpace <= 0}.
     */
    public Map<Coordinates, ContainerType> getLocationsWithAvailableSpace(int minAvailableSpace) {
        if (minAvailableSpace <= 0) {
            throw new IllegalArgumentException("Minimum available space must be greater than zero (provided: "
                    + ((minAvailableSpace == 0)
                    ? "zero. In this case simply call getLocations() instead)"
                    : minAvailableSpace + ")") );
        }

        Map<Coordinates, ContainerType> result = new HashMap<>();
        for (Map.Entry<Coordinates, ContainerType> entry : containerLocations.entrySet()) {
            if (entry.getValue().getCapacityLeft() >= minAvailableSpace) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    /**
     * Calculates a map taking in consideration just the containers with at least {@code minLoadedItems}
     * loaded items (of any type) onto them.
     *
     * @param minLoadedItems the target minimum loaded items (of any type)
     * @return The calculated map of (coordinates -> visited container tile with enough loaded items) entries.
     * @throws IllegalArgumentException If {@code minLoadedItems <= 0}.
     */
    public Map<Coordinates, ContainerType> getLocationsWithLoadedItems(int minLoadedItems) {
        if (minLoadedItems <= 0) {
            throw new IllegalArgumentException("Minimum loaded items must be greater than zero (provided: "
                    + ((minLoadedItems == 0)
                    ? "zero. In this case simply call getLocations() instead)"
                    : minLoadedItems + ")") );
        }

        Map<Coordinates, ContainerType> result = new HashMap<>();
        for (Map.Entry<Coordinates, ContainerType> entry : containerLocations.entrySet()) {
            if (entry.getValue().getLoadedItems().size() >= minLoadedItems) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    /**
     * Calculates a map taking in consideration just the containers with at least {@code minLoadedItems}
     * loaded items (of the specified type) onto them.
     *
     * @param loadedItems the type of the loaded items to check
     * @param minLoadedItems the target minimum loaded items (of the type {@code loadedItems})
     * @return The calculated map of (coordinates -> visited container tile with enough loaded items) entries.
     * @throws IllegalArgumentException If {@code minLoadedItems <= 0}.
     */
    public Map<Coordinates, ContainerType> getLocationsWithLoadedItems(LoadableType loadedItems, int minLoadedItems) {
        if (minLoadedItems <= 0) {
            throw new IllegalArgumentException("Minimum loaded items must be greater than zero (provided: "
                    + ((minLoadedItems == 0)
                    ? "zero. In this case simply call getLocations() instead)"
                    : minLoadedItems + ")") );
        }

        Map<Coordinates, ContainerType> result = new HashMap<>();
        for (Map.Entry<Coordinates, ContainerType> entry : containerLocations.entrySet()) {
            if (entry.getValue().getLoadedItems().stream()
                    .filter(i -> i.equals(loadedItems))
                    .count() >= minLoadedItems) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    /**
     * Calculates a map taking in consideration just the containers with at least {@code minLoadedItems}
     * loaded items (of any specified type) onto them.
     *
     * @param loadedItems the types of the loaded items to check
     * @param minLoadedItems the target minimum loaded items (any type in {@code loadedItems})
     * @return The calculated map of (coordinates -> visited container tile with enough loaded items) entries.
     * @throws IllegalArgumentException If {@code minLoadedItems <= 0}.
     */
    public Map<Coordinates, ContainerType> getLocationsWithLoadedItems(Set<LoadableType> loadedItems, int minLoadedItems) {
        if (minLoadedItems <= 0) {
            throw new IllegalArgumentException("Minimum loaded items must be greater than zero (provided: "
                    + ((minLoadedItems == 0)
                    ? "zero. In this case simply call getLocations() instead)"
                    : minLoadedItems + ")") );
        }

        Map<Coordinates, ContainerType> result = new HashMap<>();
        for (Map.Entry<Coordinates, ContainerType> entry : containerLocations.entrySet()) {
            if (entry.getValue().getLoadedItems().stream()
                    .filter(loadedItems::contains)
                    .count() >= minLoadedItems) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    /**
     * Calculates a map taking in consideration just the containers with at least ALL the {@code allowedItemTypes}
     * as allowed items.
     *
     * @param allowedItemTypes the target allowed content
     * @return The calculated map of (coordinates -> visited container tile with allowed items) entries.
     */
    public Map<Coordinates, ContainerType> getLocationsWithAllowedContent(Set<LoadableType> allowedItemTypes) {
        if (allowedItemTypes == null || allowedItemTypes.isEmpty()) {
            return getLocations();
        }

        Map<Coordinates, ContainerType> result = new HashMap<>();
        for (Map.Entry<Coordinates, ContainerType> entry : containerLocations.entrySet()) {
            if (entry.getValue().getAllowedItems().containsAll(allowedItemTypes)) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    /**
     * Removes up to the specified quantity of items from available containers.
     * <p>
     * This method searches for containers that contain at least one of the specified items and removes items
     * up to the given quantity.
     * The removal process stops once the required quantity has been removed or if no more
     * items are available.
     * </p>
     *
     * @param items the set of items to be removed
     * @param quantity the maximum number of items to remove
     */
    public void removeUpTo(Set<LoadableType> items, int quantity) {
        // pre-filter to call removeAny only for tiles that have something to remove
        for (ContainerTile container : getLocationsWithLoadedItems(items, 1).values()) {
            quantity -= container.removeAny(items, quantity);
            if (quantity <= 0) {
                break;
            }
        }
    }

}
