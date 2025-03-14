package src.main.java.it.polimi.ingsw.enums;

/**
 * Enum for all the possible connectors in Galaxy Trucker
 * (Single, Double and Universal) + non-connector side type: Smooth side
 */
public enum ConnectorType {
    SMOOTH(0),
    SINGLE(1),
    DOUBLE(2),
    UNIVERSAL(3);

    private final int value;
    ConnectorType(int value) {
        this.value = value;
    }

    /**
     * Check if {@code this} and {@code other} connectors are compatible with each other.
     * They can be:
     * <ul>
     *     <li>Smooth & Smooth</li>
     *     <li>Connector & Connector of the same type</li>
     *     <li>Connector & Universal Connector</li>
     * </ul>
     *
     * @param other the other connector to check {@code this} with
     * @return {@code true} if the connectors are compatible with each other, {@code false} otherwise
     */
    public boolean isCompatibleWith(ConnectorType other) {
        if (value == other.value) {
            return true;
        }
        else {
            return canBeWeldedWith(other);
        }
    }

    /**
     * Check if {@code this} and {@code other} connectors can be welded together,
     * i.e. can provide a connection between two adjacent tiles.
     *
     * @param other the other connector to check {@code this} with
     * @return {@code true} if the connectors are weldable with each other, {@code false} otherwise
     */
    public boolean canBeWeldedWith(ConnectorType other) {
        return (value & other.value) != 0;
    }
}
