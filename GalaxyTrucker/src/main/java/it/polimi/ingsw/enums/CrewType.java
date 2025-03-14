package src.main.java.it.polimi.ingsw.enums;

import src.main.java.it.polimi.ingsw.shipboard.tiles.content.ILoadableItem;

/**
 * Enum of crew tokens.
 */
public enum CrewType implements ILoadableItem {
    HUMAN,
    PURPLE_ALIEN,
    BROWN_ALIEN;

    @Override
    public int getOccupiedSpace() {
        return switch (this) {
            case PURPLE_ALIEN, BROWN_ALIEN -> 2;
            default -> 1;
        };
    }

    @Override
    public int getCreditsValue() {
        return 0;
    }
}
