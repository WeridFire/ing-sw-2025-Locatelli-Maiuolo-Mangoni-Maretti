package src.main.java.it.polimi.ingsw.enums;

/**
 * Represents the possible connection statuses between two tile sides in Galaxy Trucker.
 * <p>
 * A connection status determines whether two adjacent tiles can be properly connected
 * or if they result in an error due to incompatibility.
 * </p>
 */
public enum TilesConnectionStatus {
    /**
     * The two tile sides can be welded together, forming a valid connection.
     */
    WELDABLE,

    /**
     * The two tile sides are compatible but cannot be welded together.
     */
    COMPATIBLE_NOT_WELDABLE,

    /**
     * An undefined or unimplemented error occurred while checking tile compatibility.
     */
    UNIMPLEMENTED_ERROR,

    /**
     * Connection error due to one side having a {@link ConnectorType#SMOOTH} connector.
     */
    ERROR_CONNECTOR_SMOOTH,

    /**
     * Connection error due to an attempt to connect a {@link ConnectorType#SINGLE}
     * connector with a {@link ConnectorType#DOUBLE} connector.
     */
    ERROR_CONNECTOR_SINGLE_DOUBLE,

    /**
     * Connection error due to attempting to connect a cannon tile side
     * with another tile (cannon sides cannot be adjacent to other tiles).
     */
    ERROR_TILE_CANNON,

    /**
     * Connection error due to attempting to connect an engine tile side
     * with another tile (engine sides must be properly placed).
     */
    ERROR_TILE_ENGINE;

    /**
     * Determines the "worst" connection status between two given statuses.
     * <p>
     * The "worst" status is the one with the highest ordinal value in the enum.
     * </p>
     *
     * @param connectionStatus1 the first connection status.
     * @param connectionStatus2 the second connection status.
     * @return the worst connection status among the two.
     */
    public static TilesConnectionStatus getWorst(TilesConnectionStatus connectionStatus1,
                                                 TilesConnectionStatus connectionStatus2) {
        return (connectionStatus1.ordinal() > connectionStatus2.ordinal()) ? connectionStatus1 : connectionStatus2;
    }

    /**
     * Checks whether this connection status represents a valid tile connection.
     *
     * @return {@code true} if the connection is valid ({@link #WELDABLE} or {@link #COMPATIBLE_NOT_WELDABLE}),
     *         {@code false} if the connection is invalid.
     */
    public boolean isValid() {
        return this == WELDABLE || this == COMPATIBLE_NOT_WELDABLE;
    }
}

