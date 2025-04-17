package it.polimi.ingsw.util;

import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.shipboard.tiles.TileSkeleton;
import it.polimi.ingsw.view.cli.CLIFrame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class that determines valid board coordinates for different game levels.
 * It precomputes and caches valid coordinates for each level to optimize lookups.
 */
public class BoardCoordinates {

    /** Stores valid coordinate sets for different game levels. */
    private static final HashMap<GameLevel, Set<Integer>> validCoordinates = new HashMap<>();

    /**
     * Calculates the set of valid coordinates for the game level {@link GameLevel#TESTFLIGHT}.
     *
     * @return A set of all and only the valid coordinate IDs for the game level {@link GameLevel#TESTFLIGHT}.
     */
    private static Set<Integer> calculateValidCoordinatesSmall() {
        /*      4   5   6   7   8   9   10
            5   .   .   .   #   .   .   .
            6   .   .   #   #   #   .   .
            7   .   #   #   #   #   #   .
            8   .   #   #   #   #   #   .
            9   .   #   #   .   #   #   .
         */
        return Stream.of(new Coordinates(5, 7), new Coordinates(6, 6),
                        new Coordinates(6, 7), new Coordinates(6, 8),
                        new Coordinates(7, 5), new Coordinates(7, 6),
                        new Coordinates(7, 7), new Coordinates(7, 8),
                        new Coordinates(7, 9), new Coordinates(8, 5),
                        new Coordinates(8, 6), new Coordinates(8, 7),
                        new Coordinates(8, 8), new Coordinates(8, 9),
                        new Coordinates(9, 5), new Coordinates(9, 6),
                        new Coordinates(9, 8), new Coordinates(9, 9))
                .map(Coordinates::getID)
                .collect(Collectors.toSet());
    }

    /**
     * Calculates the set of valid coordinates for the game level {@link GameLevel#TWO}.
     *
     * @return A set of all and only the valid coordinate IDs for the game level {@link GameLevel#TWO}.
     */
    private static Set<Integer> calculateValidCoordinatesMedium() {
        /*      4   5   6   7   8   9   10
            5   .   .   #   .   #   .   .
            6   .   #   #   #   #   #   .
            7   #   #   #   #   #   #   #
            8   #   #   #   #   #   #   #
            9   #   #   #   .   #   #   #
         */
        return Stream.of(new Coordinates(5, 6), new Coordinates(5, 8),
                        new Coordinates(6, 5), new Coordinates(6, 6),
                        new Coordinates(6, 7), new Coordinates(6, 8),
                        new Coordinates(6, 9), new Coordinates(7, 4),
                        new Coordinates(7, 5), new Coordinates(7, 6),
                        new Coordinates(7, 7), new Coordinates(7, 8),
                        new Coordinates(7, 9), new Coordinates(7, 10),
                        new Coordinates(8, 4), new Coordinates(8, 5),
                        new Coordinates(8, 6), new Coordinates(8, 7),
                        new Coordinates(8, 8), new Coordinates(8, 9),
                        new Coordinates(8, 10), new Coordinates(9, 4),
                        new Coordinates(9, 5), new Coordinates(9, 6),
                        new Coordinates(9, 8), new Coordinates(9, 9),
                        new Coordinates(9, 10))
                .map(Coordinates::getID)
                .collect(Collectors.toSet());
    }

    /**
     * Computes the set of valid coordinates for a given game level.
     *
     * @param level The game level for which to compute valid coordinates.
     * @return A set of all and only the valid coordinate IDs for the specified level, or {@code null} if unsupported.
     */
    private static Set<Integer> calculateValidCoordinates(GameLevel level) {
        return switch (level) {
            case TESTFLIGHT, ONE -> calculateValidCoordinatesSmall();
            case TWO -> calculateValidCoordinatesMedium();
            default -> null;
        };
    }

