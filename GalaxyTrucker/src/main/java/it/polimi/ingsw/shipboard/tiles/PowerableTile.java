package it.polimi.ingsw.shipboard.tiles;

import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.enums.Rotation;
import it.polimi.ingsw.shipboard.SideType;
import it.polimi.ingsw.shipboard.tiles.exceptions.FixedTileException;

public abstract class PowerableTile extends TileSkeleton {
    private final Boolean[] sidesWithPower;
    private final boolean batteryNeeded;

    /**
     * Constructs a Tile with the specified sides, and if it needs battery to be used.
     *
     * @param sides An array defining the sides of the tile.
     *              Each index corresponds to a direction {@code d},
     *              where {@code sides[d.v]} represents the tile's side in that direction.
     * @param sidesWithPower A mask for {@code sides} where each side can implement a power.
     *                     Same indexing notation with directions' values.
     * @param batteryNeeded Indicates if the tile needs battery to be used ({@code true}) or not ({@code false}).
     */
    protected PowerableTile(SideType[] sides, Boolean[] sidesWithPower, boolean batteryNeeded) {
        super(sides);
        this.sidesWithPower = sidesWithPower;
        this.batteryNeeded = batteryNeeded;
    }

    /**
     * Getter for info about battery usage.
     *
     * @return {@code true} if a battery is needed to use this tile's powers, {@code false} otherwise.
     */
    public boolean isBatteryNeeded() {
        return batteryNeeded;
    }

    /**
     * Getter for info about sides with power presence.
     *
     * @param direction The direction to check for power presence.
     * @return {@code true} if the tile has its power pointing in the specified direction, {@code false} otherwise.
     */
    public boolean hasPower(Direction direction) {
        return sidesWithPower[direction.getValue()];
    }

    @Override
    public void rotateTile(Rotation rotation) throws FixedTileException {
        super.rotateTile(rotation);
        rotation.applyTo(sidesWithPower);
    }

}
