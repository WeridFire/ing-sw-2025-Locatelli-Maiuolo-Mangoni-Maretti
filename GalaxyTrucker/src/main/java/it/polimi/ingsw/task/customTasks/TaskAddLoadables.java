package it.polimi.ingsw.task.customTasks;

import it.polimi.ingsw.GamesHandler;
import it.polimi.ingsw.enums.AnchorPoint;
import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.shipboard.LoadableType;
import it.polimi.ingsw.shipboard.tiles.ContainerTile;
import it.polimi.ingsw.shipboard.tiles.exceptions.TooMuchLoadException;
import it.polimi.ingsw.shipboard.tiles.exceptions.UnsupportedLoadableItemException;
import it.polimi.ingsw.task.Task;
import it.polimi.ingsw.task.TaskType;
import it.polimi.ingsw.task.exceptions.TileNotAvailableException;
import it.polimi.ingsw.task.exceptions.WrongPlayerTurnException;
import it.polimi.ingsw.util.Coordinates;
import it.polimi.ingsw.view.cli.ANSI;
import it.polimi.ingsw.view.cli.CLIFrame;
import it.polimi.ingsw.view.cli.CLIScreen;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class TaskAddLoadables extends Task {

	private final List<LoadableType> floatingLoadables = new ArrayList<>();
	private final Consumer<Player> onFinish;


	public TaskAddLoadables(String currentPlayer, int cooldown, List<LoadableType> allocatedCargo,
										Consumer<Player> onFinish) {
		super(currentPlayer, cooldown, TaskType.CHOICE);
		this.onFinish = onFinish;
	}

	public List<LoadableType> getFloatingLoadables() {
		return floatingLoadables;
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

	private Map<Coordinates, ContainerTile> getContainerTiles() {
		return getPlayer().getShipBoard()
				.getVisitorCalculateCargoInfo()
				.getInfoAllContainers()
				.getLocationsWithAllowedContent(
						new HashSet<>(getFloatingLoadables())
				);
	}


	public void addLoadables(Player player, Map<Coordinates, List<LoadableType>> cargoToAdd) throws UnsupportedLoadableItemException, TooMuchLoadException, TileNotAvailableException, WrongPlayerTurnException {
		checkForTurn(player.getUsername());
		for(Coordinates c : cargoToAdd.keySet()){
			checkForTileMask(c);
		}
		areAllCargoItemsAvailable(cargoToAdd);
		for(Map.Entry<Coordinates, List<LoadableType>> entry : cargoToAdd.entrySet()){
			ContainerTile container = getPlayer().getShipBoard()
					.getVisitorCalculateCargoInfo()
					.getInfoAllContainers()
					.getLocations()
					.get(entry.getKey());
			for(LoadableType loadable : entry.getValue()){
				container.loadItems(loadable, 1);
				getFloatingLoadables().remove(loadable);
			}
		}
	}

	@Override
	public Set<Coordinates> getHighlightMask() {
		return getContainerTiles()
				.keySet();
	}


	@Override
	public boolean checkCondition() {
		if(getEpochTimestamp() > getExpiration()){
			return true;
		}
		if(floatingLoadables.isEmpty()){
			return true;
		}
		return false;
	}

	@Override
	protected void finish() {
		Player player = getPlayer();
		this.onFinish.accept(player);
	}


	@Override
	public CLIFrame getCLIRepresentation() {
		// Main header
		CLIFrame frame = new CLIFrame(ANSI.BACKGROUND_BLUE + ANSI.WHITE + " PLAYER INPUT REQUEST — ALLOCATE CARGO " + ANSI.RESET)
				.merge(new CLIFrame(""), Direction.SOUTH);

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
					String info = ANSI.GREEN + "(" + coord.getColumn() + ", " + coord.getRow() + "): " + ANSI.RESET
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
				new CLIFrame(ANSI.WHITE + ">allocate (x, y) <LoadableType> <amount>" + ANSI.RESET),
				Direction.SOUTH, 0
		);
		frame = frame.merge(
				new CLIFrame(ANSI.WHITE + ">confirm" + ANSI.RESET),
				Direction.SOUTH, 0
		);

		// Timeout / remaining time info
		frame = frame.merge(new CLIFrame(""), Direction.SOUTH, 0);
		frame = frame.merge(
				new CLIFrame(ANSI.WHITE + "You have " + ANSI.YELLOW + getDuration() + " seconds" + ANSI.RESET + " to allocate your cargo."),
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
