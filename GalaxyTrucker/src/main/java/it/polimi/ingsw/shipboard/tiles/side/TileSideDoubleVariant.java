package src.main.java.it.polimi.ingsw.shipboard.tiles.side;

import src.main.java.it.polimi.ingsw.enums.ConnectorType;

/**
 * Represents a tile side that has a variant which can be either single or double.
 * This is useful for components such as engines or cannons that may have increased effects when doubled.
 */
public class TileSideDoubleVariant extends TileSide {
    /**
     * Indicates whether this tile side is in its double variant.
     */
    private final boolean isDouble;

    /**
     * Constructs a tile side with a possible double variant.
     *
     * @param isDouble {@code true} if this tile side is in its double variant, {@code false} otherwise.
     * @param connectorType the type of connector associated with this tile side.
     */
    protected TileSideDoubleVariant(boolean isDouble, ConnectorType connectorType) {
        super(connectorType);
        this.isDouble = isDouble;
    }

    /**
     * Returns the multiplier associated with the double variant.
     * A double variant has a multiplier of {@code 2.0}, while a single variant has {@code 1.0}.
     *
     * @return The multiplier value depending on whether the tile side is double or not.
     */
    protected float getDoubleMultiplier() {
        return isDouble ? 2.0f : 1.0f;
    }

    @Override
    public boolean isBatteryNeeded() {
        return isDouble;
    }
}
