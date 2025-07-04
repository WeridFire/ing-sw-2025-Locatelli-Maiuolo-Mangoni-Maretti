package it.polimi.ingsw.model.shipboard.tiles.exceptions;

public class AlreadyInitializedCabinException extends Exception {
    public AlreadyInitializedCabinException(String message) {
        super(message);
    }
    public AlreadyInitializedCabinException() {
        this("Attempt to fill a cabin already initialized");
    }
}
