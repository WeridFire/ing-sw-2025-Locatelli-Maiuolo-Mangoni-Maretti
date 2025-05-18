package it.polimi.ingsw.game.exceptions;

import it.polimi.ingsw.shipboard.tiles.MainCabinTile;

public class ColorAlreadyInUseException extends Exception {
    public ColorAlreadyInUseException(String message) {
        super(message);
    }
    public ColorAlreadyInUseException(MainCabinTile.Color color) {
        this("The color " + color + " is already in use in this game.");
    }
}
