package it.polimi.ingsw.model.playerInput.PIRs;

import it.polimi.ingsw.enums.AnchorPoint;
import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.model.playerInput.PIRType;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.playerInput.exceptions.TileNotAvailableException;
import it.polimi.ingsw.model.playerInput.exceptions.WrongPlayerTurnException;
import it.polimi.ingsw.model.shipboard.LoadableType;
import it.polimi.ingsw.model.shipboard.tiles.ContainerTile;
import it.polimi.ingsw.model.shipboard.tiles.exceptions.TooMuchLoadException;
import it.polimi.ingsw.model.shipboard.tiles.exceptions.UnsupportedLoadableItemException;
import it.polimi.ingsw.util.Coordinates;
import it.polimi.ingsw.view.cli.ANSI;
import it.polimi.ingsw.view.cli.CLIFrame;
import it.polimi.ingsw.view.cli.CLIScreen;

import java.util.*;

public class PIRAddLoadables extends PIR {

	private final List<LoadableType> floatingLoadables = new ArrayList<>();


	/**
	 * This turn will make the game wait for player to specify where they want to allocate cargo on the shipboard.
	 * The game will stop and wait cooldown for the player to fulfill the action, and at the end, if the action
	 * is not completed, it will "ignore" the remaining items.
	 * @param currentPlayer The player it'll except the action from
	 * @param cooldown The maximimum time the round will last
	 * @param allocatedCargo The list of loadable items to load. May contain duplicates for multiple items
	 */
	public PIRAddLoadables(Player currentPlayer, int cooldown, List<LoadableType> allocatedCargo) {
		super(currentPlayer, cooldown, PIRType.ADD_CARGO);
		floatingLoadables.addAll(allocatedCargo);
	}

	/**
	 * Checks that all the items the player wants to add are actually present in the list of cargo that can be added.
	 * This prevents duplicates or generation of items that the game hasn't awarded.
	 * @param cargoToAdd
	 * @return
	 */
	public void areAllCargoItemsAvailable(Map<Coordinates, List<LoadableType>> cargoToAdd) throws UnsupportedLoadableItemException {
		// Convert floatingLoadables to a HashSet for faster lookups
		Map<LoadableType, Integer> availableCount = new HashMap<>();
		for (LoadableType loadable : getFloatingLoadables()) {
			availableCount.put(loadable, availableCount.getOrDefault(loadable, 0) + 1);
		}
		for (List<LoadableType> loadables : cargoToAdd.values()) {
			for (LoadableType loadable : loadables) {
				if (!availableCount.containsKey(loadable) || availableCount.get(loadable) == 0) {
					throw new UnsupportedLoadableItemException(Set.of(loadable));
				}
				// Reduce the count since we're "taking" one
				availableCount.put(loadable, availableCount.get(loadable) - 1);
			}
		}
	}

	public Map<Coordinates, ContainerTile> getContainerTiles() {
		Set<LoadableType> uniqueLoadables = new HashSet<>(getFloatingLoadables());
		Map<Coordinates, ContainerTile> result = new HashMap<>();
		uniqueLoadables.forEach(lt -> {
			result.putAll(currentPlayer.getShipBoard()
					.getVisitorCalculateCargoInfo()
					.getInfoAllContainers()
					.getLocationsWithAllowedContent(Set.of(lt)));
			;
		});
		return result;
	}

	@Override
	public Set<Coordinates> getHighlightMask() {
		return getContainerTiles()
				.keySet();
	}

	@Override
	public void run() throws InterruptedException {
		synchronized (lock){
			lock.wait(getCooldown()* 1000L);
			//at the end if the cargo is not completely allocated, it gets ignored
			//TODO: handle the case where ignored cargo is passed onto next player?
		}
	}

	@Override
	void endTurn() {
		synchronized (lock){
			lock.notifyAll();
		}
	}

