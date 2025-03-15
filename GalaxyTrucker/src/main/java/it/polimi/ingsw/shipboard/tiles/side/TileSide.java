package src.main.java.it.polimi.ingsw.shipboard.tiles.side;

import src.main.java.it.polimi.ingsw.enums.*;
import src.main.java.it.polimi.ingsw.shipboard.tiles.exceptions.IncoherentBatteryUsageException;

/**
 * Represents one side of a tile, defining its connector type and orientation.
 * This class provides default behaviors for compatibility checks and power calculations.
 * Subclasses may override methods to implement specific behaviors.
 */
public class TileSide {

    /**
     * Connector type for this side. Given with initialization.
     */
    private final ConnectorType connector;

    /**
     * Orientation for this side. It is given only when the tile is placed.
     */
    protected Direction orientation;

    /**
     * Constructs a TileSide with the specified connector type.
     *
     * @param connector the type of connector on this tile side
     */
    public TileSide(ConnectorType connector) {
        this.connector = connector;
    }

    /**
     * Gets the connector type of this tile side.
     *
     * @return the connector type
     */
    public ConnectorType getConnector() {
        return connector;
    }

    /**
     * Sets the orientation of this tile side.
     *
     * @param orientation the new orientation of the tile side
     */
    public void setOrientation(Direction orientation) {
        this.orientation = orientation;
    }

    /**
     * Determines the overall connection status between two tile sides.
     * <p>
     * The connection status is calculated by considering both directions of compatibility
     * between the two tile sides. The worst (most restrictive) status between the two is returned.
     * </p>
     *
     * @param s1 the first tile side.
     * @param s2 the adjacent tile side to check against.
     * @return the worst connection status between the two sides.
     */
    public static TilesConnectionStatus calculateConnectionStatus(TileSide s1, TileSide s2) {
        return TilesConnectionStatus.getWorst(s1.getConnectionStatusWith(s2), s2.getConnectionStatusWith(s1));
    }

    /**
     * Determines the connection status of this tile side with another tile side.
     * <p>
     * By default, the compatibility is determined by the connector type.
     * However, this method allows custom behaviors for subclasses of {@code TileSide}.
     * </p>
     * <p>
     * This relation is <b>non-symmetric</b>: if {@code this.getConnectionStatusWith(other)}
     * returns a status, it does not guarantee that {@code other.getConnectionStatusWith(this)}
     * will return the same status. This enables special behaviors in custom tile side implementations.
     * </p>
     *
     * @param other the other tile side to check compatibility with.
     * @return the connection status between the two tile sides.
     * @see #calculateConnectionStatus(TileSide, TileSide) for checking mutual compatibility.
     */
    public TilesConnectionStatus getConnectionStatusWith(TileSide other) {
        return connector.getConnectionStatus(other.connector);
    }


    /**
     * Determines whether a battery is required for this tile side power.
     * This method returns {@code false} by default and can be overridden by subclasses if needed.
     *
     * @return {@code true} if a battery is needed, {@code false} otherwise
     */
    public boolean isBatteryNeeded() {
        return false;
    }

    /**
     * Checks whether the orientation of this tile side is valid.
     * This method returns {@code true} by default and can be overridden by subclasses for specific behavior.
     *
     * @return {@code true} if the orientation is valid, {@code false} otherwise
     */
    public boolean isOrientationValid() {
        return true;
    }

    /**
     * Calculates the power provided by this tile side based on the specified power type and battery usage.
     * This method returns {@code 0f} by default and can be overridden by subclasses.
     *
     * @param powerType the type of power being calculated
     * @param batteryUsage {@code true} if battery usage should be considered, {@code false} otherwise
     * @return the amount of power provided by this tile side
     * @throws IncoherentBatteryUsageException if {@code batteryUsage} is different from
     * what is expected by the tile (i.e. {@link #isBatteryNeeded()})
     */
    public float calculatePower(PowerType powerType, boolean batteryUsage) throws IncoherentBatteryUsageException {
        return 0f;
    }
}

