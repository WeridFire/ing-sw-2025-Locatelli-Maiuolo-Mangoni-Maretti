package it.polimi.ingsw.model.shipboard.exceptions;

public class UninitializedShipboardException extends Exception {
    public UninitializedShipboardException(String message) {
        super(message);
    }
    public UninitializedShipboardException() {
        this("This shipboard has not been initialized yet.");
    }
}
