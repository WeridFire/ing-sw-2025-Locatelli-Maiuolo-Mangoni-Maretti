package src.main.java.it.polimi.ingsw.shipboard.tiles;

import src.main.java.it.polimi.ingsw.enums.Direction;
import src.main.java.it.polimi.ingsw.enums.Rotation;

/**
 * Generic structure of a Tile.
 * @param <SideType> Tile's single side type
 */
public abstract class TileSkeleton<SideType> implements Tile {
    protected SideType[] sides;
    protected Rotation appliedRotation;

    /**
     * Construct the tile with specified parameters.
     * @param sides An array of sides, where for each Direction {@code d},
     *              its value {@code d.v <- d.getValue()} is used as index to specify the related side:
     *              {@code sides[d.v]} is the tile's side in direction {@code d}.
     */
    public TileSkeleton(SideType[] sides) {
        this.sides = sides;
        this.appliedRotation = Rotation.NONE;
    }

    /**
     * Retrieve the side facing the provided direction.
     *
     * @param direction The direction to search for side.
     * @return The side of this tile facing in direction {@code direction}.
     */
    public SideType getSide(Direction direction) {
        return sides[direction.getValue()];
    }

    /**
     * Apply the specified rotation to the tile.
     * <p>
     * Sides are affected by this rotation.
     * Subclasses that manages the rotation should override this function
     * keeping the {@code super.rotateTile(rotation)}
     * @param rotation The rotation to apply.
     */
    public void rotateTile(Rotation rotation) {
        SideType[] oldSides = sides.clone();
        for (Direction direction : Direction.values()) {
            sides[direction.getRotated(rotation).getValue()] = oldSides[direction.getValue()];
        }
        appliedRotation = appliedRotation.composedRotation(rotation);
    }

    /**
     * Reset the tile rotation to its instantiation state.
     * Subclasses should not override this.
     */
    public void resetRotation() {
        rotateTile(appliedRotation.reversed());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (Direction direction : Direction.values()) {
            sb.append(direction).append(" -> ").append(sides[direction.getValue()]).append("; ");
        }
        return sb.toString();
    }
}
