package it.polimi.ingsw.shipboard.tiles;

import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.enums.Rotation;
import it.polimi.ingsw.shipboard.tiles.exceptions.FixedTileException;
import it.polimi.ingsw.shipboard.tiles.exceptions.NotFixedTileException;
import it.polimi.ingsw.util.Coordinates;

/**
 * Generic structure of a Tile.
 * @param <SideType> Tile's single side type
 */
public abstract class TileSkeleton<SideType> implements Tile {
    private final SideType[] sides;
    private Rotation appliedRotation;
    private Coordinates fixedAt;

    /**
     * Construct the tile with specified parameters.
     * @param sides An array of sides, where for each Direction {@code d},
     *              its value {@code d.v <- d.getValue()} is used as index to specify the related side:
     *              {@code sides[d.v]} is the tile's side in direction {@code d}.
     */
    public TileSkeleton(SideType[] sides) {
        this.sides = sides;
        this.appliedRotation = Rotation.NONE;
        fixedAt = null;
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
     * Retrieve {@code this} tile's coordinates.
     * @return {@code this} tile's coordinates.
     * @throws NotFixedTileException If {@code this} tile has not been placed yet.
     * @see #place(Coordinates)
     */
    public Coordinates getCoordinates() throws NotFixedTileException {
        if (fixedAt == null) {
            throw new NotFixedTileException("Attempt to retrieve unplaced tile's coordinates.");
        }

        return fixedAt;
    }

    /**
     * Set {@code this} tile fixed at specified coordinates
     * @param coordinates Where the tile is placed.
     * @throws FixedTileException If the tile is already fixed.
     * @throws NullPointerException If {@code coordinates} is {@code null}.
     */
    public void place(Coordinates coordinates) throws FixedTileException {
        if (coordinates == null) {
            throw new NullPointerException("coordinates cannot be null");
        }

        if (fixedAt != null) {
            throw new FixedTileException("Attempt to place an already fixed tile.");
        }
        fixedAt = coordinates;
    }

    /**
     * Apply the specified rotation to the tile.
     * <p>
     * Sides are affected by this rotation.
     * Subclasses that manages the rotation should override this function
     * keeping the {@code super.rotateTile(rotation)} and following the implementation note.
     * @param rotation The rotation to apply.
     * @throws FixedTileException If the tile has been already fixed.
     * @implNote uses {@link Rotation#applyTo(Object[])} to rotate sides.
     */
    public void rotateTile(Rotation rotation) throws FixedTileException {
        if (fixedAt != null) {
            throw new FixedTileException("Attempt to rotate tile already fixed at coordinates " + fixedAt);
        }

        appliedRotation = appliedRotation.composedRotation(rotation);
        rotation.applyTo(sides);
    }

    /**
     * Reset the tile rotation to its instantiation state.
     * Subclasses should not override this.
     * @throws FixedTileException If the tile has been already fixed.
     */
    public void resetRotation() throws FixedTileException {
        rotateTile(appliedRotation.reversed());
    }

    /**
     * Get info about tiles adjacency.
     * @param neighbor The tile to check for adjacency.
     * @return {@code null} if this tile is not placed adjacent to {@code other} tile,
     * otherwise the direction to go from this tile to the {@code other}.
     */
    public Direction getNeighborDirection(TileSkeleton<SideType> neighbor) {
        if (fixedAt == null) return null;
        return fixedAt.getNeighborDirection(neighbor.fixedAt);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Direction[] directions = Direction.values();
        sb.append("[").append(directions[0]).append(" -> ").append(sides[directions[0].getValue()]);
        for (int i = 1; i < directions.length; i++) {
            sb.append("; ").append(directions[i]).append(" -> ").append(sides[directions[i].getValue()]);
        }
        return sb.append("]").toString();
    }
}
