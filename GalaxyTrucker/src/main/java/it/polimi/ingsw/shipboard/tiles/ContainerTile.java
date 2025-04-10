package it.polimi.ingsw.shipboard.tiles;

import it.polimi.ingsw.shipboard.LoadableType;
import it.polimi.ingsw.shipboard.SideType;
import it.polimi.ingsw.shipboard.tiles.exceptions.NotEnoughItemsException;
import it.polimi.ingsw.shipboard.tiles.exceptions.TooMuchLoadException;
import it.polimi.ingsw.shipboard.tiles.exceptions.UnsupportedLoadableItemException;
import it.polimi.ingsw.util.ContrabandCalculator;

import java.util.*;

/**
 * Represents a container tile that can store specific types of loadable items.
 * Each container has a defined capacity and a set of allowed items.
 */
public abstract class ContainerTile extends TileSkeleton {

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
            throw new UnsupportedLoadableItemException(allowedItems, maxAllowedItems);
        }

        this.allowedItems = allowedItems;

        // if no items loaded -> no items to remove
        if ((loadedItems == null) || (loadedItems.isEmpty())) {
            return Collections.emptyList();
        }
        // else: calculate all the items to remove and remove and return them
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
            throw new UnsupportedLoadableItemException(item, allowedItems);
        }

        int requiredCapacity = quantity * item.getRequiredCapacity();
        if (requiredCapacity > getCapacityLeft()) {
            throw new TooMuchLoadException("Attempt to add too much items (" + item + " x" + quantity +
                    ") in container with space for only " + (getCapacityLeft() / item.getRequiredCapacity())
                    + " of them.");
        }

        occupiedCapacity += requiredCapacity;
        loadedItems.addAll(Collections.nCopies(quantity, item));
    }

    /**
     * Calculate and set {@link #occupiedCapacity} from loaded items
     */
    private void recalculateOccupiedCapacity() {
        occupiedCapacity = 0;
        for (LoadableType item : loadedItems) {
            occupiedCapacity += item.getRequiredCapacity();
        }
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
            throw new UnsupportedLoadableItemException(item, allowedItems);
        }

        long loadedCount = loadedItems.stream().filter(it -> it == item).count();
        if (loadedCount < quantity) {
            throw new NotEnoughItemsException("Attempt to remove " + quantity + " " + item
                    + " from a container with only " + loadedCount + " of them");
        }

        for (int i = 0; i < quantity; i++) {
            loadedItems.remove(item);
        }

        recalculateOccupiedCapacity();
    }

    /**
     * Removes up to the specified quantity of items from the container.
     * <p>
     * This method searches for items in the container that match the given set of {@link LoadableType} and removes
     * them, up to the specified {@code quantity}. If fewer matching items are available, it removes all that it can.
     * </p>
     *
     * @param items the set of {@link LoadableType} items valid for removal
     * @param quantity the maximum number of items to remove
     * @return the actual number of items removed. If fewer than {@code quantity} items are available,
     *         it returns the total number removed, else it returns {@code quantity}.
     * @throws IllegalArgumentException if {@code quantity <= 0}.
     */
    public int removeAny(Set<LoadableType> items, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero, " + quantity + " provided");
        }

        // remove items
        List<LoadableType> toRemove = new ArrayList<>(quantity);
        int removed = 0;
        for (LoadableType loadedItem : loadedItems) {
            int howMany = (int)items.stream().filter(i -> i == loadedItem).count();
            if (howMany > 0) {
                removed += howMany;
                if (removed > quantity) {
                    howMany -= removed - quantity;
                    removed = quantity;
                }
                toRemove.addAll(Collections.nCopies(howMany, loadedItem));

                if (removed == quantity) {
                    break;
                }
            }
        }

        for (LoadableType loadedItem : toRemove) {
            loadedItems.remove(loadedItem);
        }

        recalculateOccupiedCapacity();
        return removed;
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

    /**
     * Returns the set of allowed items onto the tile.
     * @return the set of allowed items on the tile.
     * @implNote a copy is returned to allow modifications: in the implementation is an unmodifiable set.
     */
    public Set<LoadableType> getAllowedItems(){
        return new HashSet<>(allowedItems);
    }

    /**
     * Equivalent to {@code getLoadedItems().isEmpty()}
     * @return {@code true} if this container has no loaded items, {@code false} otherwise.
     */
    public boolean isEmpty() {
        return loadedItems.isEmpty();
    }
}

