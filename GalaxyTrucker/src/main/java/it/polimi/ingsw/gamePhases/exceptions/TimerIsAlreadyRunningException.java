package it.polimi.ingsw.gamePhases.exceptions;

public class TimerIsAlreadyRunningException extends RuntimeException {
    public TimerIsAlreadyRunningException(String message) {
        super(message);
    }
}
