package src.main.java.it.polimi.ingsw.enums;

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
     * a rotation that composed with <code>this</code> would give the <code>NONE</code> Rotation.
     * @return Reverse rotation.
     */
    public Rotation reversed() {
        return VALUES[(4 - value) % 4];
    }

    /**
     * Calculate the composition of two rotations: <code>this</code> and the specified one.
     * It is a commutative operation.
     * @param rotation Rotation to compose <code>this</code> with.
     * @return Calculated composition of rotations.
     */
    public Rotation composedRotation(Rotation rotation) {
        return VALUES[(value + rotation.value) % 4];
    }
}


/* above is special case of the following more generic rotation

public enum Rotation {
    NONE(0),
    COUNTERCLOCKWISE(1),
    OPPOSITE(Direction.TOTAL_DIRECTIONS / 2),
    CLOCKWISE(Direction.TOTAL_DIRECTIONS - 1);

    private static final Rotation[] VALUES = values();

    private final int value;

    Rotation(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public Rotation reversed() {
        return switch (this) {
            case COUNTERCLOCKWISE -> CLOCKWISE;
            case CLOCKWISE -> COUNTERCLOCKWISE;
            default -> this;
        };
    }

    public Rotation composedRotation(Rotation rotation) {
        int composedValue = (value + rotation.value) % Direction.TOTAL_DIRECTIONS;
        for (Rotation r : values()) {
            if (r.value == composedValue) {
                return r;
            }
        }
        throw new IllegalStateException("Invalid reversed rotation computation");
    }
}

*/