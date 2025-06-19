package it.polimi.ingsw.task.exceptions;

import it.polimi.ingsw.task.TaskType;
import it.polimi.ingsw.util.Coordinates;

public class TileNotAvailableException extends Exception {

	public TileNotAvailableException(Coordinates coordinate, TaskType taskType) {
		super("The tile at coordinates " + coordinate.toString() + " is not supported for the task " + taskType);
	}
}
