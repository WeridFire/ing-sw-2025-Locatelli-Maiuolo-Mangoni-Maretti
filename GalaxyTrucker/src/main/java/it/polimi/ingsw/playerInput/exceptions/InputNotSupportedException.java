package it.polimi.ingsw.playerInput.exceptions;

import it.polimi.ingsw.playerInput.PIRType;

public class InputNotSupportedException extends Exception {
	public InputNotSupportedException(PIRType supportedType) {
		super("Incompatible action with input of type " + supportedType);
	}
}
