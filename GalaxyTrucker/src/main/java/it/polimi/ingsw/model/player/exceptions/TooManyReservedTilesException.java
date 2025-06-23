package it.polimi.ingsw.model.player.exceptions;

public class TooManyReservedTilesException extends Exception {
    public TooManyReservedTilesException() {
        super("You already have the maximum number of reserved tiles.");
    }
}
