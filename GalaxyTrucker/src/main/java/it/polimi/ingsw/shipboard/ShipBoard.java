package src.main.java.it.polimi.ingsw.shipboard;

import src.main.java.it.polimi.ingsw.enums.GameLevel;
import src.main.java.it.polimi.ingsw.shipboard.exceptions.NoTileFoundException;
import src.main.java.it.polimi.ingsw.shipboard.exceptions.OutOfBuildingAreaException;
import src.main.java.it.polimi.ingsw.shipboard.exceptions.TileAlreadyPresentException;
import src.main.java.it.polimi.ingsw.shipboard.tiles.Tile;
import src.main.java.it.polimi.ingsw.util.BoardCoordinates;
import src.main.java.it.polimi.ingsw.util.Coordinates;

import java.util.*;


/**
 * Represents a ship board where tiles can be placed according to the game level's constraints.
 * Each tile is mapped to specific coordinates, ensuring valid placement (following the rules) and retrieval.
 * Other methods must be implemented.
 */
public class ShipBoard {

    /** The game level, which defines the board's constraints. */
    private final GameLevel level;

    /** A mapping between coordinate IDs and the corresponding tiles placed on the board. */
    private final HashMap<Integer, Tile> board;

    /**
     * Constructs a new ship board for the given game level.
     *
     * @param level The game level defining the board's constraints.
     */
    public ShipBoard(GameLevel level) {
        this.level = level;
        board = new HashMap<>();
    }

    /**
     * Retrieves the tile located at the given coordinates.
     *
     * @param coordinates The coordinates of the tile to retrieve.
     * @return The tile at the specified coordinates.
     * @throws OutOfBuildingAreaException If the coordinates are outside the valid building area.
     * @throws NoTileFoundException If no tile is found at the given coordinates.
     */
    public Tile getTile(Coordinates coordinates) throws OutOfBuildingAreaException, NoTileFoundException {
        if (!BoardCoordinates.isOnBoard(level, coordinates)) {
            throw new OutOfBuildingAreaException(level, coordinates);
        }
        Tile result = board.get(coordinates.getID());
        if (result == null) {
            throw new NoTileFoundException(coordinates);
        }
        return result;
    }

    /**
     * Places a tile at the specified coordinates on the board.
     *
     * @param tile The tile to place.
     * @param coordinates The coordinates where the tile should be placed.
     * @throws OutOfBuildingAreaException If the coordinates are outside the valid building area.
     * @throws TileAlreadyPresentException If there is already a tile at the specified coordinates.
     * @throws IllegalArgumentException If the provided tile is null.
     */
    public void setTile(Tile tile, Coordinates coordinates) throws OutOfBuildingAreaException,
            TileAlreadyPresentException, IllegalArgumentException {
        if (tile == null) {
            throw new IllegalArgumentException("Tile cannot be null");
        }
        if (!BoardCoordinates.isOnBoard(level, coordinates)) {
            throw new OutOfBuildingAreaException(level, coordinates);
        }
        if (board.containsKey(coordinates.getID())) {
            throw new TileAlreadyPresentException(coordinates, board.get(coordinates.getID()));
        }
        board.put(coordinates.getID(), tile);
    }
}

