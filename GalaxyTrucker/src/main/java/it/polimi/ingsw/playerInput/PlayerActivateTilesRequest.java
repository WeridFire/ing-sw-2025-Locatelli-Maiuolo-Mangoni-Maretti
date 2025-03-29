package src.main.java.it.polimi.ingsw.playerInput;

import src.main.java.it.polimi.ingsw.enums.PowerType;
import src.main.java.it.polimi.ingsw.player.Player;
import src.main.java.it.polimi.ingsw.shipboard.LoadableType;
import src.main.java.it.polimi.ingsw.util.Coordinates;

import java.util.HashSet;
import java.util.Set;

public class PlayerActivateTilesRequest extends PlayerInputRequest {

	private PowerType powerType;

	public PlayerActivateTilesRequest(Player currentPlayer, int cooldown, PowerType powerType) {
		super(currentPlayer, cooldown, PlayerTurnType.ACTIVATE_TILE);
		if(powerType == PowerType.NONE){
			throw new RuntimeException("Can not create an activate request with power type NONE.");
		}
		this.powerType = powerType;
	}

	@Override
	public Set<Coordinates> getHighlightMask() {
		//TODO: RETURN A SET OF THE TILES WITH A POWER COMPATBILE WITH THE POWER TYPE
		return Set.of();
	}

	@Override
	public void run() throws InterruptedException {
		synchronized (lock){
			lock.wait(getCooldown()* 1000L);
		}
	}

	@Override
	public void checkForResult() {
		//This function gets called by the player when they're done activating stuff.
		lock.notifyAll();
	}
}
