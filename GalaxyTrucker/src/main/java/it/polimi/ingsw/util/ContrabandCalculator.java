package src.main.java.it.polimi.ingsw.util;

import src.main.java.it.polimi.ingsw.enums.*;
import src.main.java.it.polimi.ingsw.shipboard.tiles.content.ILoadableItem;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for sorting ILoadableItem based on contraband importance.
 */
public class ContrabandCalculator {

    /**
     * Map of ILoadableItem to their contraband importance as Integer.
      */
    private static final Map<ILoadableItem, Integer> orderedItems = calculateOrderedItems();
    private static Map<ILoadableItem, Integer> calculateOrderedItems() {
        Map<ILoadableItem, Integer> orderedItems = new HashMap<>();
        orderedItems.put(BatteryType.BATTERY, 1);
        orderedItems.put(CargoType.BLUE_GOODS, 2);
        orderedItems.put(CargoType.GREEN_GOODS, 3);
        orderedItems.put(CargoType.YELLOW_GOODS, 4);
        orderedItems.put(CargoType.RED_GOODS, 5);
        return orderedItems;
    }
    public static final int maxCargoValue = calculateMaxContrabandValue();
    private static int calculateMaxContrabandValue() {
        return orderedItems.values().stream().max(Comparator.naturalOrder()).orElse(0);
    }

    /**
     * Retrieve the importance of specified item for smugglers as an Integer representing its contraband value.<br>
     * If {@code A.getContrabandValue() > B.getContrabandValue()} => smugglers will prefer {@code A}.
     *
     * @param item the item to check
     * @return the virtual contraband value. Non-contraband items will have a value of zero (0).
     */
    public static int getContrabandValue(ILoadableItem item) {
        return orderedItems.getOrDefault(item, 0);
    }

    /**
     * Comparator that orders ILoadableItem by predefined contraband importance in ascending order.
     */
    public static final Comparator<ILoadableItem> ascendingContrabandComparator =
            Comparator.comparingInt(ContrabandCalculator::getContrabandValue);

    /**
     * Comparator that orders ILoadableItem by predefined contraband importance in descending order.
     */
    public static final Comparator<ILoadableItem> descendingContrabandComparator =
            ascendingContrabandComparator.reversed();

}
