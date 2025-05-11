package it.polimi.ingsw.shipboard.tiles;

import com.fasterxml.jackson.annotation.JsonIgnore;
import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.enums.Rotation;
import it.polimi.ingsw.shipboard.SideType;
import it.polimi.ingsw.shipboard.tiles.exceptions.FixedTileException;
import it.polimi.ingsw.shipboard.tiles.exceptions.NotFixedTileException;
import it.polimi.ingsw.util.Coordinates;
import it.polimi.ingsw.view.cli.CLIFrame;
import it.polimi.ingsw.view.cli.ICLIPrintable;

import java.util.Arrays;

/**
 * Generic structure of a Tile.
 */
public abstract class TileSkeleton implements Tile, ICLIPrintable {
    private final SideType[] sides;
    private Rotation appliedRotation;
    private Coordinates fixedAt;
    private String cliSymbol = "?";
    private int id;
    private String textureName;

    /**
     * Construct the tile with specified parameters.
     * @param sides An array of sides, where for each Direction {@code d},
     *              its value {@code d.v <- d.getValue()} is used as index to specify the related side:
     *              {@code sides[d.v]} is the tile's side in direction {@code d}.
     * @throws IllegalArgumentException If the sides array does not contain one element for each possible direction
     *                                  in the {@link Direction values}.
     */
    public TileSkeleton(SideType[] sides) {
        if (sides == null || sides.length != Direction.TOTAL_DIRECTIONS) {
            throw new IllegalArgumentException("The sides array must contain exactly one side " +
                    "for each of the possible directions: " + Arrays.toString(Direction.values()));
        }
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
    @JsonIgnore
    public Coordinates getCoordinates() throws NotFixedTileException {
        if (fixedAt == null) {
            throw new NotFixedTileException("Attempt to retrieve unplaced tile's coordinates.");
        }
        return fixedAt;
    }

    /**
     * Retrieve {@code this} tile's coordinates.
     * @return {@code this} tile's coordinates.
     * @throws RuntimeException If {@code this} tile has not been placed yet.
     * @see #getCoordinates()
     */
    public Coordinates forceGetCoordinates() {
        try {
            return getCoordinates();
        } catch (NotFixedTileException e) {
            throw new RuntimeException(e);
        }
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
     * Remove {@code this} tile from its place
     * @throws NotFixedTileException If the tile does not have a place already.
     */
    public void unplace() throws NotFixedTileException {
        if (fixedAt == null) {
            throw new NotFixedTileException("Attempt to unplace an unplaced tile.");
        }
        fixedAt = null;
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
     * @throws FixedTileException If the tile has been already fixed.
     */
    public final void resetRotation() throws FixedTileException {
        rotateTile(appliedRotation.reversed());
    }

    public Rotation getAppliedRotation() {
        return appliedRotation;
    }

    /**
     * Get info about tiles adjacency.
     * @param neighbor The tile to check for adjacency.
     * @return {@code null} if this tile is not placed adjacent to {@code other} tile,
     * otherwise the direction to go from this tile to the {@code other}.
     */
    public Direction getNeighborDirection(TileSkeleton neighbor) {
        if (fixedAt == null) return null;
        return fixedAt.getNeighborDirection(neighbor.fixedAt);
    }

    /**
     * Sets the CLI (Command Line Interface) symbol representing this tile or entity.
     *
     * @param cliSymbol the symbol to be displayed in the CLI
     */
    public void setCLISymbol(String cliSymbol) {
        this.cliSymbol = cliSymbol;
    }

    /**
     * Returns the CLI (Command Line Interface) symbol representing this tile or entity.
     *
     * @return the CLI symbol
     */
    public String getCLISymbol() {
        return cliSymbol;
    }

    /**
     * Returns the unique identifier of the tile.
     *
     * @return the tile ID
     */
    public int getTileId() {
        return id;
    }

    /**
     * Sets the unique identifier of the tile.
     *
     * @param id the tile ID to set
     */
    public void setTileId(int id) {
        this.id = id;
    }

    /**
     * @return the name of the texture associated with this tile, or {@code null} if no texture name has been set
     */
    public String getTextureName() {
        return textureName;
    }

    /**
     * Sets the name of the texture associated with this tile
     *
     * @param textureName the tile texture name to set
     */
    public void setTextureName(String textureName) {
        this.textureName = textureName;
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

    public boolean equals(TileSkeleton other) {
        if (other == null) return false;
        return id == other.id;
    }

    @Override
    public CLIFrame getCLIRepresentation(){
        String firstLine = "┌" + SideType.getCLIRepresentation(getSide(Direction.NORTH), Direction.NORTH) + "┐";
        String secondLine = SideType.getCLIRepresentation(getSide(Direction.WEST), Direction.WEST) +
                            getCLISymbol() +
                            SideType.getCLIRepresentation(getSide(Direction.EAST), Direction.EAST);
        String thirdLine = "└" + SideType.getCLIRepresentation(getSide(Direction.SOUTH), Direction.SOUTH) + "┘";
        return new CLIFrame(new String[]{firstLine, secondLine, thirdLine});
    }

    public static String[] getFreeTileCLIRepresentation(int row, int col){
        String c = "▒▒";
        if((row + col) % 2 == 0){
            c = "░░";
        }
        String firstLine = "┌┉┉┐";
        String secondLine = "┊" + c +"┊";
        String thirdLine = "└┉┉┘";
        return new String[]{firstLine, secondLine, thirdLine};
    }

    public static String[] getForbiddenTileCLIRepresentation(int row, int col){
        String c = "▒▒▒▒";
        if((row + col) % 2 == 0){
            c = "░░░░";
        }
        return new String[]{c, c, c};
    }

    /**
     * Used to retrieve a tile name for CLI representation.
     * @return The tile name and details.
     */
    public abstract String getName();


}
