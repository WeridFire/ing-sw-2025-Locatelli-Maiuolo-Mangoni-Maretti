package src.main.java.it.polimi.ingsw.util;

import src.main.java.it.polimi.ingsw.enums.*;
import src.main.java.it.polimi.ingsw.shipboard.tiles.content.ILoadableItem;

import java.util.Comparator;

/**
 * Utility class for sorting ILoadableItem based on contraband importance.
 */
public class ContrabandCalculator {
    // Ordered array of ILoadableItem from least to most contraband importance
    private static final ILoadableItem[] ORDERED_ITEMS = {
            BatteryType.BATTERY,
            CargoType.BLUE_GOODS,
            CargoType.GREEN_GOODS,
            CargoType.YELLOW_GOODS,
            CargoType.RED_GOODS
    };

    private static int getContrabandIndex(ILoadableItem item) {
        for (int i = 0; i < ORDERED_ITEMS.length; i++) {
            if (ORDERED_ITEMS[i] == item) {
                return i;
            }
        }
        return -1;  // when item is not a contraband item
    }

    /**
     * Comparator that orders ILoadableItem by predefined contraband importance in descending order.
     */
    public static final Comparator<ILoadableItem> CONTRABAND_COMPARATOR =
            Comparator.comparingInt(ContrabandCalculator::getContrabandIndex).reversed();

}
