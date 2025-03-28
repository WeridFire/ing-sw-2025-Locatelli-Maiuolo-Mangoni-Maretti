package src.main.java.it.polimi.ingsw.playerInput;

import src.main.java.it.polimi.ingsw.player.Player;
import src.main.java.it.polimi.ingsw.util.Coordinates;

import java.util.Set;

public abstract class PlayerInputRequest {

	protected Player currentPlayer;
	private int cooldown;

	/**
	 * Abstract object for a PlayerInput request. The server will instance a new thread and wait for the player to
	 * perform a specific action (view implementations). It will wait for a cooldown, and if the player hasn't taken
	 * action yet it will take action for it.
	 * @param currentPlayer The player the game waits for
	 * @param cooldown The cooldown duration.
	 */
	public PlayerInputRequest(Player currentPlayer, int cooldown){
		this.currentPlayer = currentPlayer;
		this.cooldown = cooldown;
	}

	/**
	 * Returns the highlight mask of the current request for the player. The mask may be used by the client
	 * to highlight the positions the user should look at to fulfill the request.
	 * @return The mask with coordinates the player must look for to fulfill the request
	 */
	//The mask has to be calculated upon
	//* function call, as it may vary during the execution of the turn. For example we can't precalculate the mask of
	//* the request of removing N astronauts from the ship: if an astronaut is removed from a tile that no longer holds
	//* any astronauts, the mask will need to be changed. This is why we calculate it live.
	public abstract Set<Coordinates> getHighlightMask();


	protected void sleep() throws InterruptedException {
		Thread.sleep(cooldown);
	}

	public abstract void run() throws InterruptedException;

}
