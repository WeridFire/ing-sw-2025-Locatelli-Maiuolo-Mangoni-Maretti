package src.main.java.it.polimi.ingsw.playerInput;

import src.main.java.it.polimi.ingsw.player.Player;
import src.main.java.it.polimi.ingsw.shipboard.LoadableType;
import src.main.java.it.polimi.ingsw.util.Coordinates;

import java.util.Set;
import java.util.stream.Collectors;

public class PlayerRemoveLoadableRequest extends PlayerInputRequest {

	private LoadableType targetCargo;
	private int targetAmount;

	public PlayerRemoveLoadableRequest(Player currentPlayer, int cooldown, LoadableType targetCargo, int amount) {
		super(currentPlayer, cooldown);
		this.targetCargo = targetCargo;

		this.targetAmount = currentPlayer
						.getShipBoard()
						.getVisitorCalculateCargoInfo()
						.getInfoAllContainers().count(targetCargo) - amount;
		if(this.targetAmount < 0){
			this.targetAmount = 0;
			throw new RuntimeException("Can't ask a player to remove more resources than they have");
		}
	}

	@Override
	public Set<Coordinates> getHighlightMask() {
		return currentPlayer.getShipBoard()
				.getVisitorCalculateCargoInfo()
				.getInfoAllContainers()
				.getLocationsWithLoadedItems(targetCargo, 1)
				.keySet();
	}

	private int getLoadableAmount(){
		return currentPlayer.getShipBoard()
				.getVisitorCalculateCargoInfo()
				.getInfoAllContainers()
				.count(targetCargo);
	}

	@Override
	public void run() throws InterruptedException {
		while(getLoadableAmount() > targetAmount){
			//TODO: implement thread logic to wait
		}
		//TODO: time expired, hard-remove remaining cargo items
	}
}
