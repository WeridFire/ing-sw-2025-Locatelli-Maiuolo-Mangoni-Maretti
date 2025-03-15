package src.main.java.it.polimi.ingsw.shipboard.tiles.side;

import src.main.java.it.polimi.ingsw.enums.ConnectorType;
import src.main.java.it.polimi.ingsw.enums.PowerType;
import src.main.java.it.polimi.ingsw.shipboard.tiles.exceptions.IncoherentBatteryUsageException;

/**
 * Represents the side of a tile with a shield.
 * Shield sides can be adjacent to other tiles based on the connector type of this side.
 * Shields require battery power to function and provide a defense mechanism against external threats.
 */
public class TileSideShield extends TileSide {

    /**
     * Constructs a shield tile side with the specified connector.
     *
     * @param connectorType The type of connector on this tile side, which determines adjacency compatibility.
     */
    public TileSideShield(ConnectorType connectorType) {
        super(connectorType);
    }

    /**
     * Calculates the power output of the shield.
     * The shield only responds to {@link PowerType#SHIELD} requests.
     * It always provides a power of {@code 1.0} if activated correctly.
     *
     * @param powerType The type of power requested. Only {@link PowerType#SHIELD} is valid:
     *                  others are ignored and associated with {@code 0f} power.
     * @param batteryUsage Whether a battery is being used to activate the shield.
     * @return {@code 1.0} if the shield is correctly activated, otherwise {@code 0}.
     * @throws IncoherentBatteryUsageException if the battery usage does not match the expected requirement
     *                                        (shields always require a battery to function).
     */
    @Override
    public float calculatePower(PowerType powerType, boolean batteryUsage) throws IncoherentBatteryUsageException {
        if (powerType != PowerType.SHIELD) {
            return 0;
        }
        if (batteryUsage != isBatteryNeeded()) {
            throw new IncoherentBatteryUsageException(batteryUsage, isBatteryNeeded());
        }
        return 1f;
    }

    /**
     * Determines whether this shield side requires battery power to function.
     * Since shields always require energy to activate, this method always returns {@code true}.
     *
     * @return {@code true}, indicating that battery power is required.
     */
    @Override
    public boolean isBatteryNeeded() {
        return true;
    }
}

