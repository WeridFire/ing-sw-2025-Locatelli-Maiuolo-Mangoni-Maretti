package it.polimi.ingsw.model.playerInput.PIRs;

import it.polimi.ingsw.enums.AnchorPoint;
import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.model.playerInput.PIRType;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.playerInput.exceptions.TileNotAvailableException;
import it.polimi.ingsw.model.playerInput.exceptions.WrongPlayerTurnException;
import it.polimi.ingsw.model.shipboard.LoadableType;
import it.polimi.ingsw.model.shipboard.tiles.ContainerTile;
import it.polimi.ingsw.model.shipboard.tiles.exceptions.NotEnoughItemsException;
import it.polimi.ingsw.model.shipboard.tiles.exceptions.UnsupportedLoadableItemException;
import it.polimi.ingsw.util.Coordinates;
import it.polimi.ingsw.view.cli.ANSI;
import it.polimi.ingsw.view.cli.CLIFrame;
import it.polimi.ingsw.view.cli.CLIScreen;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PIRRemoveLoadables extends PIR {

	private int amountToRemove;
	private final Set<LoadableType> allowedCargo;

	private boolean resendRequest;
	public boolean shouldResendRequest() {
		return resendRequest;
	}

	public PIRRemoveLoadables(Player currentPlayer, int cooldown, Set<LoadableType> allowedCargo, int amount) {
		super(currentPlayer, cooldown, PIRType.REMOVE_CARGO);
		this.allowedCargo = allowedCargo;
		this.amountToRemove = amount;

		resendRequest = false;
	}

	public PIRRemoveLoadables(PIRRemoveLoadables toCopy) {
		this(toCopy.currentPlayer, toCopy.getCooldown(), toCopy.getAllowedCargo(), toCopy.amountToRemove);
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
			if (!resendRequest && amountToRemove > 0) {
				System.out.println("Removing default loadables...");
				if(this.allowedCargo.containsAll(LoadableType.CREW_SET)){  // crew
					currentPlayer.getShipBoard().loseCrew(amountToRemove);
				} else if (this.allowedCargo.contains(LoadableType.BATTERY) && this.allowedCargo.size() == 1) {
					// only batteries
					currentPlayer.getShipBoard().loseBatteries(amountToRemove);
				} else {  // goods that are not batteries
					currentPlayer.getShipBoard().loseBestGoods(amountToRemove);
				}
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

		Map<Coordinates, ContainerTile> containerTiles = currentPlayer.getShipBoard()
				.getVisitorCalculateCargoInfo()
				.getInfoAllContainers()
				.getLocations();

		for(Map.Entry<Coordinates, List<LoadableType>> entry : cargoToRemove.entrySet()){
			if(!allowedCargo.containsAll(entry.getValue())){
				throw new UnsupportedLoadableItemException(new HashSet<>(entry.getValue()), allowedCargo);
			}
			ContainerTile containerTile = containerTiles.get(entry.getKey());
			if(containerTile == null){
				//shouldn't really happen tbh
				throw new TileNotAvailableException(entry.getKey(), getPIRType());
			}
			for(LoadableType loadable : entry.getValue()){
				containerTile.removeItems(loadable, 1);
				amountToRemove--;
			}
		}

		if (!cargoToRemove.isEmpty() && (amountToRemove > 0)) {  // valid interaction and still need interaction
			resendRequest = true;
		}

		endTurn();
	}

	/**
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
		CLIFrame frame = new CLIFrame(ANSI.BACKGROUND_RED + ANSI.WHITE + " REMOVE CARGO FROM CONTAINERS " + ANSI.RESET)
				.merge(new CLIFrame(""), Direction.SOUTH);

		frame = frame.merge(
				new CLIFrame(" Remove any of these Cargo Types: "),
				Direction.SOUTH, 1
		);

		for (LoadableType type : getAllowedCargo()) {
			frame = frame.merge(
					new CLIFrame(type.getUnicodeColoredString() + "[" + type +"]"),
					Direction.SOUTH, 0
			);
		}

		frame = frame.merge(
				new CLIFrame("Remaining to remove: " + ANSI.YELLOW + (getAmountToRemove()) + ANSI.RESET),
				Direction.SOUTH, 1
		);

		frame = frame.merge(
				new CLIFrame(ANSI.CYAN + " Containers with Removable Cargo: " + ANSI.RESET),
				Direction.SOUTH, 1
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

				String containerInfo = coords.toString() + ": " + tile.getName();
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
				new CLIFrame(ANSI.WHITE + "You have " + ANSI.YELLOW + getCooldown() + " seconds" + ANSI.RESET + " to remove the requested cargo."),
				Direction.SOUTH, 0
		);

		int containerRows = Math.max(frame.getRows() + 2, 24);
		int containerColumns = 100;

		CLIFrame screenFrame = CLIScreen.getScreenFrame(containerRows, containerColumns, ANSI.BACKGROUND_BLACK, ANSI.WHITE);

		return screenFrame.merge(frame, AnchorPoint.CENTER, AnchorPoint.CENTER);
	}

}
