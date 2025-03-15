package src.main.java.it.polimi.ingsw.shipboard.tiles.side;

import src.main.java.it.polimi.ingsw.enums.ConnectorType;
import src.main.java.it.polimi.ingsw.enums.Direction;
import src.main.java.it.polimi.ingsw.enums.PowerType;
import src.main.java.it.polimi.ingsw.shipboard.tiles.exceptions.IncoherentBatteryUsageException;

/**
 * Represents the side of a tile containing a cannon, which can be either single or double.
 * Cannon sides cannot be adjacent to other tiles and always have a smooth connector.
 */
public class TileSideCannon extends TileSideDoubleVariant {

    /**
     * Constructs a cannon tile side with the specified variant.
     *
     * @param isDouble {@code true} if the cannon is double, {@code false} for a single cannon.
     */
    public TileSideCannon(boolean isDouble) {
        super(isDouble, ConnectorType.SMOOTH);
    }

    /**
     * Calculates the power output of the cannon based on its orientation and whether it is double.
     * A cannon facing North has a base power of {@code 1.0}, while other directions have a base power of {@code 0.5}.
     * If the cannon is double, the power is multiplied accordingly.
     *
     * @param powerType The type of power requested. Only {@link PowerType#FIRE} is valid for cannons:
     *                  others are ignored and associated with {@code 0f} power.
     * @param batteryUsage Whether a battery is being used to activate the cannon.
     * @return The calculated power output of the cannon.
     * @throws IncoherentBatteryUsageException if the battery usage does not match the expected requirement
     *                                        (double cannons require a battery, single ones do not).
     */
    @Override
    public float calculatePower(PowerType powerType, boolean batteryUsage) throws IncoherentBatteryUsageException {
        if (powerType != PowerType.FIRE) {
            return 0;
        }
        if (batteryUsage != isBatteryNeeded()) {
            throw new IncoherentBatteryUsageException(batteryUsage, isBatteryNeeded());
        }
        float singlePower = (orientation == Direction.NORTH) ? 1f : 0.5f;
        return singlePower * getDoubleMultiplier();
    }

    /**
     * Determines whether this cannon side is compatible with another tile side.
     * Since cannon sides cannot be adjacent to any other tile, this method always returns {@code false}.
     *
     * @param other The other tile side to check compatibility with.
     * @return Always {@code false}, as cannon sides must remain isolated.
     */
    @Override
    public boolean isCompatibleWith(TileSide other) {
        return false;
    }
}

