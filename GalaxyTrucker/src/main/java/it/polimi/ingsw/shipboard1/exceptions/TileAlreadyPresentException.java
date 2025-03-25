package src.main.java.it.polimi.ingsw.shipboard1.exceptions;

import src.main.java.it.polimi.ingsw.shipboard1.tiles.Tile;
import src.main.java.it.polimi.ingsw.util.Coordinates;

public class TileAlreadyPresentException extends Exception {
    public TileAlreadyPresentException(String message) {
        super(message);
    }

    public TileAlreadyPresentException(Coordinates coordinates, Tile tile) {
        super("Tile " + tile + " already present at coordinates " + coordinates);
    }
}
