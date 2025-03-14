package src.main.java.it.polimi.ingsw.shipboard.tiles;

import src.main.java.it.polimi.ingsw.enums.*;

/**
 * Generic structure of a Tile.
 * @param <SideType> Tile's single side type
 * @param <ContentType> Tile's content type
 */
public abstract class TileSkeleton<SideType, ContentType> {
    protected SideType[] sides;
    protected ContentType content;
    protected Rotation appliedRotation;

    /**
     * Construct the tile with specified parameters.
     * @param sides An array of sides, where for each Direction <code>d</code>,
     *              its value <code>d.v <- d.getValue()</code> is used as index to specify the related side:
     *              <code>sides[d.v]</code> is the tile's side in direction <code>d</code>.
     * @param content Tile's content.
     */
    public TileSkeleton(SideType[] sides, ContentType content) {
        this.sides = sides;
        this.content = content;
        this.appliedRotation = Rotation.NONE;
    }

    /**
     * Apply the specified rotation to the tile.
     * <p>
     * Content is not affected by this rotation;
     * Sides are affected by this rotation.
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
        sb.append("Content: ").append(content).append("]");
        return sb.toString();
    }
}
