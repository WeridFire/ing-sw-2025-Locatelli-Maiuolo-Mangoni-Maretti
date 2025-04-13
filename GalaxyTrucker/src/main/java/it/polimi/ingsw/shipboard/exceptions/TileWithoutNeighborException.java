package it.polimi.ingsw.shipboard.exceptions;

import it.polimi.ingsw.util.Coordinates;

public class TileWithoutNeighborException extends Exception {
    public TileWithoutNeighborException(String message) {
        super(message);
    }
    public TileWithoutNeighborException(Coordinates coordinates) {
        this("Attempt to place tile at coordinates " + coordinates + ", but there is not neighbor tile there.");
    }
}
