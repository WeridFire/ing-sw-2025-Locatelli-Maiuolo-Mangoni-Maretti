package it.polimi.ingsw.playerInput.exceptions;

import it.polimi.ingsw.playerInput.PIRType;
import it.polimi.ingsw.task.TaskType;

public class InputNotSupportedException extends Exception {
	public InputNotSupportedException(PIRType supportedType) {
		super("Incompatible action with input of type " + supportedType);
	}

	public InputNotSupportedException(TaskType supportedType) {
		super("Incompatible action with input of type " + supportedType);
	}
}
