package it.polimi.ingsw.model.shipboard.exceptions;

import it.polimi.ingsw.model.shipboard.tiles.TileSkeleton;
import it.polimi.ingsw.util.Coordinates;

public class TileAlreadyPresentException extends Exception {
    public TileAlreadyPresentException(String message) {
        super(message);
    }

    public TileAlreadyPresentException(Coordinates coordinates, TileSkeleton tile) {
        super("Tile " + tile + " already present at coordinates " + coordinates);
    }
}
