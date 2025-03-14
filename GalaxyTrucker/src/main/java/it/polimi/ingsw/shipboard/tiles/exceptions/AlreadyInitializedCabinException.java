package src.main.java.it.polimi.ingsw.shipboard.tiles.exceptions;

public class AlreadyInitializedCabinException extends RuntimeException {
    public AlreadyInitializedCabinException(String message) {
        super("AlreadyInitializedCabinException: " + message);
    }
    public AlreadyInitializedCabinException() {
        super("AlreadyInitializedCabinException");
    }
}
