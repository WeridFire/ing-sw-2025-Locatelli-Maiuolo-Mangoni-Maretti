package it.polimi.ingsw.playerInput.exceptions;

import it.polimi.ingsw.playerInput.PlayerTurnType;

public class InputNotSupportedException extends Exception {
	public InputNotSupportedException(PlayerTurnType supportedType) {
		super("Incompatible action with input of type " + supportedType);
	}
}
