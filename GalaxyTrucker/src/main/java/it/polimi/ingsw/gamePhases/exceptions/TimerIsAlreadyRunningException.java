package it.polimi.ingsw.gamePhases.exceptions;

import it.polimi.ingsw.controller.cp.exceptions.CommandNotAllowedException;

public class TimerIsAlreadyRunningException extends CommandNotAllowedException {
    public TimerIsAlreadyRunningException(String message) {
        super(message);
    }
}
