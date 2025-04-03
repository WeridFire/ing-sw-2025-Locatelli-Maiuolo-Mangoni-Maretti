package it.polimi.ingsw.shipboard;

import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.enums.ProtectionType;
import it.polimi.ingsw.enums.Rotation;
import it.polimi.ingsw.shipboard.tiles.*;
import it.polimi.ingsw.shipboard.tiles.exceptions.FixedTileException;
import it.polimi.ingsw.shipboard.visitors.*;
import it.polimi.ingsw.shipboard.visitors.integrity.*;
import it.polimi.ingsw.shipboard.exceptions.*;
import it.polimi.ingsw.util.BoardCoordinates;
import it.polimi.ingsw.util.Coordinates;

import java.util.*;

public class ShipBoard {

	private final GameLevel level;
	private final Map<Coordinates, TileSkeleton> board;

	private VisitorCalculateCargoInfo visitorCalculateCargoInfo;
	private VisitorCalculatePowers visitorCalculatePowers;
	private VisitorCalculateShieldedSides visitorCalculateShieldedSides;
	private VisitorCheckIntegrity visitorCheckIntegrity;

	public ShipBoard(GameLevel level) {
		board = new HashMap<>();
		this.level = level;
	}

	private void resetVisitors() {
		visitorCalculateCargoInfo = new VisitorCalculateCargoInfo();
		visitorCalculatePowers = new VisitorCalculatePowers();
		visitorCalculateShieldedSides = new VisitorCalculateShieldedSides();
		visitorCheckIntegrity = new VisitorCheckIntegrity();

		for (TileSkeleton tile : board.values()) {
			tile.accept(visitorCalculateCargoInfo);
			tile.accept(visitorCalculatePowers);
			tile.accept(visitorCalculateShieldedSides);
			tile.accept(visitorCheckIntegrity);
		}
	}

	public VisitorCalculateCargoInfo getVisitorCalculateCargoInfo() {
		return visitorCalculateCargoInfo;
	}

	public VisitorCalculatePowers getVisitorCalculatePowers() {
		return visitorCalculatePowers;
	}

	public VisitorCalculateShieldedSides getVisitorCalculateShieldedSides() {
		return visitorCalculateShieldedSides;
	}

	public VisitorCheckIntegrity getVisitorCheckIntegrity() {
		return visitorCheckIntegrity;
	}

	/**
	 * Retrieves a set of all tiles currently placed on the board, mapped to their coordinates.
	 * The returned set is unmodifiable to prevent external modifications.
	 *
	 * @return A copy of the board.
	 */
	public Map<Coordinates, TileSkeleton> getTilesOnBoard() {
		return Map.copyOf(board);
	}

	/**
	 * Retrieves the set of coordinates that are currently occupied by tiles on the board.
	 * The returned set is unmodifiable to prevent external modifications.
	 *
	 * @return An unmodifiable set of coordinates where tiles are placed.
	 */
	public Set<Coordinates> getOccupiedCoordinates() {
		return Set.copyOf(board.keySet());
	}

	/**
	 * Retrieves a set of all the tiles currently placed on the board.
	 * The returned set is unmodifiable to prevent external modifications.
	 *
	 * @return An unmodifiable set of all and only the tiles placed onto the shipboard.
	 */
	public Set<TileSkeleton> getTiles() {
		return Set.copyOf(board.values());
	}

	/**
	 * Retrieves only the tiles placed in the provided coordinates.
	 * If a coordinate value is not associated with a tile (no tile found) it is simply not considered.
	 *
	 * @param coordinates The coordinates to check to retrieve tiles.
	 * @return All and only the tiles placed onto the shipboard which share the position with the provided coordinates.
	 */
	public Set<Tile> getTiles(Set<Coordinates> coordinates) {
		Set<Tile> placedTiles = new HashSet<>();
		for (Coordinates coordinate : coordinates) {
			try {
				placedTiles.add(getTile(coordinate));
			} catch (OutOfBuildingAreaException | NoTileFoundException e) {
				// do nothing: ok
			}
		}
		return placedTiles;
	}

