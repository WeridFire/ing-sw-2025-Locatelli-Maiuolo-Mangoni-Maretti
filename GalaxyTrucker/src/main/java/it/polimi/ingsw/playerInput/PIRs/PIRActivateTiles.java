package it.polimi.ingsw.playerInput.PIRs;

import it.polimi.ingsw.enums.PowerType;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.playerInput.exceptions.TileNotAvailableException;
import it.polimi.ingsw.playerInput.exceptions.WrongPlayerTurnException;
import it.polimi.ingsw.shipboard.LoadableType;
import it.polimi.ingsw.shipboard.tiles.exceptions.NotEnoughItemsException;
import it.polimi.ingsw.util.Coordinates;

import java.util.HashSet;
import java.util.Set;

public class PIRActivateTiles extends PIR {

	private PowerType powerType;
	private final Set<Coordinates> activatedTiles = new HashSet<>();

	public PIRActivateTiles(Player currentPlayer, int cooldown, PowerType powerType) {
		super(currentPlayer, cooldown, it.polimi.ingsw.playerInput.PIRType.ACTIVATE_TILE);
		this.powerType = powerType;
	}

	@Override
	public Set<Coordinates> getHighlightMask() {
		return currentPlayer.getShipBoard()
				.getVisitorCalculatePowers()
				.getInfoPower(powerType)
				.getLocationsToActivate().keySet();
	}

	@Override
	public void run() throws InterruptedException {
		synchronized (lock){
			lock.wait(getCooldown()* 1000L);
		}
	}

	@Override
	void endTurn() {
		//This function gets called by the player when they're done activating stuff.
		lock.notifyAll();
	}

	/**
	 * Calculates the number of available battery units that are not currently used for tile activation.
	 *
	 * @return the number of available batteries
	 */
	private int getAvailableBatteriesAmount() {
		return (currentPlayer.getShipBoard().getVisitorCalculateCargoInfo()
				.getInfoAllContainers()
				.count(LoadableType.BATTERY) - getActivatedTiles().size());
	}

	/**
	 * This input action will allow the player to specify which coordinates they want to activate. It can only
	 * be used for power types of type PowerType.FIRE & PowerType.THRUST. Once the player submits the action, the
	 * turn will auto end. If the player submits a list of coordinates that are not valid, the game will notify an
	 * error but will not end the turn.
	 * @param player The player that sent the command
	 * @param coordinates a set of tile coordinates to activate
	 * @throws WrongPlayerTurnException If the player requesting the command is not who the turn is for.
	 * @throws NotEnoughItemsException If the player does not have enough batteries to activate the tiles.
	 * @throws TileNotAvailableException If the tile is not supported for this action in this turn.
	 */
	public void activateTiles(Player player, Set<Coordinates> coordinates) throws WrongPlayerTurnException, NotEnoughItemsException, TileNotAvailableException {
		checkForTurn(player);
		for(Coordinates c : coordinates){
			checkForTileMask(c);
		}
		if (getAvailableBatteriesAmount() >= coordinates.size()) {
			activatedTiles.addAll(coordinates);
			endTurn();
		} else {
			throw new NotEnoughItemsException("Attempt to activate more tiles than batteries available");
		}
	}

	 Set<Coordinates> getActivatedTiles() {
		return activatedTiles;
	}
}
