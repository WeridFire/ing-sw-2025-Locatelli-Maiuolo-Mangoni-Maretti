package src.main.java.it.polimi.ingsw.shipboard.tiles.exceptions;

public class AlreadyInitializedCabinException extends Exception {
    public AlreadyInitializedCabinException(String message) {
        super(message);
    }
    public AlreadyInitializedCabinException() {
        this("Attempt to fill a cabin already initialized");
    }
}
