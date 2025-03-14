package src.main.java.it.polimi.ingsw.enums;

import src.main.java.it.polimi.ingsw.shipboard.tiles.ILoadableItem;

import java.util.HashMap;
import java.util.Map;

/**
 * Enum of all possible tokens used in the game that can be loaded onto tiles,
 * and some useful combinations of them.
 */
public enum LoadableItem implements ILoadableItem, IFlagEnum<LoadableItem> {
    NONE(0),

    // battery
    BATTERY(1),

    // goods
    BLUE_GOODS(1 << 1),
    GREEN_GOODS(1 << 2),
    YELLOW_GOODS(1 << 3),
    RED_GOODS(1 << 4),

    // crew
    HUMAN(1 << 5),
    PURPLE_ALIEN(1 << 6),
    BROWN_ALIEN(1 << 7),

    // useful mix
    BATTERY_COMPONENT(BATTERY.value),

    CARGO_HOLD(BLUE_GOODS.value + GREEN_GOODS.value + YELLOW_GOODS.value),
    SPECIAL_CARGO_HOLD(CARGO_HOLD.value + RED_GOODS.value),

    CONTRABAND_CARGO(SPECIAL_CARGO_HOLD.value + BATTERY_COMPONENT.value),

    CREW(HUMAN.value + PURPLE_ALIEN.value + BROWN_ALIEN.value),
    ALIENS(PURPLE_ALIEN.value + BROWN_ALIEN.value);


    private final int value;
    LoadableItem(int value) {
        this.value = value;
    }

    private static final Map<Integer, LoadableItem> lookup = new HashMap<>();
    static {
        for (LoadableItem item : LoadableItem.values()) {
            lookup.put(item.value, item);
        }
    }

    @Override
    public int getFlagsValue() {
        return value;
    }

    @Override
    public LoadableItem fromValue(int value) {
        return lookup.get(value);  // O(1) instead of O(n)
    }

    @Override
    public int getOccupiedSpace() {
        if (isContainedIn(ALIENS)) {
            return 2;
        } else {
            return 1;
        }
    }

    @Override
    public int getCreditsValue() {
        return switch (this) {
            case BLUE_GOODS -> 1;
            case GREEN_GOODS -> 2;
            case YELLOW_GOODS -> 3;
            case RED_GOODS -> 4;
            default -> 0;
        };
    }

    @Override
    public int getContrabandValue() {
        return switch (this) {
            case BATTERY -> 1;
            case BLUE_GOODS -> 2;
            case GREEN_GOODS -> 3;
            case YELLOW_GOODS -> 4;
            case RED_GOODS -> 5;
            default -> 0;
        };
    }
}
