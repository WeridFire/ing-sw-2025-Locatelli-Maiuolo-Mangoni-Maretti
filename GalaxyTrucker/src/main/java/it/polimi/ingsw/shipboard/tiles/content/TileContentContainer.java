package src.main.java.it.polimi.ingsw.shipboard.tiles.content;

import java.util.*;

/**
 * A container tile that holds loadable items on itself, enforcing capacity constraints and allowed item types.
 * Subclasses can extend this class to define specific behavior for managing tile content.
 */
public abstract class TileContentContainer {
    private Set<ILoadableItem> allowedItems;
    private final List<ILoadableItem> loadedItems;
    private int occupiedCapacity;
    private final int maxCapacity;

    /**
     * Constructs a TileContentContainer with the specified allowed items and maximum capacity.
     *
     * @param allowedItems the set of item types that can be loaded into this container
     * @param maxCapacity  the maximum capacity in terms of occupied space
     */
    protected TileContentContainer(Set<ILoadableItem> allowedItems, int maxCapacity) {
        this.allowedItems = allowedItems;
        this.maxCapacity = maxCapacity;
        this.loadedItems = new ArrayList<>(maxCapacity);
        this.occupiedCapacity = 0;
    }

    /**
     * Checks if a given item is allowed in this container.
     *
     * @param item the item to check
     * @return {@code true} if the item is allowed, {@code false} otherwise
     */
    protected boolean isAllowed(ILoadableItem item) {
        return allowedItems.contains(item);
    }

    /**
     * Gets the remaining capacity in the container.
     *
     * @return the available capacity
     */
    protected int getCapacityLeft() {
        return maxCapacity - occupiedCapacity;
    }

    /**
     * Attempts to add an item to the container if space and allowance permit.
     *
     * @param item the item to add
     * @return {@code true} if the item was successfully added, {@code false} otherwise
     */
    protected boolean add(ILoadableItem item) {
        int occupiedSpace = item.getOccupiedSpace();
        boolean canAdd = isAllowed(item) && getCapacityLeft() >= occupiedSpace;
        if (!canAdd) {
            return false;
        }
        occupiedCapacity += occupiedSpace;
        loadedItems.add(item);
        return true;
    }

    /**
     * Removes a specific item from the container.
     *
     * @param content the item to remove
     * @return {@code true} if the item was removed, {@code false} if it was not found
     */
    protected boolean remove(ILoadableItem content) {
        boolean removed = loadedItems.remove(content);
        if (removed) {
            occupiedCapacity -= content.getOccupiedSpace();
        }
        return removed;
    }

    /**
     * Removes a specified number of any items from the container.
     *
     * @param n the number of items to remove
     * @return {@code true} if the removal was successful, {@code false} otherwise
     */
    protected boolean removeAny(int n) {
        if (n <= 0 || n > loadedItems.size()) {
            return false;
        }
        loadedItems.subList(0, n).clear();
        occupiedCapacity = loadedItems.stream().mapToInt(ILoadableItem::getOccupiedSpace).sum();
        return true;
    }

    /**
     * Gets a list of all currently loaded items.
     *
     * @return a new list containing all loaded items
     */
    protected ArrayList<ILoadableItem> getLoadedItems() {
        return new ArrayList<>(loadedItems);
    }

    /**
     * Gets the distinct types of loaded items.
     *
     * @return a set of unique item types currently present in the container
     */
    protected Set<ILoadableItem> getLoadedItemsTypes() {
        return new HashSet<>(loadedItems);
    }

    /**
     * Counts the occurrences of a specific item type in the container.
     *
     * @param item the item type to count
     * @return the number of occurrences of the specified item
     */
    protected int countLoaded(ILoadableItem item) {
        int count = 0;
        for (ILoadableItem loadedItem : loadedItems) {
            if (loadedItem.equals(item)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Updates the allowed items set and removes any currently loaded items that are no longer allowed.
     *
     * @param newAllowedItems the new set of allowed items
     * @return a list of items that were removed due to being disallowed
     */
    protected ArrayList<ILoadableItem> updateAllowedItems(Set<ILoadableItem> newAllowedItems) {
        this.allowedItems = newAllowedItems;
        ArrayList<ILoadableItem> removedItems = new ArrayList<>();

        Iterator<ILoadableItem> iterator = loadedItems.iterator();
        while (iterator.hasNext()) {
            ILoadableItem item = iterator.next();
            if (!newAllowedItems.contains(item)) {
                removedItems.add(item);
                iterator.remove();
            }
        }

        // Recalculate the occupied capacity after removal
        occupiedCapacity = loadedItems.stream().mapToInt(ILoadableItem::getOccupiedSpace).sum();

        return removedItems;
    }
}
