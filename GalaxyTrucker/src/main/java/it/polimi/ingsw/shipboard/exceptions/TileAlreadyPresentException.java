package it.polimi.ingsw.shipboard.exceptions;

import it.polimi.ingsw.shipboard.SideType;
import it.polimi.ingsw.shipboard.tiles.TileSkeleton;
import it.polimi.ingsw.util.Coordinates;

public class TileAlreadyPresentException extends Exception {
    public TileAlreadyPresentException(String message) {
        super(message);
    }

    public TileAlreadyPresentException(Coordinates coordinates, TileSkeleton tile) {
        super("Tile " + tile + " already present at coordinates " + coordinates);
    }
}