	/**
	 * This method allows the player to load items onto the shipboard. It will do all the due check to make sure the
	 * loading is allowed. If the player loads too many items, the partial list of items (up until the method throwing
	 * error) will still be allocated, and removed from the floatingLoadables list, and the turn will not end. The player
	 * will then be able to finish allocation of the items that are left, or just terminate the turn. If the player
	 * allocates every item (or a subset of every item) correctly, the game will auto-end the turn assuming the player
	 * has finished his choice making.
	 * @param player The player sending the command.
	 * @param cargoToAdd A map of list of items and coordinates of where they need to be placed.
	 * @throws WrongPlayerTurnException The player requesting the command is not who the turn is for.
	 * @throws TileNotAvailableException One of the tiles input by the player is not valid for this action.
	 * @throws UnsupportedLoadableItemException An item being loaded is not supported either by a container, or by the list of cargo that has to be loaded.
	 * @throws TooMuchLoadException A tile is being loaded with too much loadables.
	 */
	@Override
	public void addLoadables(Player player, Map<Coordinates, List<LoadableType>> cargoToAdd) throws WrongPlayerTurnException, TileNotAvailableException, UnsupportedLoadableItemException, TooMuchLoadException {
		checkForTurn(player);
		for(Coordinates c : cargoToAdd.keySet()){
			checkForTileMask(c);
		}
		areAllCargoItemsAvailable(cargoToAdd);

		for(Map.Entry<Coordinates, List<LoadableType>> entry : cargoToAdd.entrySet()){
			ContainerTile container = currentPlayer.getShipBoard()
											.getVisitorCalculateCargoInfo()
											.getInfoAllContainers()
											.getLocations()
											.get(entry.getKey());
			for(LoadableType loadable : entry.getValue()){
				container.loadItems(loadable, 1);
				getFloatingLoadables().remove(loadable);
			}
		}
		endTurn();
	}

	/**
	 * Returns a list of cargo items that yet have to be loaded in the current turn. This list is dynamic, meaning
	 * that if a player decides to load too much items than allowed, the turn will not end, all the "correct" items
	 * will be allocated, and this list will show only the remaining items.
	 * @return
	 */
	public List<LoadableType> getFloatingLoadables() {
		return floatingLoadables;
	}

	@Override
	public CLIFrame getCLIRepresentation() {
		// Main header
		StringBuilder loadablesString = new StringBuilder();
		floatingLoadables.forEach(l -> loadablesString.append(l.getUnicodeColoredString()));

		CLIFrame frame = new CLIFrame(ANSI.BACKGROUND_BLUE + ANSI.WHITE
				+ " PLAYER INPUT REQUEST — ALLOCATE CARGO " + ANSI.RESET)
				.merge(new CLIFrame(new String[] {
						"",
						loadablesString.toString(),
						"",
				}), Direction.SOUTH);

		// Fetch valid container tiles from highlight mask
		Set<Coordinates> targetCoords = getHighlightMask();

		if (targetCoords.isEmpty()) {
			frame = frame.merge(
					new CLIFrame(ANSI.RED + "No available containers for cargo allocation!" + ANSI.RESET),
					Direction.SOUTH, 0
			);
		} else {
			CLIFrame containersFrame = new CLIFrame(ANSI.CYAN + "Available Containers:" + ANSI.RESET);

			Map<Coordinates, ContainerTile> containerTiles = getContainerTiles();

			for (Coordinates coord : targetCoords) {
				ContainerTile container = containerTiles.get(coord);
				if (container != null) {
					String info = ANSI.GREEN + coord+": " + ANSI.RESET
							+ container.getName();
					containersFrame = containersFrame.merge(new CLIFrame(info), Direction.SOUTH, 0);
				}
			}
			frame = frame.merge(containersFrame, Direction.SOUTH, 0);
		}

		// Command section
		frame = frame.merge(new CLIFrame(""), Direction.SOUTH, 0);
		frame = frame.merge(
				new CLIFrame(ANSI.YELLOW + "Commands:" + ANSI.RESET),
				Direction.SOUTH, 0
		);
		frame = frame.merge(
				new CLIFrame(ANSI.WHITE + ">allocate (row,col) <LoadableType> <amount>" + ANSI.RESET),
				Direction.SOUTH, 0
		);
		frame = frame.merge(
				new CLIFrame(ANSI.WHITE + ">confirm" + ANSI.RESET),
				Direction.SOUTH, 0
		);

		// Timeout / remaining time info
		frame = frame.merge(new CLIFrame(""), Direction.SOUTH, 0);
		frame = frame.merge(
				new CLIFrame(ANSI.WHITE + "You have " + ANSI.YELLOW + getCooldown() + " seconds" + ANSI.RESET + " to allocate your cargo."),
				Direction.SOUTH, 0
		);

		// Wrap into a full screen background
		int containerRows = Math.max(frame.getRows() + 2, 24);
		int containerColumns = 100;

		CLIFrame screenFrame = CLIScreen.getScreenFrame(containerRows, containerColumns, ANSI.BACKGROUND_BLACK, ANSI.WHITE);

		// Merge the content centered
		return screenFrame.merge(frame, AnchorPoint.CENTER, AnchorPoint.CENTER);
	}



}
