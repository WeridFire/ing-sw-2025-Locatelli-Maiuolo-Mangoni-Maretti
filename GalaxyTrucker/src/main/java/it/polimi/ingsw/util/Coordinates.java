package it.polimi.ingsw.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.model.shipboard.tiles.TileSkeleton;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * Represents a coordinate in a grid-based system.
 * Each coordinate is defined by a row and a column and is uniquely identified by an ID.
 */
public class Coordinates implements Serializable {
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
     * Parse an array of coordinates as strings into a list of {@link Coordinates}
     * @param coordinatesString the content to parse (e.g. "[(8; 8), (8; 7), (7; 6), (7; 7)]")
     * @return the list of parsed coordinates (e.g. List.of(new Coordinates(8,8), new Coordinates(8,7), ...))
     */
    public static List<Coordinates> parseArray(String coordinatesString) {
        List<Coordinates> coordinatesList = new ArrayList<>();

        if (coordinatesString == null || coordinatesString.isBlank()) {
            return coordinatesList;
        }

        // remove outer brackets and whitespace
        String cleaned = coordinatesString.trim();
        if (cleaned.startsWith("[") && cleaned.endsWith("]")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }

        // match pairs like (8; 8)
        String[] pairs = cleaned.split("\\),\\s*\\(");
        for (String pair : pairs) {
            pair = pair.replace("(", "").replace(")", "").trim();
            String[] parts = pair.split("[;,]");
            if (parts.length == 2) {
                try {
                    int row = Integer.parseInt(parts[0].trim());
                    int col = Integer.parseInt(parts[1].trim());
                    coordinatesList.add(new Coordinates(row, col));
                } catch (NumberFormatException e) {
                    // skip malformed coordinate
                }
            }
        }

        return coordinatesList;
    }


    /**
     * Constructs a coordinate with the specified row and column.
     * @implNote The unique ID is computed as {@code row * MAX_COL + column}.
     *
     * @param row the row index of the coordinate
     * @param column the column index of the coordinate
     */
    @JsonCreator
    public Coordinates(@JsonProperty("row") int row, @JsonProperty("column") int column) {
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
     * @return the coordinate row
     */
    public int getRow() {
        return row;
    }

    /**
     * @return the coordinate column
     */
    public int getColumn() {
        return column;
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
    @JsonIgnore
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
    @JsonIgnore
    public Set<Coordinates> getNeighbors() {
        Set<Coordinates> neighbors = new HashSet<>();
        for (Direction direction : Direction.values()) {
            neighbors.add(getNext(direction));
        }
        return neighbors;
    }

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
        try {
            Coordinates other = (Coordinates) obj;
            return row == other.row && column == other.column;
        }
        catch (ClassCastException e) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, column);
    }


    public static class KeyDeserializer extends com.fasterxml.jackson.databind.KeyDeserializer {

        @Override
        public Object deserializeKey(String s, DeserializationContext deserializationContext) throws IOException {
            // Expected format: "(row; column)"
            s = s.replace("(", "").replace(")", "").trim();
            String[] parts = s.split(";");
            if (parts.length != 2) {
                throw new IOException("Invalid Coordinates key: " + s);
            }
            int row = Integer.parseInt(parts[0].trim());
            int column = Integer.parseInt(parts[1].trim());
            return new Coordinates(row, column);
        }
    }
}

