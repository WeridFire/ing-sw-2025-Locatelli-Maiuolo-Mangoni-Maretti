package src.main.java.it.polimi.ingsw.shipboard.tiles;

import src.main.java.it.polimi.ingsw.shipboard.LoadableType;
import src.main.java.it.polimi.ingsw.shipboard.SideType;
import src.main.java.it.polimi.ingsw.shipboard.tiles.exceptions.NotEnoughItemsException;
import src.main.java.it.polimi.ingsw.shipboard.tiles.exceptions.TooMuchLoadException;
import src.main.java.it.polimi.ingsw.shipboard.tiles.exceptions.UnsupportedLoadableItemException;
import src.main.java.it.polimi.ingsw.util.ContrabandCalculator;

import java.util.*;

/**
 * Represents a container tile that can store specific types of loadable items.
 * Each container has a defined capacity and a set of allowed items.
 */
public abstract class ContainerTile extends TileSkeleton<SideType> {

    private final Set<LoadableType> maxAllowedItems;

    private final int capacity;
    private Set<LoadableType> allowedItems;
    private final List<LoadableType> loadedItems;
    private int occupiedCapacity;

    /**
     * Constructs a ContainerTile with specified sides, allowed items, and capacity.
     *
     * @param sides An array of sides defining the structure of the tile.
     * @param maxAllowedItems The set of items that are allowed to be stored in this kind of container.
     *                        Even a {@link #setAllowedItems(Set)} will have to obey this constraint.
     * @param allowedItems The set of items that are allowed to be stored in this container instance.
     * @param capacity The maximum capacity of the container.
     */
    public ContainerTile(SideType[] sides, Set<LoadableType> maxAllowedItems,
                         Set<LoadableType> allowedItems, int capacity) {
        super(sides);
        this.maxAllowedItems = maxAllowedItems;
        try {
            setAllowedItems(allowedItems);
        } catch (UnsupportedLoadableItemException e) {
            // should never happen because constructors should be solidly programmed -> RuntimeException
            throw new RuntimeException(e);
        }
        this.capacity = capacity;
        this.loadedItems = new ArrayList<>(capacity);
    }

    /**
     * Sets new allowed items and removes any currently loaded items that are no longer allowed.
     *
     * @param allowedItems The new set of allowed items.
     * @return A list of items that were removed because they were no longer allowed.
     */
    public List<LoadableType> setAllowedItems(Set<LoadableType> allowedItems) throws UnsupportedLoadableItemException {
        if (!maxAllowedItems.containsAll(allowedItems)) {
            throw new UnsupportedLoadableItemException("Attempt to modify the allowed items"
                    + " with a non-subset of the allowed items for this kind of container: "
                    + allowedItems + " is not a subset of " + maxAllowedItems
                    + ". This would result in undesired and unsignaled exceptions.");
        }

        this.allowedItems = allowedItems;
        List<LoadableType> removedItems = new ArrayList<>(loadedItems.size());

        Iterator<LoadableType> iterator = loadedItems.iterator();
        while (iterator.hasNext()) {
            LoadableType item = iterator.next();
            if (!allowedItems.contains(item)) {
                removedItems.add(item);
                iterator.remove();
            }
        }

        return removedItems;
    }

    /**
     * Loads a specified quantity of an item into the container.
     *
     * @param item The item to load.
     * @param quantity The number of units to load.
     * @throws TooMuchLoadException If there is not enough capacity for the items.
     * @throws UnsupportedLoadableItemException If the item is not allowed in this container.
     */
    public void loadItems(LoadableType item, int quantity) throws TooMuchLoadException,
            UnsupportedLoadableItemException {
        if (!allowedItems.contains(item)) {
            throw new UnsupportedLoadableItemException("Attempt to add " + item +
                    " in a container which allows only the following items: " + allowedItems);
        }

        int requiredCapacity = quantity * item.getRequiredCapacity();
        if (requiredCapacity > getCapacityLeft()) {
            throw new TooMuchLoadException("Attempt to add too much items (" + item + " x" + quantity +
                    ") in container with space for only " + (getCapacityLeft() / item.getRequiredCapacity())
                    + " of them.");
        }

        occupiedCapacity -= requiredCapacity;
        loadedItems.addAll(Collections.nCopies(quantity, item));
    }

    /**
     * Removes a specified quantity of an item from the container.
     *
     * @param item The item to remove.
     * @param quantity The number of units to remove.
     *
     * @throws IllegalArgumentException If {@code quantity <= 0}.
     * @throws UnsupportedLoadableItemException If the item is not allowed in this container.
     * @throws NotEnoughItemsException If {@code quantity} is greater than the count of loaded items to remove.
     */
    public void removeItems(LoadableType item, int quantity) throws
            UnsupportedLoadableItemException, NotEnoughItemsException {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero, " + quantity + " provided");
        }

        if (!allowedItems.contains(item)) {
            throw new UnsupportedLoadableItemException("Attempt to remove " + item +
                    " in a container which allows only the following items: " + allowedItems);
        }

        long loadedCount = loadedItems.stream().filter(it -> it == item).count();
        if (loadedCount < quantity) {
            throw new NotEnoughItemsException("Attempt to remove " + quantity + " " + item
                    + " from a container with only " + loadedCount + " of them");
        }

        loadedItems.removeAll(Collections.nCopies(quantity, item));
    }

    /**
     * Checks whether a specific item is allowed to be stored in this container.
     *
     * @param item The item to check.
     * @return {@code true} if the item is allowed, otherwise {@code false}.
     */
    public boolean isAllowedItem(LoadableType item) {
        return allowedItems.contains(item);
    }

    /**
     * Retrieves a list of currently loaded items.
     *
     * @return A (copy of the) list of items currently in the container.
     */
    public List<LoadableType> getLoadedItems() {
        return new ArrayList<>(loadedItems);
    }

    /**
     * Returns the most valuable loaded items present on this tile,
     * for smugglers, up to a given limit, in descending order.
     * <p>
     * By default, this method returns an empty queue.
     * Subclasses that store contraband items should override it.
     * </p>
     *
     * @param limit the maximum number of items to return.
     * @param minimumContrabandValueExclusive the minimum contraband value (exclusive) to get items.
     *        Any item with contraband value below or equal to
     *        {@code minimumContrabandValueExclusive} is not considered.
     * @return a priority queue of the most valuable loaded items, ordered by value in descending order.
     * @throws IllegalArgumentException if {@code limit <= 0} or {@code minimumContrabandValueExclusive < 0}
     */
    public PriorityQueue<LoadableType> getContrabandMostValuableItems(int limit, int minimumContrabandValueExclusive)
            throws IllegalArgumentException {
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be greater than 0");
        }
        if (minimumContrabandValueExclusive < 0) {
            throw new IllegalArgumentException("Minimum contraband value (exclusive) must be greater or equal to 0");
        }

        PriorityQueue<LoadableType> queue =
                new PriorityQueue<>(limit, ContrabandCalculator.descendingContrabandComparator);
        int queueSize = 0;
        for (LoadableType item : getLoadedItems()) {
            if (ContrabandCalculator.getContrabandValue(item) > minimumContrabandValueExclusive) {
                queue.add(item);
                queueSize++;
                if (queueSize >= limit) {
                    break;
                }
            }
        }
        return queue;
    }

    /**
     * Returns the remaining capacity of the container.
     *
     * @return The amount of available capacity left.
     */
    public int getCapacityLeft() {
        return capacity - occupiedCapacity;
    }
}

