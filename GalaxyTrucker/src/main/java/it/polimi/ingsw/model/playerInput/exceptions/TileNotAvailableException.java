package it.polimi.ingsw.model.playerInput.exceptions;

import it.polimi.ingsw.model.playerInput.PIRType;
import it.polimi.ingsw.util.Coordinates;

public class TileNotAvailableException extends Exception {
	public TileNotAvailableException(Coordinates coordinate, PIRType turnType) {
		super("The tile at coordinates " + coordinate.toString() + " is not supported for the action " + turnType);
	}
}
