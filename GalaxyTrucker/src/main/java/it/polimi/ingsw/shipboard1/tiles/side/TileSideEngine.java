package src.main.java.it.polimi.ingsw.shipboard1.tiles.side;

import src.main.java.it.polimi.ingsw.enums.ConnectorType;
import src.main.java.it.polimi.ingsw.enums.Direction;
import src.main.java.it.polimi.ingsw.enums.PowerType;
import src.main.java.it.polimi.ingsw.enums.TilesConnectionStatus;
import src.main.java.it.polimi.ingsw.shipboard1.tiles.exceptions.IncoherentBatteryUsageException;

/**
 * Represents the side of a tile containing an engine, which can be either single or double.
 * Engine sides cannot be adjacent to other tiles and always have a smooth side (non-connector).
 */
public class TileSideEngine extends TileSideDoubleVariant {

    /**
     * Constructs an engine tile side with the specified variant.
     *
     * @param isDouble {@code true} if the engine is double, {@code false} for a single engine.
     */
    public TileSideEngine(boolean isDouble) {
        super(isDouble, ConnectorType.SMOOTH);
    }

    /**
     * Calculates the power output of the engine whether it is double or not.
     * A single engine has a power of {@code 1.0}.
     * If the engine is double, the power is multiplied accordingly.
     *
     * @param powerType The type of power requested. Only {@link PowerType#THRUST} is valid for engines:
     *                  others are ignored and associated with {@code 0f} power.
     * @param batteryUsage Whether a battery is being used to activate the engine.
     * @return The calculated power output of the engine.
     * @throws IncoherentBatteryUsageException if the battery usage does not match the expected requirement
     *                                        (double engines require a battery, single ones do not).
     */
    @Override
    public float calculatePower(PowerType powerType, boolean batteryUsage) throws IncoherentBatteryUsageException {
        if (powerType != PowerType.THRUST) {
            return 0;
        }
        if (batteryUsage != isBatteryNeeded()) {
            throw new IncoherentBatteryUsageException(batteryUsage, isBatteryNeeded());
        }
        return getDoubleMultiplier();
    }

    /**
     * Determines whether this engine side is compatible with another tile side.
     * Since engine sides cannot be adjacent to any other tile, this method always returns {@code false}.
     *
     * @param other The other tile side to check compatibility with.
     * @return Always {@code false}, as engine sides must remain isolated.
     */
    @Override
    public TilesConnectionStatus getConnectionStatusWith(TileSide other) {
        return TilesConnectionStatus.ERROR_TILE_ENGINE;
    }

    /**
     * Determines whether this engine side has a valid orientation.
     * Only {@link Direction#SOUTH} is valid for engines:
     * Rules -> "it must point to the rear of the spaceship (toward the player)"
     *
     * @return {@code true} if the engine is pointing to the rear of the spaceship, {@code false} otherwise.
     */
    @Override
    public boolean isOrientationValid() {
        return orientation == Direction.SOUTH;
    }
}
