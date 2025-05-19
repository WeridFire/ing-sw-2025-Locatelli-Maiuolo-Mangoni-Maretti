package it.polimi.ingsw.enums;

/**
 * Four (4) sides rotation enumerator.
 * <p>
 * The proposed values are used in conjunction with the Direction enum to optimize some calculations.
 */
public enum Rotation {
    NONE(0),
    COUNTERCLOCKWISE(1),
    OPPOSITE(2),
    CLOCKWISE(3);

    private static final Rotation[] VALUES = values();

    public static Rotation fromString(String rotation) {
        return switch (rotation.toUpperCase()) {
            case "LEFT", "L", "COUNTERCLOCKWISE", "CC", "-90", "-PI/2" -> COUNTERCLOCKWISE;
            case "RIGHT", "R", "CLOCKWISE", "C", "90", "PI/2" -> CLOCKWISE;
            case "OPPOSITE", "O", "180", "PI" -> OPPOSITE;
            default -> NONE;
        };
    }

    public static Rotation random() {
        return VALUES[(int) (Math.random() * VALUES.length)];
    }

    private final int value;

    Rotation(int value) {
        this.value = value;
    }

    /**
     * Cast the direction to an int
     * @return Rotation value as int.
     */
    public int getValue() {
        return value;
    }

    /**
     * Calculate the reverse rotation:
     * a rotation that composed with {@code this} would give the {@code NONE} Rotation.
     * @return Reverse rotation.
     */
    public Rotation reversed() {
        return VALUES[(4 - value) % 4];
    }

    /**
     * Calculate the composition of two rotations: {@code this} and the specified one.
     * It is a commutative operation.
     * @param rotation Rotation to compose {@code this} with.
     * @return Calculated composition of rotations.
     */
    public Rotation composedRotation(Rotation rotation) {
        return VALUES[(value + rotation.value) % 4];
    }

    /**
     * Applies {@code this} rotation to an array of elements, rearranging them according to the rotated directions.
     * <p>
     * This method takes an array where each index corresponds to a {@link Direction} and reorders its elements
     * based on how the directions are rotated.
     * </p>
     *
     * @param <T> The type of elements in the array.
     * @param sides An array representing elements associated with each {@link Direction}.
     */
    public <T> void applyTo(T[] sides) {
        T[] oldSides = sides.clone();
        for (Direction direction : Direction.values()) {
            sides[direction.getRotated(this).getValue()] = oldSides[direction.getValue()];
        }
    }

    public double toDegrees() {
        return switch (this)  {
            case COUNTERCLOCKWISE -> -90;
            case CLOCKWISE -> 90;
            case OPPOSITE -> 180;
            default -> 0;
        };
    }
}