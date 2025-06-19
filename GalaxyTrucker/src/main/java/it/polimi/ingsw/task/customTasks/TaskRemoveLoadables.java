package it.polimi.ingsw.task.customTasks;

import it.polimi.ingsw.enums.AnchorPoint;
import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.shipboard.LoadableType;
import it.polimi.ingsw.shipboard.tiles.ContainerTile;
import it.polimi.ingsw.shipboard.tiles.exceptions.NotEnoughItemsException;
import it.polimi.ingsw.shipboard.tiles.exceptions.UnsupportedLoadableItemException;
import it.polimi.ingsw.task.Task;
import it.polimi.ingsw.task.TaskType;
import it.polimi.ingsw.task.exceptions.TileNotAvailableException;
import it.polimi.ingsw.task.exceptions.WrongPlayerTurnException;
import it.polimi.ingsw.util.Coordinates;
import it.polimi.ingsw.view.cli.ANSI;
import it.polimi.ingsw.view.cli.CLIFrame;
import it.polimi.ingsw.view.cli.CLIScreen;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class TaskRemoveLoadables extends Task {

	private final int targetAmount;
	private final int amountToRemove;
	private final Set<LoadableType> allowedCargo;
	private final Consumer<Player> onFinish;


	public TaskRemoveLoadables(Player currentPlayer, int cooldown, Set<LoadableType> allowedCargo,
							   int amount, Consumer<Player> onFinish) {
		super(currentPlayer.getUsername(), cooldown, TaskType.REMOVE_CARGO);
		this.allowedCargo = allowedCargo;

		this.targetAmount = Math.max(currentPlayer
				.getShipBoard()
				.getVisitorCalculateCargoInfo()
				.getInfoAllContainers().countAll(allowedCargo) - amount, 0);
		this.amountToRemove = amount;

		this.onFinish = onFinish;
	}



	@Override
	protected void finish() {
		if(getCargoAmount() > targetAmount){
			getPlayer().getShipBoard().loseBestGoods(getAmountToRemove());
		}
		this.onFinish.accept(getPlayer());
	}

	@Override
	public boolean checkCondition() {
		if(getEpochTimestamp() > getExpiration()){
			return true;
		}
		return getCargoAmount() <= targetAmount;
	}

	/**
	 * TASK INTERACTION
	 */

	/**
	 * Function used to remove loadables from the player's shipboard, based on the player request. The function will
	 * check that the current turn and game state allow for this.
	 * @param player The player that requested the action
	 * @param cargoToRemove The list of tiles and the cargo to remove
	 * @throws NotEnoughItemsException The tile requested does not have enough items.
	 * @throws UnsupportedLoadableItemException The tile requested does not support the requested loadable.
	 */
	public void removeLoadables(Player player, Map<Coordinates, List<LoadableType>> cargoToRemove) throws NotEnoughItemsException, UnsupportedLoadableItemException, TileNotAvailableException, WrongPlayerTurnException {
		checkForTurn(player.getUsername());
		for(Coordinates c : cargoToRemove.keySet()){
			checkForTileMask(c);
		}
		for(Map.Entry<Coordinates, List<LoadableType>> entry : cargoToRemove.entrySet()){
			if(!allowedCargo.containsAll(entry.getValue())){
				throw new UnsupportedLoadableItemException(new HashSet<>(entry.getValue()), allowedCargo);
			}
			ContainerTile containerTile = getPlayer().getShipBoard()
					.getVisitorCalculateCargoInfo()
					.getInfoAllContainers()
					.getLocations()
					.get(entry.getKey());
			if(containerTile == null){
				//shouldn't really happen tbh
				throw new TileNotAvailableException(entry.getKey(), getTaskType());
			}
			for(LoadableType loadable : entry.getValue()){
				containerTile.removeItems(loadable, 1);
			}
		}
	}



	/**
	 * TASK-RELATED DATA GETTERS
	 **/

	/**
	 * Returns the amount of loadable items to remove from the shipboard.
	 * @return The amount of loadable items to remove from the shipboard.
	 */
	public int getAmountToRemove() {
		return amountToRemove;
	}

	public Set<LoadableType> getAllowedCargo() {
		return allowedCargo;
	}

	private Map<Coordinates, ContainerTile> getContainerTiles() {
		return getPlayer().getShipBoard()
				.getVisitorCalculateCargoInfo()
				.getInfoAllContainers()
				.getLocationsWithLoadedItems(allowedCargo, 1);
	}


	@Override
	public Set<Coordinates> getHighlightMask() {
		return getContainerTiles()
				.keySet();
	}


	public int getCargoAmount(){
		return getPlayer().getShipBoard()
				.getVisitorCalculateCargoInfo()
				.getInfoAllContainers()
				.countAll(allowedCargo);
	}


	/**
	 * CLI RELATED CODE
	 * **/


	@Override
	public CLIFrame getCLIRepresentation() {

		CLIFrame frame = new CLIFrame(ANSI.BACKGROUND_RED + ANSI.WHITE + " REMOVE CARGO FROM CONTAINERS " + ANSI.RESET)
				.merge(new CLIFrame(""), Direction.SOUTH);

		frame = frame.merge(
				new CLIFrame(" Remove any of these Cargo Types: "),
				Direction.SOUTH, 1
		);

		for (LoadableType type : getAllowedCargo()) {
			frame = frame.merge(
					new CLIFrame( "  - " + type.getUnicodeColoredString() + "[" + type.toString() +"]"),
					Direction.SOUTH, 0
			);
		}

		frame = frame.merge(new CLIFrame(""), Direction.SOUTH, 1);
		frame = frame.merge(
				new CLIFrame(ANSI.CYAN + " Containers with Removable Cargo: " + ANSI.RESET),
				Direction.SOUTH, 0
		);

		Map<Coordinates, ContainerTile> containers = getContainerTiles();

		if (containers.isEmpty()) {
			frame = frame.merge(
					new CLIFrame(ANSI.RED + " No containers currently match the allowed cargo for removal." + ANSI.RESET),
					Direction.SOUTH, 0
			);
		} else {
			for (Map.Entry<Coordinates, ContainerTile> entry : containers.entrySet()) {
				Coordinates coords = entry.getKey();
				ContainerTile tile = entry.getValue();

				String containerInfo = String.format(" (%d, %d): ", coords.getColumn(), coords.getRow()) + tile.getName();
				frame = frame.merge(
						new CLIFrame(containerInfo),
						Direction.SOUTH, 0
				);
			}
		}

		frame = frame.merge(new CLIFrame(""), Direction.SOUTH, 2);
		frame = frame.merge(
				new CLIFrame(ANSI.GREEN + " Commands:" + ANSI.RESET),
				Direction.SOUTH, 0
		);
		frame = frame.merge(
				new CLIFrame(" >remove (x, y) <LoadableType> <amount>"),
				Direction.SOUTH, 0
		);
		frame = frame.merge(
				new CLIFrame(" >confirm"),
				Direction.SOUTH, 0
		);

		frame = frame.merge(new CLIFrame(""), Direction.SOUTH, 0);
		frame = frame.merge(
				new CLIFrame(ANSI.WHITE + "You have " + ANSI.YELLOW + getDuration() + " seconds" + ANSI.RESET + " to remove the requested cargo."),
				Direction.SOUTH, 0
		);

		int containerRows = Math.max(frame.getRows() + 2, 24);
		int containerColumns = 100;

		CLIFrame screenFrame = CLIScreen.getScreenFrame(containerRows, containerColumns, ANSI.BACKGROUND_BLACK, ANSI.WHITE);

		return screenFrame.merge(frame, AnchorPoint.CENTER, AnchorPoint.CENTER);
	}
}
