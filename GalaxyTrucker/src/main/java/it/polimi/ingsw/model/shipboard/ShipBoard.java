package it.polimi.ingsw.model.shipboard;

import com.fasterxml.jackson.annotation.JsonIgnore;
import it.polimi.ingsw.TilesFactory;
import it.polimi.ingsw.enums.*;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.playerInput.PIRs.PIRHandler;
import it.polimi.ingsw.model.playerInput.PIRs.PIRMultipleChoice;
import it.polimi.ingsw.model.shipboard.exceptions.*;
import it.polimi.ingsw.model.shipboard.integrity.IntegrityProblem;
import it.polimi.ingsw.model.shipboard.integrity.IShipIntegrityListener;
import it.polimi.ingsw.model.shipboard.integrity.VisitorCheckIntegrity;
import it.polimi.ingsw.model.shipboard.tiles.*;
import it.polimi.ingsw.model.shipboard.visitors.*;
import it.polimi.ingsw.model.shipboard.tiles.exceptions.AlreadyInitializedCabinException;
import it.polimi.ingsw.model.shipboard.tiles.exceptions.FixedTileException;
import it.polimi.ingsw.model.shipboard.tiles.exceptions.NotFixedTileException;
import it.polimi.ingsw.model.shipboard.tiles.exceptions.UnsupportedLoadableItemException;
import it.polimi.ingsw.util.*;
import it.polimi.ingsw.view.cli.ANSI;
import it.polimi.ingsw.view.cli.CLIFrame;
import it.polimi.ingsw.view.cli.CLIScreen;
import it.polimi.ingsw.view.cli.ICLIPrintable;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public class ShipBoard implements ICLIPrintable, Serializable {

	private final GameLevel level;
	private final Map<Coordinates, TileSkeleton> board;
	private MainCabinTile.Color color;

	private final CLIFrame emptyRepresentation;

	private boolean endedAssembly;
	private boolean filled;
	private boolean endedFlight;

	private final List<IShipIntegrityListener> integrityListeners;
	private Integer countExposedConnectors;

	private VisitorCalculateCargoInfo visitorCalculateCargoInfo;
	private VisitorCalculatePowers visitorCalculatePowers;
	private VisitorCalculateShieldedSides visitorCalculateShieldedSides;
	private VisitorCheckIntegrity visitorCheckIntegrity;
	private VisitorLifeSupport visitorLifeSupport;


	protected ShipBoard(GameLevel level) {
		this.level = level;
		board = new HashMap<>();
		color = null;
		emptyRepresentation = BoardCoordinates.getCLIRepresentation(level);
		endedAssembly = false;
		filled = false;
		endedFlight = false;
		integrityListeners = new ArrayList<>();
		countExposedConnectors = null;
	}

	/**
	 * Creates a ShipBoard for the player with specified index, already managing the main cabin placement.
	 * @param level The game level to play (for the shipboard form)
	 * @param color The player color (for the main-cabin color)
	 * @return The created ShipBoard
	 * @throws IllegalArgumentException if {@code playerIndex} is not coherent with players number
	 */
	public static ShipBoard create(GameLevel level, MainCabinTile.Color color) {
		ShipBoard sb = new ShipBoard(level);
		sb.color = color;
		MainCabinTile mainCabin = TilesFactory.createMainCabinTile(sb.color);
        try {
            sb.forceSetTile(mainCabin, BoardCoordinates.getMainCabinCoordinates());
        } catch (FixedTileException e) {
            throw new RuntimeException(e);  // should never happen -> runtime error
        }
		return sb;
    }

	private void populateVisitors(Set<TileVisitor> visitors) {
		for (TileSkeleton tile : board.values()) {
			for (TileVisitor visitor : visitors) {
				tile.accept(visitor);
			}
		}
	}

	/**
	 * Resets and re-applies all visitor computations on the current board.
	 */
	public void resetVisitors() {
		// 1. life supports
		visitorLifeSupport = new VisitorLifeSupport();
		populateVisitors(Set.of(visitorLifeSupport));
		// ensure correct update for cabins allowed items
		visitorLifeSupport.updateLifeSupportSystems();

		// 2. other visitors
		visitorCalculateCargoInfo = new VisitorCalculateCargoInfo();
		visitorCalculatePowers = new VisitorCalculatePowers();
		visitorCalculateShieldedSides = new VisitorCalculateShieldedSides();

		// populate visitors
		populateVisitors(Set.of(
				visitorCalculateCargoInfo,
				visitorCalculatePowers,
				visitorCalculateShieldedSides)
		);

		// 3. after all: check integrity
		visitorCheckIntegrity = new VisitorCheckIntegrity();
		populateVisitors(Set.of(visitorCheckIntegrity));
	}

	/**
	 * Validates the current structure of the ship, if it's still flying.
	 * <p>
	 * This method recomputes all cargo, powers, shielded sides, and structural integrity
	 * by resetting and reapplying visitors to each tile.
	 * If any integrity problem is detected, registered listeners are notified.
	 * <p>
	 * Typically called after initialization or after structural modifications such as removing tiles.
	 */
	public void validateStructure() {
		if (endedFlight) return;
		countExposedConnectors = null;
		Logger.info("Starting visitors reset proces...");
		long startTime = System.currentTimeMillis();
		resetVisitors();
		Logger.info("Time elapsed: " + (System.currentTimeMillis() - startTime) + "ms");
		notifyIntegrityListeners(visitorCheckIntegrity.getProblem(!filled));
	}

	@JsonIgnore
	public VisitorCalculateCargoInfo getVisitorCalculateCargoInfo() {
		return visitorCalculateCargoInfo;
	}

	@JsonIgnore
	public VisitorCalculatePowers getVisitorCalculatePowers() {
		return visitorCalculatePowers;
	}

	@JsonIgnore
	public VisitorCalculateShieldedSides getVisitorCalculateShieldedSides() {
		return visitorCalculateShieldedSides;
	}

	@JsonIgnore
	public VisitorCheckIntegrity getVisitorCheckIntegrity() {
		return visitorCheckIntegrity;
	}

	/**
	 * Registers a {@link IShipIntegrityListener} to receive notifications
	 * about integrity problems.
	 *
	 * @param listener the listener to be attached; must not be {@code null}
	 */
	public void attachIntegrityListener(IShipIntegrityListener listener) {
		integrityListeners.add(listener);
	}

	/**
	 * Unregisters a previously attached {@link IShipIntegrityListener}.
	 *
	 * @param listener the listener to be detached
	 * @return {@code true} if the listener was successfully removed;
	 *         {@code false} if it was not registered
	 */
	public boolean detachIntegrityListener(IShipIntegrityListener listener) {
		return integrityListeners.remove(listener);
	}

	/**
	 * Notifies all registered {@link IShipIntegrityListener} instances
	 * about a detected integrity problem.
	 * @implNote IMPORTANT: it needs to be called also on {@code integrityProblem.isProblem() == false}
	 *
	 * @param integrityProblem the integrity problem that occurred; must not be {@code null}
	 */
	private void notifyIntegrityListeners(IntegrityProblem integrityProblem) {
		for (IShipIntegrityListener listener : integrityListeners) {
			listener.update(integrityProblem);
		}
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
	@JsonIgnore
	public Set<TileSkeleton> getTiles() {
		return Set.copyOf(board.values());
	}

	/**
	 * Returns the map representing the shipboard.
	 * The map is returned by reference, allowing direct modifications to the shipboard.
	 *
	 * @return the map representing the shipboard.
	 */

	public Map<Coordinates, TileSkeleton> getBoard() {
		return board;
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
	 * Get the color this ship has been initialized with, even if it has no main cabin anymore.
	 * Can not be null (see exceptions below).
	 *
	 * @return This ship's original main cabin color.
	 * @throws UninitializedShipboardException If the {@link ShipBoard} has not been properly constructed,
	 * hence the main cabin has never been assigned and no color can be retrieved.
	 */
	public MainCabinTile.Color getColor() throws UninitializedShipboardException {
		if (color == null) {
			throw new UninitializedShipboardException();
		}
		return color;
	}

	/**
	 * Places a tile at the specified coordinates on the board.
	 * <b>Use with caution</b>: this method bypasses internal board validations.
	 *
	 * @param tile The tile to place.
	 * @param coordinates The coordinates where the tile should be placed.
	 * @throws FixedTileException If the provided tile has already been placed.
	 * @throws NullPointerException If the provided tile is null.
	 */
	public void forceSetTile(TileSkeleton tile, Coordinates coordinates) throws FixedTileException {
		tile.place(coordinates);
		board.put(coordinates, tile);
	}

	/**
	 * Removes the tile at the specified coordinates on the board without triggering any visitor updates
	 * or integrity checks.
	 * <b>Use with caution</b>: this method bypasses internal board validations.
	 *
	 * @param coordinates the coordinates of the tile to remove; must not be {@code null}
	 * @return the previous value associated with {@code coordinates},
	 * or {@code null} if there was no mapping for {@code coordinates}.
	 */
	public TileSkeleton forceRemoveTile(Coordinates coordinates) {
		return board.remove(coordinates);
	}

	/**
	 * Places a tile at the specified coordinates on the board.
	 *
	 * @param tile The tile to place.
	 * @param coordinates The coordinates where the tile should be placed.
	 * @throws AlreadyEndedAssemblyException If this shipboard has already been consolidated as assembled.
	 * @throws OutOfBuildingAreaException If the coordinates are outside the valid building area.
	 * @throws TileAlreadyPresentException If there is already a tile at the specified coordinates.
	 * @throws FixedTileException If the provided tile has already been placed.
	 * @throws TileWithoutNeighborException If the provided coordinates are not adjacent to an already placed tile
	 * coordinates.
	 * @throws IllegalArgumentException If the provided tile is null.
	 */
	public void setTile(TileSkeleton tile, Coordinates coordinates) throws AlreadyEndedAssemblyException,
			OutOfBuildingAreaException, TileAlreadyPresentException, FixedTileException, TileWithoutNeighborException,
			IllegalArgumentException {
		if (endedAssembly) {
			throw new AlreadyEndedAssemblyException();
		}

		if (tile == null) {
			throw new IllegalArgumentException("Tile cannot be null");
		}

		if (!BoardCoordinates.isOnBoard(level, coordinates)) {
			throw new OutOfBuildingAreaException(level, coordinates);
		}

		if (board.containsKey(coordinates)) {
			throw new TileAlreadyPresentException(coordinates, board.get(coordinates));
		}

		boolean hasNeighbor = false;
		for (Coordinates neighbor : coordinates.getNeighbors()) {
			if (board.containsKey(neighbor)) {
				hasNeighbor = true;
				break;
			}
		}
		if (!hasNeighbor) {
			throw new TileWithoutNeighborException(coordinates);
		}

		forceSetTile(tile, coordinates);
	}


	/**
	 * Prepares the ship for flight.
	 * @implNote Resets all visitors (also integrity check is called).
	 */
	public void endAssembly() throws AlreadyEndedAssemblyException {
		if (endedAssembly) {
			throw new AlreadyEndedAssemblyException();
		}
		endedAssembly = true;
		validateStructure();
	}


	/**
	 * Notify the shipboard that the flight ended (no more interaction accepted)
	 */
	public void endFlight() {
		endedFlight = true;
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

		// Remove the tile and revalidate structure
		board.remove(coordinates);
		validateStructure();
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
    public Coordinates getFirstTileLocation(Direction direction, int coordinate) {
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
	 *
	 * @return if the shipboard assembly process is finished.
	 */
	public boolean isEndedAssembly() {
		return endedAssembly;
	}

	/**
	 * Determines the level of protection from cannons in a given direction and coordinate.
	 * <p>
	 * This method checks for the presence of single or double cannons
	 * in the specified direction and returns the corresponding {@link ProtectionType}.
	 * <p>
	 * Single cannons have priority over double cannons: if both a single and a double cannon are found,
	 * the protection is given by the single cannon (no need for battery: low entropy -> Stevino wins).
	 *
	 * @param direction The direction in which to check for cannon protection.
	 * @param coordinate The coordinate along the perpendicular axis to the direction.
	 * @return The level of protection, which can be: {@link ProtectionType#SINGLE_CANNON},
	 *         {@link ProtectionType#DOUBLE_CANNON} or {@link ProtectionType#NONE}.
	 */
	public ProtectionType getCannonProtection(Direction direction, int coordinate) {

		// create coordinates box range
		int firstCoordValue = BoardCoordinates.getFirstCoordinateFromDirection(direction);
		int lastCoordValue = BoardCoordinates.getFirstCoordinateFromDirection(direction.getRotated(Rotation.OPPOSITE));
		Coordinates coordTopLeft = null, coordBottomRight = null;
		switch (direction) {
			case EAST:
				coordTopLeft = new Coordinates(coordinate - 1, lastCoordValue);
				coordBottomRight = new Coordinates(coordinate + 1, firstCoordValue);
				break;
			case NORTH:
				coordTopLeft = new Coordinates(firstCoordValue, coordinate);
				coordBottomRight = new Coordinates(lastCoordValue, coordinate);
				break;
			case WEST:
				coordTopLeft = new Coordinates(coordinate - 1, firstCoordValue);
				coordBottomRight = new Coordinates(coordinate + 1, lastCoordValue);
				break;
			case SOUTH:
				coordTopLeft = new Coordinates(lastCoordValue, coordinate - 1);
				coordBottomRight = new Coordinates(firstCoordValue, coordinate + 1);
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
	public void loseBestGoods(int quantityToRemove) {
		VisitorSmugglers smugglers = new VisitorSmugglers(quantityToRemove);
		for (TileSkeleton tile : board.values()) {
			tile.accept(smugglers);
		}
		smugglers.removeMostValuableItems(quantityToRemove);
	}

	/**
	 * Processes the removal of crew.
	 *
	 * @param quantityToRemove the number of crew items to remove
	 */
	public void loseCrew(int quantityToRemove) {
		visitorCalculateCargoInfo.getCrewInfo().removeUpTo(LoadableType.CREW_SET, quantityToRemove);
	}

	/**
	 * Processes the removal (usage) of batteries.
	 *
	 * @param quantityToRemove the number of batteries to remove
	 *
	 * @implSpec requires to be called when this ship has enough batteries (>= quantityToRemove)
	 */
	public void loseBatteries(int quantityToRemove) {
		visitorCalculateCargoInfo.getBatteriesInfo().removeUpTo(Set.of(LoadableType.BATTERY), quantityToRemove);
	}

	/**
	 * This function auto-fills itself with elements that initialize the game, for example batteries and humans.
	 * Also for non-obvious fills, such as cabins in which there can be multiple type of loadables filled (eg aliens / humans)
	 * It uses the PIRHandler to request the player's preference.
	 * @param p The player to send any eventual request to
	 * @param handler A reference to the PIRHandler, to handle these requests.
	 */
	public void fill(Player p, PIRHandler handler){
		System.out.println("Filling up shipboard: " + p.getUsername());

		//Fill batteries
		getVisitorCalculateCargoInfo()
				.getBatteriesInfo()
				.getLocations()
				.values()
				.forEach(BatteryComponentTile::fill);


		Set<LoadableType> loadedAliens = new HashSet<>(); //this is a set to keep track of the aliens added,
														// to prevent adding more than 1 alien of the same type.
		getVisitorCalculateCargoInfo()
				.getCrewInfo()
				.getLocations()
				.values()
				.forEach((cabin) -> {
					List<LoadableType> allowedTypes = cabin
							.getAllowedItems()
							.stream()
							.sorted(Comparator.naturalOrder())
							.collect(Collectors.toCollection(ArrayList::new));
 					//We need it ordered

					allowedTypes.removeAll(loadedAliens); //Remove any alien that was already added, to prevent adding duplicates
					LoadableType fillType = allowedTypes.getFirst(); //Get default choice
					if(allowedTypes.size() > 1){
						String[] choices = allowedTypes.stream().map((type) -> {
							int amount = cabin.getCapacityLeft() / type.getRequiredCapacity();
							return amount + " units of " + type.name();
						}).toArray(String[]::new); //Generate messages for each type
						try {
							PIRMultipleChoice choicePir = new PIRMultipleChoice(p,
									Default.PIR_SECONDS,
									"What type of crew do you want to add in cabin at coordinates "
											+ cabin.getCoordinates().toString() + "?",
									choices,
									0
							);
							int selected = handler.setAndRunTurn(choicePir);
							fillType = allowedTypes.get(selected);
							if(fillType == LoadableType.PURPLE_ALIEN || fillType == LoadableType.BROWN_ALIEN){
								loadedAliens.add(fillType);
							}
						} catch (NotFixedTileException e) {
							throw new RuntimeException(e); //shouldn't happen
						}
					}

					try {
						cabin.fillWith(fillType);
					} catch (AlreadyInitializedCabinException | UnsupportedLoadableItemException e) {
						throw new RuntimeException(e); //Shouldn't happen
					}
		});

		// here this shipboard is completely filled
		filled = true;
	}

	/**
	 *
	 * @return if the ship has been already filled with the starting content (batteries etc...)
	 */
	public boolean isFilled() {
		return filled;
	}

	public GameLevel getLevel(){
		return level;
	}

	private CLIFrame getInfoCliRepresentation() {
		/* info example
				"+--------------------+",
				"|      Overview      |",
				"+--------------------+",
				"| Total Crew: 8      |",
				"| Humans: 7          |",
				"| Purple Alien: Yes  |",
				"| Brown Alien: No    |",
				"+--------------------+",
				"| Red Goods: 0       |",
				"| Yellow Goods: 2    |",
				"| Green Goods: 0     |",
				"| Blue Goods: 3      |",
				"+--------------------+",
				"| Batteries: 5       |",
				"+--------------------+"
		 */

		CLIFrame info = CLIScreen.getScreenFrame(new int[] {1, 4, 4, 1}, 20, ANSI.RESET, ANSI.GREEN);
		info = info.merge(new CLIFrame("Overview"), AnchorPoint.TOP, AnchorPoint.TOP, 1, 0);
		info = info.merge(new CLIFrame(new String[] {
				"Total Crew: " + visitorCalculateCargoInfo.getCrewInfo().countAll(LoadableType.CREW_SET),
				"Humans: " + visitorCalculateCargoInfo.getCrewInfo().count(LoadableType.HUMAN),
				"Purple Alien: " + ((visitorCalculateCargoInfo.getCrewInfo().count(LoadableType.PURPLE_ALIEN) > 0)
						? "Yes" : "No"),
				"Brown Alien: " + ((visitorCalculateCargoInfo.getCrewInfo().count(LoadableType.BROWN_ALIEN) > 0)
						? "Yes" : "No"),
		}), AnchorPoint.TOP_LEFT, AnchorPoint.TOP_LEFT, 3, 2);
		info = info.merge(new CLIFrame(new String[] {
				"Red Goods: " + visitorCalculateCargoInfo.getGoodsInfo().count(LoadableType.RED_GOODS),
				"Yellow Goods: " + visitorCalculateCargoInfo.getGoodsInfo().count(LoadableType.YELLOW_GOODS),
				"Green Goods: " + visitorCalculateCargoInfo.getGoodsInfo().count(LoadableType.GREEN_GOODS),
				"Blue Goods: " + visitorCalculateCargoInfo.getGoodsInfo().count(LoadableType.BLUE_GOODS)
		}), AnchorPoint.TOP_LEFT, AnchorPoint.TOP_LEFT, 8, 2);
		info = info.merge(new CLIFrame(new String[] {
				"Batteries: " + visitorCalculateCargoInfo.getBatteriesInfo().count(LoadableType.BATTERY)
		}), AnchorPoint.TOP_LEFT, AnchorPoint.TOP_LEFT, 13, 2);

		return info;
	}

	public CLIFrame getCLIRepresentation(Set<Coordinates> highlight, String fgColor) {
		int minRow = BoardCoordinates.getFirstCoordinateFromDirection(Direction.NORTH);
		int minCol = BoardCoordinates.getFirstCoordinateFromDirection(Direction.WEST);
		final int tileWidth = 4;
		final int tileHeight = 3;

		CLIFrame tilesRepresentation = new CLIFrame();
		for (Map.Entry<Coordinates, TileSkeleton> entry : board.entrySet()) {
			Coordinates c = entry.getKey();
			tilesRepresentation = tilesRepresentation.merge(entry.getValue().getCLIRepresentation()
							.paintForeground(highlight.contains(c) ? fgColor : ANSI.RESET),
					AnchorPoint.TOP_LEFT, AnchorPoint.TOP_LEFT,
					(c.getRow() - minRow) * tileHeight, (c.getColumn() - minCol) * tileWidth);
		}
		// consider the numbers offset in the empty representation
		tilesRepresentation.applyOffset(tileHeight + 1, tileWidth + 2);

		CLIFrame rep = emptyRepresentation.merge(tilesRepresentation);
		if (filled) {
			// already filled for the first time -> show content
			rep = rep.merge(getInfoCliRepresentation(), Direction.EAST, 5);
		}

		return rep;
	}

	public CLIFrame getCLIRepresentation(List<Set<Coordinates>> highlight, List<String> fgColor) {
		int minRow = BoardCoordinates.getFirstCoordinateFromDirection(Direction.NORTH);
		int minCol = BoardCoordinates.getFirstCoordinateFromDirection(Direction.WEST);
		final int tileWidth = 4;
		final int tileHeight = 3;

		CLIFrame tilesRepresentation = new CLIFrame();
		for (int i = 0; i < highlight.size(); i++) {
			for (Map.Entry<Coordinates, TileSkeleton> entry : board.entrySet()) {
				Coordinates c = entry.getKey();
				if (highlight.get(i).contains(c)) {
					tilesRepresentation = tilesRepresentation
							.merge(entry.getValue().getCLIRepresentation()
											.paintForeground(Util.getModularAt(fgColor, i)),
									AnchorPoint.TOP_LEFT, AnchorPoint.TOP_LEFT,
									(c.getRow() - minRow) * tileHeight,
									(c.getColumn() - minCol) * tileWidth);
				}
			}
		}

		// consider the numbers offset in the empty representation
		tilesRepresentation.applyOffset(tileHeight + 1, tileWidth + 2);

		CLIFrame rep = emptyRepresentation.merge(tilesRepresentation);
		if (filled) {
			// already filled for the first time -> show content
			rep = rep.merge(getInfoCliRepresentation(), Direction.EAST, 5);
		}

		return rep;
	}

	@Override
	public CLIFrame getCLIRepresentation() {
		return getCLIRepresentation(Collections.emptySet(), ANSI.RESET);
	}

	/**
	 * Used to count exposed connectors (e.g. in StarDust Adventure)
	 * @implNote for each tile checks the neighbors and
	 * increments counter for each empty adjacent one if its side is a connector.
	 * If the count has already been calculated since last structural change in the ship,
	 * the previously stored result is returned, avoiding duplicated calculations.
	 * @return the number of exposed connectors
	 */
	public int getExposedConnectorsCount() {
		if (countExposedConnectors != null) {
			return countExposedConnectors;
		}
		// else: calculate and store it
		countExposedConnectors = 0;
		for(Coordinates coord : getOccupiedCoordinates())
		{
			// Count exposed connectors for each tile
			countExposedConnectors += countTileExposedConnectors(coord);
		}
		return countExposedConnectors;
	}

	/**
	 * Counts the number of exposed connects for the tile
	 * @param coord the coords of the tile we are currently counting exposed connectors
	 * @return number of exposed connects for the tile
	 */
	private int countTileExposedConnectors(Coordinates coord) {
		int exposedCount = 0;

		// Check all 4 adjacent positions
		for (Coordinates neighborCoord: coord.getNeighbors()) {
			if (!board.containsKey(neighborCoord)) {
				Direction sideDirectionToCheck = coord.getNeighborDirection(neighborCoord);
				SideType sideToCheck = board.get(coord).getSide(sideDirectionToCheck);
				if (sideToCheck.isConnector()) {
					exposedCount++;
				}
			}
		}

		return exposedCount;
	}

}
