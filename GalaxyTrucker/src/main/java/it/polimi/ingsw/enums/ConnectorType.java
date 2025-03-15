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
     * Determines if this connector can be welded with another connector,
     * meaning they can form a valid connection between two adjacent tiles.
     *
     * @param other the other connector to check against.
     * @return {@code true} if the connectors can be welded together, {@code false} otherwise.
     */
    private boolean isWeldableWith(ConnectorType other) {
        return (this.value & other.value) != 0;
    }

    /**
     * Determines the type of invalid connection when {@code this} connector is incompatible with {@code other}.
     *
     * @param other the other connector being compared: non-weldable with and non-equal to {@code this}.
     * @return a {@link TilesConnectionStatus} representing the specific error type.
     */
    private TilesConnectionStatus calculateInvalidConnectionStatus(ConnectorType other) {
        // Ensure consistent ordering: always compare the smaller ordinal first
        if (this.ordinal() > other.ordinal()) {
            return other.calculateInvalidConnectionStatus(this);
        }

        // Determine the specific error case
        if (this == SMOOTH) {
            // other > SMOOTH: other = SINGLE, DOUBLE or UNIVERSAL -> Connector with Smooth-Side error
            return TilesConnectionStatus.ERROR_CONNECTOR_SMOOTH;
        } else if (this == SINGLE) {
            // other > SINGLE && other != UNIVERSAL (or else they would have been weldable):
            // other = DOUBLE -> Single Connector with Double Connector error
            return TilesConnectionStatus.ERROR_CONNECTOR_SINGLE_DOUBLE;
        } else {
            return TilesConnectionStatus.UNIMPLEMENTED_ERROR; // Should never happen
        }
    }

    /**
     * Determines the connection status between this connector and another.
     *
     * @param other the other connector to check compatibility with.
     * @return a {@link TilesConnectionStatus} indicating whether the connectors can be welded,
     *         are compatible but not weldable, or result in a specific error.
     */
    public TilesConnectionStatus getConnectionStatus(ConnectorType other) {
        if (isWeldableWith(other)) {
            return TilesConnectionStatus.WELDABLE;
        } else if (this == other) {
            return TilesConnectionStatus.COMPATIBLE_NOT_WELDABLE;
        } else {
            return calculateInvalidConnectionStatus(other);
        }
    }
}
