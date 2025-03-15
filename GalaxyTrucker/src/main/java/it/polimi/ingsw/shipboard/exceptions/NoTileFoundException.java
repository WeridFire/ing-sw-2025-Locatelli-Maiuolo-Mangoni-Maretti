package src.main.java.it.polimi.ingsw.shipboard.exceptions;

import src.main.java.it.polimi.ingsw.util.Coordinates;

public class NoTileFoundException extends Exception {
    public NoTileFoundException(String message) {
        super(message);
    }

    public NoTileFoundException(Coordinates coordinates) {
        super("No tile found at coordinates " + coordinates);
    }
}
