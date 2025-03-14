package src.main.java.it.polimi.ingsw.enums;

import src.main.java.it.polimi.ingsw.shipboard.tiles.ILoadableItem;

/**
 * Enum of cargo tokens.
 */
public enum CargoType implements ILoadableItem {
    BLUE_GOODS,
    GREEN_GOODS,
    YELLOW_GOODS,
    RED_GOODS;

    @Override
    public int getOccupiedSpace() {
        return 1;
    }

    @Override
    public int getCreditsValue() {
        return switch (this) {
            case BLUE_GOODS -> 1;
            case GREEN_GOODS -> 2;
            case YELLOW_GOODS -> 3;
            case RED_GOODS -> 4;
        };
    }
}
