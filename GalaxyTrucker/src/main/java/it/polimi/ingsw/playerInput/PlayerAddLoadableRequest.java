package src.main.java.it.polimi.ingsw.playerInput;

import src.main.java.it.polimi.ingsw.player.Player;
import src.main.java.it.polimi.ingsw.shipboard.LoadableType;
import src.main.java.it.polimi.ingsw.util.Coordinates;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PlayerAddLoadableRequest extends PlayerInputRequest {

	private final List<LoadableType> allocatedCargo;

	public PlayerAddLoadableRequest(Player currentPlayer, int cooldown, List<LoadableType> allocatedCargo) {
		super(currentPlayer, cooldown, PlayerTurnType.ADD_CARGO);
		this.allocatedCargo = allocatedCargo;
	}

	@Override
	public Set<Coordinates> getHighlightMask() {
		return currentPlayer.getShipBoard()
				.getVisitorCalculateCargoInfo()
				.getInfoAllContainers()
				.getLocationsWithAllowedContent(new HashSet<>(allocatedCargo))
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
	public void checkForResult() {
		synchronized (lock){
			if(allocatedCargo.){
				lock.notifyAll();;
			}
		}
	}
}
