package it.polimi.ingsw.model.playerInput.exceptions;

import it.polimi.ingsw.model.playerInput.PIRType;

public class InputNotSupportedException extends Exception {
	public InputNotSupportedException(PIRType supportedType) {
		super("Incompatible action with input of type " + supportedType);
	}
}
