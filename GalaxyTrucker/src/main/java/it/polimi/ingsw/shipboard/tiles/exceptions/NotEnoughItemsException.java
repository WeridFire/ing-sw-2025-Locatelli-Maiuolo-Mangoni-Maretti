package src.main.java.it.polimi.ingsw.shipboard.tiles.exceptions;

public class NotEnoughItemsException extends RuntimeException {
    public NotEnoughItemsException(String message) {
        super("NotEnoughItemsException: " + message);
    }
    public NotEnoughItemsException() {
        super("NotEnoughItemsException");
    }
}
