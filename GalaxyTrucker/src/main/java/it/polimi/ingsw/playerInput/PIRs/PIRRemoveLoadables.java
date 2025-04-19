package it.polimi.ingsw.playerInput.PIRs;

import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.playerInput.exceptions.TileNotAvailableException;
import it.polimi.ingsw.playerInput.exceptions.WrongPlayerTurnException;
import it.polimi.ingsw.shipboard.LoadableType;
import it.polimi.ingsw.shipboard.tiles.ContainerTile;
import it.polimi.ingsw.shipboard.tiles.exceptions.NotEnoughItemsException;
import it.polimi.ingsw.shipboard.tiles.exceptions.UnsupportedLoadableItemException;
import it.polimi.ingsw.util.Coordinates;
import it.polimi.ingsw.view.cli.ANSI;
import it.polimi.ingsw.view.cli.CLIFrame;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PIRRemoveLoadables extends PIR {

	private int targetAmount;
	private int amountToRemove;
	private final Set<LoadableType> allowedCargo;

	public PIRRemoveLoadables(Player currentPlayer, int cooldown, Set<LoadableType> allowedCargo, int amount) {
		super(currentPlayer, cooldown, it.polimi.ingsw.playerInput.PIRType.REMOVE_CARGO);
		this.allowedCargo = allowedCargo;

		this.targetAmount = currentPlayer
						.getShipBoard()
						.getVisitorCalculateCargoInfo()
						.getInfoAllContainers().countAll(allowedCargo) - amount;
		this.amountToRemove = amount;
		if(this.targetAmount < 0){
			this.targetAmount = 0;
		}
	}

	private Map<Coordinates, ContainerTile> getContainerTiles() {
		return currentPlayer.getShipBoard()
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
		return currentPlayer.getShipBoard()
				.getVisitorCalculateCargoInfo()
				.getInfoAllContainers()
				.countAll(allowedCargo);
	}

	@Override
	public void run() throws InterruptedException {
		synchronized (lock){
			lock.wait(getCooldown()* 1000L);
			if(getCargoAmount() > targetAmount){
				//TODO: remove remaining cargo automatically, as player has not fulfilled the request.
			}
		}
	}

	@Override
	void endTurn() {
		synchronized (lock){
			lock.notifyAll();
		}
	}

	/**
	 * Function used to remove loadables from the player's shipboard, based on the player request. The function will
	 * check that the current turn and game state allow for this.
	 * @param player The player that requested the action
	 * @param cargoToRemove The list of tiles and the cargo to remove
	 * @throws WrongPlayerTurnException The player that asked for the action is not the one the turn is for.
	 * @throws TileNotAvailableException The tile requested is not supported for this operation.
	 * @throws NotEnoughItemsException The tile requested does not have enough items.
	 * @throws UnsupportedLoadableItemException The tile requested does not support the requested loadable.
	 */
	@Override
	public void removeLoadables(Player player, Map<Coordinates, List<LoadableType>> cargoToRemove) throws WrongPlayerTurnException, TileNotAvailableException, NotEnoughItemsException, UnsupportedLoadableItemException {
		checkForTurn(player);
		for(Coordinates c : cargoToRemove.keySet()){
			checkForTileMask(c);
		}
		for(Map.Entry<Coordinates, List<LoadableType>> entry : cargoToRemove.entrySet()){
			if(!allowedCargo.containsAll(entry.getValue())){
				throw new UnsupportedLoadableItemException(new HashSet<>(entry.getValue()), allowedCargo);
			}
			ContainerTile containerTile = currentPlayer.getShipBoard()
											.getVisitorCalculateCargoInfo()
											.getInfoAllContainers()
											.getLocations()
											.get(entry.getKey());
			if(containerTile == null){
				//shouldn't really happen tbh
				throw new TileNotAvailableException(entry.getKey(), getPIRType());
			}
			for(LoadableType loadable : entry.getValue()){
				containerTile.removeItems(loadable, 1);
			}
		}
		endTurn();
	}

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

	@Override
	public CLIFrame getCLIRepresentation() {

		// Frame Header: Purpose of the PIR
		CLIFrame root = new CLIFrame(ANSI.BACKGROUND_RED + ANSI.WHITE + " REMOVE CARGO FROM CONTAINERS " + ANSI.RESET)
				.merge(new CLIFrame(""), Direction.SOUTH);

		// Display allowed cargo types
		root = root.merge(
				new CLIFrame(" Remove any of these Cargo Types: "),
				Direction.SOUTH, 1
		);

		for (LoadableType type : getAllowedCargo()) {
			root = root.merge(
					new CLIFrame("  - " + type.getRequiredCapacity()),
					Direction.SOUTH, 1
			);
		}

		// Display available container tiles for removal
		root = root.merge(new CLIFrame(""), Direction.SOUTH, 1);
		root = root.merge(
				new CLIFrame(ANSI.CYAN + " Containers with Removable Cargo: " + ANSI.RESET),
				Direction.SOUTH, 1
		);

		Map<Coordinates, ContainerTile> containers = getContainerTiles();

		if (containers.isEmpty()) {
			root = root.merge(
					new CLIFrame(ANSI.RED + " No containers currently match the allowed cargo for removal." + ANSI.RESET),
					Direction.SOUTH, 1
			);
		} else {
			for (Map.Entry<Coordinates, ContainerTile> entry : containers.entrySet()) {
				Coordinates coords = entry.getKey();
				ContainerTile tile = entry.getValue();

				String containerInfo = String.format(" (%d, %d): ", coords.getColumn(), coords.getRow()) + tile.getName();
				root = root.merge(
						new CLIFrame(containerInfo),
						Direction.SOUTH, 1
				);
			}
		}

		// Display commands footer
		root = root.merge(new CLIFrame(""), Direction.SOUTH, 2);
		root = root.merge(
				new CLIFrame(ANSI.GREEN + " Commands:" + ANSI.RESET),
				Direction.SOUTH, 1
		);
		root = root.merge(
				new CLIFrame(" >remove (x, y) <LoadableType> <amount>"),
				Direction.SOUTH, 1
		);
		root = root.merge(
				new CLIFrame(" >confirm"),
				Direction.SOUTH, 1
		);

		return root;
	}

}
