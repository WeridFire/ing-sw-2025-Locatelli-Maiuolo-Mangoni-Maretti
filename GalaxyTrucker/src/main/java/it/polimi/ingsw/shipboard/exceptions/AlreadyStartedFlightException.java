package it.polimi.ingsw.shipboard.exceptions;

public class AlreadyStartedFlightException extends Exception {
    public AlreadyStartedFlightException(String message) {
        super(message);
    }
    public AlreadyStartedFlightException() {
        this("Attempt to start a flight multiple times");
    }
}
