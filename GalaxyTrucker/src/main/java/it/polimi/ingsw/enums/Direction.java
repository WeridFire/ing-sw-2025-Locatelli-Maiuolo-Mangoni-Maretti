package src.main.java.it.polimi.ingsw.enums;

import java.util.ArrayList;
import java.util.List;

/**
 * Four (4) directions enumerator.
 * <p>
 * Values can be interpreted as array indices, starting from <code>East</code> as <code>0</code>
 * and going counterclockwise with <code>North</code>, <code>West</code> and <code>South</code>
 */
public enum Direction {
    EAST(0),
    NORTH(1),
    WEST(2),
    SOUTH(3);

    private static final Direction[] VALUES = values();
    public static final int TOTAL_DIRECTIONS = VALUES.length;

    /**
     * Sort the specified objects accordingly to internal order of Directions.
     * @param eastObject Object headed East.
     * @param northObject Object headed North.
     * @param westObject Object headed West.
     * @param southObject Object headed South.
     * @return Array A where <code>A[Direction.EAST.getValue()] == eastObject</code> and so on for every direction.
     * @param <T> Any object type.
     */
    public static <T> List<T> sortedArray(T eastObject, T northObject, T westObject, T southObject) {
        List<T> result = new ArrayList<>(TOTAL_DIRECTIONS);
        result.add(EAST.value, eastObject);
        result.add(NORTH.value, northObject);
        result.add(WEST.value, westObject);
        result.add(SOUTH.value, southObject);
        return result;
    }

    private final int value;

    Direction(int value) {
        this.value = value;
    }

    /**
     * Cast the direction to an int
     * @return Direction value as int.
     * Can be used as array index.
     */
    public int getValue() {
        return value;
    }

    /**
     * Calculate the direction obtained as a rotation of <code>this</code> direction.
     * @param rotation The rotation to apply.
     * @return The direction obtained as <code>this</code> direction rotated by <code>rotation</code>.
     */
    public Direction getRotated(Rotation rotation) {
        int newIndex = (value + rotation.getValue()) % TOTAL_DIRECTIONS;
        return VALUES[newIndex];
    }
}
