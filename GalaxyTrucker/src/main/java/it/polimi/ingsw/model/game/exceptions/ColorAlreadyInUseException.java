package it.polimi.ingsw.model.game.exceptions;

import it.polimi.ingsw.model.shipboard.tiles.MainCabinTile;

public class ColorAlreadyInUseException extends Exception {
    public ColorAlreadyInUseException(String message) {
        super(message);
    }
    public ColorAlreadyInUseException(MainCabinTile.Color color) {
        this("The color " + color + " is already in use in this game.");
    }
}
