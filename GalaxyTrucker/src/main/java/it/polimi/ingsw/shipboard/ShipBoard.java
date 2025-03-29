package src.main.java.it.polimi.ingsw.shipboard;

import src.main.java.it.polimi.ingsw.enums.GameLevel;
import src.main.java.it.polimi.ingsw.shipboard.tiles.*;
import src.main.java.it.polimi.ingsw.shipboard.tiles.exceptions.FixedTileException;
import src.main.java.it.polimi.ingsw.shipboard.tiles.exceptions.NotEnoughItemsException;
import src.main.java.it.polimi.ingsw.shipboard.visitors.*;
import src.main.java.it.polimi.ingsw.shipboard.visitors.integrity.*;
import src.main.java.it.polimi.ingsw.shipboard.exceptions.*;
import src.main.java.it.polimi.ingsw.util.BoardCoordinates;
import src.main.java.it.polimi.ingsw.util.Coordinates;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ShipBoard {

	private final GameLevel level;
	private final Map<Coordinates, TileSkeleton<SideType>> board;
	private final Set<Coordinates> activatedTiles = new HashSet<>();

	private VisitorCalculateCargoInfo visitorCalculateCargoInfo;
	private VisitorCalculateFirePower visitorCalculateFirePower;
	private VisitorCalculateThrustPower visitorCalculateThrustPower;
	private VisitorCalculateShieldedSides visitorCalculateShieldedSides;
	private VisitorCheckIntegrity visitorCheckIntegrity;

	public ShipBoard(GameLevel level) {
		board = new HashMap<>();
		this.level = level;
	}

	private void resetVisitors() {
		visitorCalculateCargoInfo = new VisitorCalculateCargoInfo();
		visitorCalculateFirePower = new VisitorCalculateFirePower();
		visitorCalculateThrustPower = new VisitorCalculateThrustPower();
		visitorCalculateShieldedSides = new VisitorCalculateShieldedSides();
		visitorCheckIntegrity = new VisitorCheckIntegrity();

		for (TileSkeleton<SideType> tile : board.values()) {
			tile.accept(visitorCalculateCargoInfo);
			tile.accept(visitorCalculateFirePower);
			tile.accept(visitorCalculateThrustPower);
			tile.accept(visitorCalculateShieldedSides);
			tile.accept(visitorCheckIntegrity);
		}
	}

	public VisitorCalculateCargoInfo getVisitorCalculateCargoInfo() {
		return visitorCalculateCargoInfo;
	}

	public VisitorCalculateFirePower getVisitorCalculateFirePower() {
		return visitorCalculateFirePower;
	}

	public VisitorCalculateThrustPower getVisitorCalculateThrustPower() {
		return visitorCalculateThrustPower;
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
	public Map<Coordinates, TileSkeleton<SideType>> getTilesOnBoard() {
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
	public Set<TileSkeleton<SideType>> getTiles() {
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
	public TileSkeleton<SideType> getTile(Coordinates coordinates)
			throws OutOfBuildingAreaException, NoTileFoundException {
		if (!BoardCoordinates.isOnBoard(level, coordinates)) {
			throw new OutOfBuildingAreaException(level, coordinates);
		}
		TileSkeleton<SideType> result = board.get(coordinates);
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
	public void setTile(TileSkeleton<SideType> tile, Coordinates coordinates) throws OutOfBuildingAreaException,
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
	public void removeTile(Coordinates coordinates) throws OutOfBuildingAreaException, NoTileFoundException {
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
	 * Processes the removal of contraband items.
	 *
	 * @param quantityToRemove the number of contraband items to remove
	 */
	public void acceptSmugglers(int quantityToRemove) {
		VisitorSmugglers smugglers = new VisitorSmugglers(quantityToRemove);
		for (TileSkeleton<SideType> tile : board.values()) {
			tile.accept(smugglers);
		}
		smugglers.removeMostValuableItems(quantityToRemove);
	}

	/**
	 * Retrieves the set of currently activated tiles.
	 *
	 * @return a set of {@link Coordinates} representing activated tiles
	 */
	public Set<Coordinates> getActivatedTiles() {
		return activatedTiles;
	}

	/**
	 * Clears the set of activated tiles, resetting their state.
	 */
	public void resetActivatedTiles() {
		activatedTiles.clear();
	}

	/**
	 * Calculates the number of available battery units that are not currently used for tile activation.
	 *
	 * @return the number of available batteries
	 */
	private int getAvailableBatteriesAmount() {
		return (getVisitorCalculateCargoInfo()
				.getInfoAllContainers()
				.count(LoadableType.BATTERY) - getActivatedTiles().size());
	}

	/**
	 * Activates a set of tiles if there are enough available batteries to power them.
	 *
	 * @param coordinates a set of tile coordinates to activate
	 * @throws NotEnoughItemsException if there are not enough available batteries to activate the tiles
	 */
	public void activateTiles(Set<Coordinates> coordinates) throws NotEnoughItemsException {
		if (getAvailableBatteriesAmount() >= coordinates.size()) {
			activatedTiles.addAll(coordinates);
		} else {
			throw new NotEnoughItemsException("Attempt to activate more tiles than batteries available");
		}
	}

}
