package it.polimi.ingsw.util;

import java.util.*;

/**
 * Utility class containing general-purpose static methods.
 */
public class Util {

    /**
     * Calculate an index using modular indexing.
     * <p>
     * The index wraps around {@code module}, allowing access with negative or out-of-bounds indices.
     *
     * @param index The modular index (can be negative or {@code >= module}).
     * @param module The maximum number to have (exclusive).
     * @return The {@code index} module {@code module}.
     */
    public static int getModular(int index, int module) {
        int times;
        if (index < 0) {
            times = ((-index) / module) + 1;
        } else {
            times = -index / module;
        }
        index += times * module;
        return index;
    }

    /**
     * Retrieves an element from the given list using modular indexing.
     * <p>
     * The index wraps around the list size, allowing access with negative or out-of-bounds indices.
     *
     * @param list The list from which to retrieve an element.
     * @param index The modular index (can be negative or greater than the list size).
     * @param <T> The type of elements in the list.
     * @return The element at the given modular index.
     * @throws IllegalArgumentException if the list is null or empty.
     */
    public static <T> T getModularAt(List<T> list, int index) {
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("The list is null or empty");
        }
        return list.get(getModular(index, list.size()));
    }
}
