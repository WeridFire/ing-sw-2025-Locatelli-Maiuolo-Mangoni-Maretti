package src.main.java.it.polimi.ingsw.enums;

import src.main.java.it.polimi.ingsw.shipboard.tiles.content.ILoadableItem;

/**
 * Enum of battery tokens.
 */
public enum BatteryType implements ILoadableItem {
    BATTERY;

    @Override
    public int getOccupiedSpace() {
        return 1;
    }

    @Override
    public int getCreditsValue() {
        return 0;
    }
}
