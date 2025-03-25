package src.main.java.it.polimi.ingsw.util;

import src.main.java.it.polimi.ingsw.enums.Direction;

import java.util.*;

/**
 * Represents a coordinate in a grid-based system.
 * Each coordinate is defined by a row and a column and is uniquely identified by an ID.
 */
public class Coordinates {
    /** The maximum number of columns in the grid.
     * Based on the maximum grid possible.
     */
    private static final int MAX_COL = 12;

    /** The row index of the coordinate. */
    private final int row;

    /** The column index of the coordinate. */
    private final int column;

    /** A unique identifier for the coordinate, computed based on its position in the grid. */
    private final int id;

    /**
     * Constructs a coordinate with the specified row and column.
     * @implNote The unique ID is computed as {@code row * MAX_COL + column}.
     *
     * @param row the row index of the coordinate
     * @param column the column index of the coordinate
     */
    public Coordinates(int row, int column) {
        this.row = row;
        this.column = column;
        id = row * MAX_COL + column;
    }

    /**
     * Creates a {@code Coordinates} instance from a unique ID.
     * The row and column are derived from the given ID based on the grid's structure.
     *
     * @param id the unique identifier representing a coordinate
     * @return a {@code Coordinates} object corresponding to the given ID
     */
    public static Coordinates fromID(int id) {
        return new Coordinates(id / MAX_COL, id % MAX_COL);
    }

    /**
     * Returns the unique identifier associated with this coordinate.
     *
     * @return the unique coordinate ID
     */
    public int getID() {
        return id;
    }

    /**
     * Computes and returns the adjacent coordinate in the specified direction.
     * <p>
     * If the direction is:
     * <ul>
     *     <li>{@code NORTH} → Moves one row up (-1 row)</li>
     *     <li>{@code SOUTH} → Moves one row down (+1 row)</li>
     *     <li>{@code EAST} → Moves one column right (+1 column)</li>
     *     <li>{@code WEST} → Moves one column left (-1 column)</li>
     * </ul>
     *
     * @param direction the direction in which to move
     * @return a new {@code Coordinates} object representing the adjacent position
     */
    public Coordinates getNext(Direction direction) {
        int newRow = switch (direction) {
            case SOUTH -> row + 1;
            case NORTH -> row - 1;
            default -> row;
        };
        int newColumn = switch (direction) {
            case EAST -> column + 1;
            case WEST -> column - 1;
            default -> column;
        };
        return new Coordinates(newRow, newColumn);
    }

    /**
     * Returns a set of map entries of neighboring coordinates indexed by their respective directions from this.
     * <p>
     * This method provides a mapping between each possible movement direction and the corresponding adjacent coordinate.
     * It allows for structured iteration over all possible neighbors, with direct knowledge of their relative direction.
     * </p>
     *
     * @return a map where the keys are movement directions, and the values are the corresponding neighboring coordinates.
     */
    public Set<Map.Entry<Direction, Coordinates>> neighbors() {
        Map<Direction, Coordinates> neighbors = new EnumMap<>(Direction.class);
        for (Direction direction : Direction.values()) {
            neighbors.put(direction, getNext(direction));
        }
        return neighbors.entrySet();
    }


    /**
     * Retrieves the neighbors of this coordinates going in all the possible directions.
     *
     * @return The neighbors of this coordinates going in all the possible directions.
     */
    public Set<Coordinates> getNeighbors() {
        Set<Coordinates> neighbors = new HashSet<>();
        for (Direction direction : Direction.values()) {
            neighbors.add(getNext(direction));
        }
        return neighbors;
    }

    /**
     * Get info about coordinates adjacency.
     * @param neighbor The other coordinates to check for adjacency.
     * @return {@code null} if this and {@code other} coordinates are not adjacent,
     * otherwise the direction to go from this tile to the {@code other}.
     */
    public Direction getNeighborDirection(Coordinates neighbor) {
        if (neighbor == null) return null;
        for (Direction direction : Direction.values()) {
            if (getNext(direction).equals(neighbor)) return direction;
        }
        return null;
    }

    @Override
    public String toString() {
        return "(" + row + "; " + column + ")";
    }

    /* implementation of equals and hashCode needed to work with new coordinates object but have the same behavior.
    Example:
        board.put(new Coordinates(5, 5), new Tile());
        Tile t = board.get(new Coordinates(5, 5));
    -> tile t will be the tile put in the line above!
     */

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Coordinates other = (Coordinates) obj;
        return row == other.row && column == other.column;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, column);
    }
}

