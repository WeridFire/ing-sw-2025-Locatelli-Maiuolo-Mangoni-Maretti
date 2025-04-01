package it.polimi.ingsw.playerInput.exceptions;

import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.playerInput.PlayerTurnType;
import it.polimi.ingsw.util.Coordinates;

public class TileNotAvailableException extends Exception {
	public TileNotAvailableException(Coordinates coordinate, PlayerTurnType turnType) {
		super("The tile at coordinates " + coordinate.toString() + " is not supported for the action " + turnType);
	}
}
