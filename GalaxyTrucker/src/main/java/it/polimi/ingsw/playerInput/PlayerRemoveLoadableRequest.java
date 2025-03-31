package src.main.java.it.polimi.ingsw.playerInput;

import src.main.java.it.polimi.ingsw.player.Player;
import src.main.java.it.polimi.ingsw.playerInput.exceptions.TileNotAvailableException;
import src.main.java.it.polimi.ingsw.playerInput.exceptions.WrongPlayerTurnException;
import src.main.java.it.polimi.ingsw.shipboard.LoadableType;
import src.main.java.it.polimi.ingsw.shipboard.tiles.ContainerTile;
import src.main.java.it.polimi.ingsw.shipboard.tiles.exceptions.NotEnoughItemsException;
import src.main.java.it.polimi.ingsw.shipboard.tiles.exceptions.UnsupportedLoadableItemException;
import src.main.java.it.polimi.ingsw.util.Coordinates;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PlayerRemoveLoadableRequest extends PlayerInputRequest {

	private int targetAmount;
	private int amountToRemove;
	private final Set<LoadableType> allowedCargo;

	public PlayerRemoveLoadableRequest(Player currentPlayer, int cooldown, Set<LoadableType> allowedCargo, int amount) {
		super(currentPlayer, cooldown, PlayerTurnType.REMOVE_CARGO);
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

	@Override
	public Set<Coordinates> getHighlightMask() {
		return currentPlayer.getShipBoard()
				.getVisitorCalculateCargoInfo()
				.getInfoAllContainers()
				.getLocationsWithLoadedItems(allowedCargo, 1)
				.keySet();
	}


	private int getCargoAmount(){
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
	public void endTurn() {
		synchronized (lock){
			if(getCargoAmount() <= targetAmount){
				lock.notifyAll();
			}
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
				throw new TileNotAvailableException(entry.getKey(), getPlayerTurnType());
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
}
