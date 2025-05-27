package it.polimi.ingsw.task.exceptions;

import it.polimi.ingsw.task.Task;

public class AnotherTaskAlreadyActiveException extends RuntimeException {

	public AnotherTaskAlreadyActiveException(Task activeTask) {
		super("Another task already active. Task type: " + activeTask.getTaskType() );
	}
}
