package it.polimi.ingsw.util;

import java.util.*;

/**
 * Utility class containing general-purpose static methods.
 */
public class Util {

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
        int total = list.size();
        int times;
        if (index < 0) {
            times = ((-index) / total) + 1;
        } else {
            times = -index / total;
        }
        index += times * total;
        return list.get(index);
    }
}
