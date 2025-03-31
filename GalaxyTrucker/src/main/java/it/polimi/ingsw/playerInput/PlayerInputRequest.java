package src.main.java.it.polimi.ingsw.playerInput;

import src.main.java.it.polimi.ingsw.player.Player;
import src.main.java.it.polimi.ingsw.playerInput.exceptions.InputNotSupportedException;
import src.main.java.it.polimi.ingsw.playerInput.exceptions.TileNotAvailableException;
import src.main.java.it.polimi.ingsw.playerInput.exceptions.WrongPlayerTurnException;
import src.main.java.it.polimi.ingsw.shipboard.LoadableType;
import src.main.java.it.polimi.ingsw.shipboard.tiles.exceptions.NotEnoughItemsException;
import src.main.java.it.polimi.ingsw.shipboard.tiles.exceptions.UnsupportedLoadableItemException;
import src.main.java.it.polimi.ingsw.util.Coordinates;

import java.util.*;

public abstract class PlayerInputRequest {

	protected Player currentPlayer;
	private final int cooldown;
	protected final Object lock = new Object();
	private final PlayerTurnType playerTurnType;

	/**
	 * Abstract object for a PlayerInput request. The server will instance a new thread and wait for the player to
	 * perform a specific action (view implementations). It will wait for a cooldown, and if the player hasn't taken
	 * action yet it will take action for it.
	 * @param currentPlayer The player the game waits for
	 * @param cooldown The cooldown duration.
	 */
	public PlayerInputRequest(Player currentPlayer, int cooldown, PlayerTurnType playerTurnType){
		this.currentPlayer = currentPlayer;
		this.cooldown = cooldown;
		this.playerTurnType = playerTurnType;
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


	public abstract void run() throws InterruptedException;

	/**
	 *
	 * @return the cooldown of the turn (the max time it can take for the player to fulfill the request)
	 */
	public int getCooldown() {
		return cooldown;
	}

	/**
	 * @return the current player of the turn.
	 */
	public Player getCurrentPlayer() {
		return currentPlayer;
	}

	/**
	 * Calling this function will check for the result of the input request. If the request is fulfilled, the
	 * turn will end and move to the next. If not fulfilled, it will keep waiting.
	 */
	public abstract void checkForResult();

	/**
	 * This is used both for the client and the controller to understand what type of action to allow to the player.
	 * When the player takes an action, the controller will check that the action will match the turn type, and
	 * that the targeted coordinate for the action is contained in the coordinate mask.
	 * @return
	 */
	public PlayerTurnType getPlayerTurnType() {
		return playerTurnType;
	}

	protected void checkForTileMask(Coordinates coordinate) throws TileNotAvailableException {
		if(!getHighlightMask().contains(coordinate)){
			throw new TileNotAvailableException(coordinate, playerTurnType);
		}
	}

	protected void checkForTurn(Player player) throws WrongPlayerTurnException {
		if(player != currentPlayer){
			throw new WrongPlayerTurnException(currentPlayer, player, playerTurnType);
		}
	}

	/**
	 * Activates a set of tiles if there are enough available batteries to power them.
	 *
	 * @param coordinates a set of tile coordinates to activate
	 * @throws NotEnoughItemsException if there are not enough available batteries to activate the tiles
	 */
	public void activateTiles(Player player, Set<Coordinates> coordinates) throws WrongPlayerTurnException, InputNotSupportedException, NotEnoughItemsException, TileNotAvailableException {
		throw new InputNotSupportedException(playerTurnType);
	}

	public void removeLoadables(Player player, Map<Coordinates, List<LoadableType>> cargoToRemove) throws InputNotSupportedException, WrongPlayerTurnException, TileNotAvailableException, NotEnoughItemsException, UnsupportedLoadableItemException {
		throw new InputNotSupportedException(playerTurnType);
	}



}