    /**
     * Determines whether the given coordinates are valid for the specified game level.
     *
     * @param level The game level.
     * @param coordinates The coordinates to check.
     * @return {@code true} if the coordinates are valid for the given level, {@code false} otherwise.
     * @throws UnsupportedOperationException If the specified level is not supported.
     */
    public static boolean isOnBoard(GameLevel level, Coordinates coordinates) {
        Set<Integer> allowedPlaces = validCoordinates.get(level);
        if (allowedPlaces == null) {
            allowedPlaces = calculateValidCoordinates(level);
            if (allowedPlaces == null) {
                throw new UnsupportedOperationException("level " + level + " is not supported as a board of coordinates");
            }
            validCoordinates.put(level, allowedPlaces);
        }

        return allowedPlaces.contains(coordinates.getID());
    }

    /**
     * Returns the first coordinate value valid on the board associated with a given direction.
     * <p>
     * These values are used as a starting point depending on the direction provided.
     * This method works for {@link GameLevel#TESTFLIGHT}, {@link GameLevel#ONE} and {@link GameLevel#TWO}.
     *
     * @param direction The direction for which to get the associated first coordinate value.
     * @return The first coordinate value corresponding to the provided direction.
     */
    public static int getFirstCoordinateFromDirection(Direction direction) {
        return switch (direction) {
            case EAST -> 10;
            case NORTH -> 5;
            case WEST -> 4;
            case SOUTH -> 9;
        };
    }

    /**
     * Returns the coordinates where to put the main cabin on the board.
     * It does NOT depend on {@link GameLevel}
     *
     * @return The main cabin coordinates.
     */
    public static Coordinates getMainCabinCoordinates() {
        return new Coordinates(7, 7);
    }

    /**
     * Generates a CLI representation of the game board for the specified game level.
     * <p>
     * The representation includes a grid structure with row and column numbers for easy identification.
     * It distinguishes between playable board tiles and forbidden tiles.
     *
     * @param level The game level for which the CLI representation should be generated.
     * @return A {@link CLIFrame} representing the game board for the given level.
     * @throws RuntimeException If the CLI representation for the given level is not implemented.
     */
    public static CLIFrame getCLIRepresentation(GameLevel level) {
        if (level != GameLevel.TESTFLIGHT && level != GameLevel.ONE && level != GameLevel.TWO) {
            throw new RuntimeException("Unimplemented CLI representation for level " + level);
        }

        int minRow = getFirstCoordinateFromDirection(Direction.NORTH) - 1;
        int maxRow = getFirstCoordinateFromDirection(Direction.SOUTH) + 1;
        int minCol = getFirstCoordinateFromDirection(Direction.WEST) - 1;
        int maxCol = getFirstCoordinateFromDirection(Direction.EAST) + 1;
        List<String> result = new ArrayList<>();
        StringBuilder frame = new StringBuilder();

        // column numbers to easily identify the shipboard
        frame.append("  ");  // offset by 2 to account for the vertical frame
        for (int col = minCol; col <= maxCol; col++) {
            if(col < 10){
                frame.append("│").append(col).append(" │");
            }else{
                frame.append("│").append(col).append("│");
            }
        }
        result.add(frame.toString());

        // Iterate over each row on the board.
        for (int row = minRow; row <= maxRow; row++) {
            // Each tile has 3 rows in its CLI representation.
            StringBuilder line1 = new StringBuilder();
            StringBuilder line2 = new StringBuilder();
            StringBuilder line3 = new StringBuilder();

            line1.append("──");
            line2.append((row < 10) ? " " : "").append(row);
            line3.append("──");

            // Iterate over each column for the current row.
            for (int col = minCol; col <= maxCol; col++) {
                Coordinates coord = new Coordinates(row, col);
                String[] tileRep;

                if (BoardCoordinates.isOnBoard(level, coord)) {
                    tileRep = TileSkeleton.getFreeTileCLIRepresentation(row, col);
                }
                else {
                    tileRep = TileSkeleton.getForbiddenTileCLIRepresentation(row, col);
                }

                line1.append(tileRep[0]);
                line2.append(tileRep[1]);
                line3.append(tileRep[2]);
            }

            line1.append("──");
            line2.append(row);
            line3.append("──");

            result.add(line1.toString());
            result.add(line2.toString());
            result.add(line3.toString());
        }

        return new CLIFrame(result.toArray(new String[0]));
    }
}
