package it.polimi.ingsw.model.shipboard.exceptions;

import it.polimi.ingsw.util.Coordinates;

public class NoTileFoundException extends Exception {
    public NoTileFoundException(String message) {
        super(message);
    }

    public NoTileFoundException(Coordinates coordinates) {
        super("No tile found at coordinates " + coordinates);
    }
}
