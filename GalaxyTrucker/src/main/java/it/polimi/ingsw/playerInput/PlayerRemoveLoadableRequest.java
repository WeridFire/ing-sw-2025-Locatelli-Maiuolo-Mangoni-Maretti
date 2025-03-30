package src.main.java.it.polimi.ingsw.playerInput;

import src.main.java.it.polimi.ingsw.player.Player;
import src.main.java.it.polimi.ingsw.shipboard.LoadableType;
import src.main.java.it.polimi.ingsw.util.Coordinates;

import java.util.Set;

public class PlayerRemoveLoadableRequest extends PlayerInputRequest {

	private int targetAmount;
	private final Set<LoadableType> allowedCargo;

	public PlayerRemoveLoadableRequest(Player currentPlayer, int cooldown, Set<LoadableType> allowedCargo, int amount) {
		super(currentPlayer, cooldown, PlayerTurnType.REMOVE_CARGO);
		this.allowedCargo = allowedCargo;

		this.targetAmount = currentPlayer
						.getShipBoard()
						.getVisitorCalculateCargoInfo()
						.getInfoAllContainers().countAll(allowedCargo) - amount;
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
	public void checkForResult() {
		synchronized (lock){
			if(getCargoAmount() <= targetAmount){
				lock.notifyAll();
			}
		}
	}
}