	/**
	 * Retrieves the tile located at the given coordinates.
	 *
	 * @param coordinates The coordinates of the tile to retrieve.
	 * @return The tile at the specified coordinates.
	 * @throws OutOfBuildingAreaException If the coordinates are outside the valid building area.
	 * @throws NoTileFoundException If no tile is found at the given coordinates.
	 */
	public TileSkeleton getTile(Coordinates coordinates)
			throws OutOfBuildingAreaException, NoTileFoundException {
		if (!BoardCoordinates.isOnBoard(level, coordinates)) {
			throw new OutOfBuildingAreaException(level, coordinates);
		}
		TileSkeleton result = board.get(coordinates);
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
	 * @throws FixedTileException If the provided tile has already been placed.
	 */
	public void setTile(TileSkeleton tile, Coordinates coordinates) throws OutOfBuildingAreaException,
            TileAlreadyPresentException, IllegalArgumentException, FixedTileException {
		if (tile == null) {
			throw new IllegalArgumentException("Tile cannot be null");
		}
		if (!BoardCoordinates.isOnBoard(level, coordinates)) {
			throw new OutOfBuildingAreaException(level, coordinates);
		}
		if (board.containsKey(coordinates)) {
			throw new TileAlreadyPresentException(coordinates, board.get(coordinates));
		}
		board.put(coordinates, tile);
		tile.place(coordinates);
	}


	/**
	 * Prepares the ship for flight.
	 * @implNote Resets all visitors (also integrity check is called).
	 */
	public void startFlight() {
		resetVisitors();
	}

	/**
	 * Removes a tile from the board at the specified coordinates.
	 *
	 * @param coordinates The coordinates of the tile to be removed.
	 * @throws OutOfBuildingAreaException If the specified coordinates are outside the valid building area.
	 * @throws NoTileFoundException If there is no tile at the given coordinates.
	 */
	private void removeTile(Coordinates coordinates) throws OutOfBuildingAreaException, NoTileFoundException {
		// Check if the coordinates are within the valid board area
		if (!BoardCoordinates.isOnBoard(level, coordinates)) {
			throw new OutOfBuildingAreaException(level, coordinates);
		}

		// Check if a tile actually exists at the given coordinates
		if (!board.containsKey(coordinates)) {
			throw new NoTileFoundException(coordinates);
		}

		// Remove the tile and reset visitors
		board.remove(coordinates);
		resetVisitors();
	}

    /**
     * Finds the first tile in a given direction from a specified coordinate.
     * <p>
     * This method searches for the first tile along the given direction, starting from the
     * specified coordinate value interpreting it as row or column depending on the specified direction.
     *
     * @param direction The direction to search in (EAST, WEST, NORTH, SOUTH).
     * @param coordinate The coordinate along the perpendicular axis to the direction.
     * @return The {@link Coordinates} of the first found tile, or {@code null} if no tile is found.
     */
    private Coordinates getFirstTileLocation(Direction direction, int coordinate) {
        int firstCoordValue = BoardCoordinates.getFirstCoordinateFromDirection(direction);
        Coordinates coord = switch (direction) {
            case EAST, WEST -> new Coordinates(coordinate, firstCoordValue);
            case NORTH, SOUTH -> new Coordinates(firstCoordValue, coordinate);
        };

        Direction checkDirection = direction.getRotated(Rotation.OPPOSITE);
        int maxIterations = BoardCoordinates.getFirstCoordinateFromDirection(checkDirection) - firstCoordValue;
        if (maxIterations < 0) {
            maxIterations = -maxIterations;
        }

        for (int i = 0; i < maxIterations; i++) {
            if (board.containsKey(coord)) {
                return coord;
            }
            coord = coord.getNext(checkDirection);
        }

        return null;
    }

    /**
     * Hits a tile at the first found position in the given direction.
     * <p>
     * This method find and remove a tile by searching in the given direction from the specified coordinate.
     *
     * @param direction The direction from which the hit is coming.
     * @param coordinate The coordinate along the perpendicular axis to the direction.
     * @throws NoTileFoundException If no tile is found in the given direction and coordinate.
     * @throws OutOfBuildingAreaException If the given coordinate value does not represent
     * a valid row or column for the board.
     */
    public void hit(Direction direction, int coordinate) throws NoTileFoundException, OutOfBuildingAreaException {
        Coordinates coordinates = getFirstTileLocation(direction, coordinate);
        if (coordinates == null) {
            throw new NoTileFoundException("Attempt to hit the shipboard from non-valid direction (" +
                    direction + ") & coordinate (" + coordinate + ")");
        } else {
            removeTile(coordinates);
        }
    }

    /**
     * Checks whether there is a cannon pointing in the given direction.
     * <p>
     * This method iterates over a defined rectangular area and determines if there is
     * a cannon that is properly oriented towards the specified direction.
     *
     * @param checkForDoubleCannon Whether to check for double cannons ({@code true})
     *                             or a single cannon ({@code false}).
     * @param pointingDirection The direction in which the cannon should be pointing.
     * @param coordTopLeft The top-left coordinate of the search area.
     * @param coordBottomRight The bottom-right coordinate of the search area.
     * @return {@code true} if a cannon pointing in the given direction is found, otherwise {@code false}.
     */
    private boolean hasCannonPointing(boolean checkForDoubleCannon, Direction pointingDirection,
                                      Coordinates coordTopLeft, Coordinates coordBottomRight) {
        for (int row = coordTopLeft.getRow(); row <= coordBottomRight.getRow(); row++) {
            for (int col = coordTopLeft.getColumn(); col <= coordBottomRight.getColumn(); col++) {
                Coordinates coord = new Coordinates(row, col);
                if (visitorCalculatePowers.getInfoFirePower().isPresent(coord)) {  // there is a cannon
                    if ((checkForDoubleCannon == visitorCalculatePowers.getInfoFirePower()
                            .getLocationsToActivate().containsKey(coord))
                            // cannon type is the target cannon type
                            && (board.get(coord).getSide(pointingDirection) == SideType.CANNON)) {
                            // cannon is pointing in the target direction
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines the level of protection from cannons in a given direction, coordinate and range.
     * <p>
     * This method checks for the presence of single or double cannons within a certain range
     * in the specified direction and returns the corresponding {@link ProtectionType}.
     * <p>
     * Single cannons have priority over double cannons: if both a single and a double cannon are found,
     * the protection is given by the single cannon (no need for battery: low entropy -> Stevino wins).
     *
     * @param direction The direction in which to check for cannon protection.
     * @param coordinate The coordinate along the perpendicular axis to the direction.
     * @param range The range within which to search for cannons.
     *              This equivalently represents the number of rows (or columns) to check, centered in the
     *              {@code coordinate} row/column with the given direction.
     *              It should be an odd integer for symmetry.
     * @return The level of protection, which can be: {@link ProtectionType#SINGLE_CANNON},
     *         {@link ProtectionType#DOUBLE_CANNON} or {@link ProtectionType#NONE}.
	 *
	 * @throws IllegalArgumentException If {@code range <= 0}.
     */
    public ProtectionType getCannonProtection(Direction direction, int coordinate, int range) {
		if (range <= 0) {
			throw new IllegalArgumentException("range must be greater than zero. " + range + " provided.");
		}

		// create coordinates box range
        int firstCoordValue = BoardCoordinates.getFirstCoordinateFromDirection(direction);
        Coordinates coordTopLeft = null, coordBottomRight = null;
		int halfRangeDown = (range - 1) / 2;
		int halfRangeUp = (range - 1) - halfRangeDown;
        switch (direction) {
            case EAST, WEST:
                coordTopLeft = new Coordinates(coordinate - halfRangeDown, firstCoordValue);
                coordBottomRight = new Coordinates(coordinate + halfRangeUp, firstCoordValue);
                break;
            case NORTH, SOUTH:
                coordTopLeft = new Coordinates(firstCoordValue, coordinate - halfRangeDown);
                coordBottomRight = new Coordinates(firstCoordValue, coordinate + halfRangeUp);
                break;
        }

		// first search for single cannons: higher priority
        if (hasCannonPointing(false, direction, coordTopLeft, coordBottomRight)) {
            return ProtectionType.SINGLE_CANNON;
        }
		// if no single cannon is found: search for double cannons
        if (hasCannonPointing(true, direction, coordTopLeft, coordBottomRight)) {
            return ProtectionType.DOUBLE_CANNON;
        }
		// if here: no cannon found
        return ProtectionType.NONE;
    }


	/**
	 * Processes the removal of contraband items.
	 *
	 * @param quantityToRemove the number of contraband items to remove
	 */
	public void acceptSmugglers(int quantityToRemove) {
		VisitorSmugglers smugglers = new VisitorSmugglers(quantityToRemove);
		for (TileSkeleton tile : board.values()) {
			tile.accept(smugglers);
		}
		smugglers.removeMostValuableItems(quantityToRemove);
	}

	public GameLevel getLevel(){
		return level;
	}

	public List<String> getCLIRepresentation() {
		// Determine the board boundaries.
		int minRow = 5;
		int maxRow = 9;
		int minCol = 3;
		int maxCol = 11;
		List<String> result = new ArrayList<>();
		StringBuilder frame = new StringBuilder();
		frame.append(" "); //offset by 1 to account for the vertical frame
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

			line1.append("─");
			line2.append(row);
			line3.append("─");

			// Iterate over each column for the current row.
			for (int col = minCol; col <= maxCol; col++) {
				Coordinates coord = new Coordinates(row, col);
				TileSkeleton tile = board.get(coord);
				String[] tileRep;

				if (tile != null) {
					tileRep = tile.getCLIRepresentation();
				} else {
					if(BoardCoordinates.isOnBoard(getLevel(), coord)){
						tileRep = TileSkeleton.getFreeTileCLIRepresentation(row, col);
					}else{
						tileRep = TileSkeleton.getForbiddenTileCLIRepresentation(row, col);
					}

				}

				line1.append(tileRep[0]);
				line2.append(tileRep[1]);
				line3.append(tileRep[2]);
			}

			line1.append("─");
			line2.append(row);
			line3.append("─");

			result.add(line1.toString());
			result.add(line2.toString());
			result.add(line3.toString());


		}
		result.add(frame.toString());
		return result;
	}


}
