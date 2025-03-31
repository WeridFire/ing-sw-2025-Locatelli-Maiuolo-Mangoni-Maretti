package src.main.java.it.polimi.ingsw.playerInput.exceptions;

import src.main.java.it.polimi.ingsw.player.Player;
import src.main.java.it.polimi.ingsw.playerInput.PlayerTurnType;
import src.main.java.it.polimi.ingsw.util.Coordinates;

public class TileNotAvailableException extends Exception {
	public TileNotAvailableException(Coordinates coordinate, PlayerTurnType turnType) {
		super("The tile at coordinates " + coordinate.toString() + " is not supported for the action " + turnType);
	}
}
