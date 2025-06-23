package it.polimi.ingsw.model.shipboard.exceptions;

import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.util.Coordinates;

public class OutOfBuildingAreaException extends Exception {
    public OutOfBuildingAreaException(String message) {
        super(message);
    }

    public OutOfBuildingAreaException(GameLevel level, Coordinates coordinates) {
        super("Invalid coordinates " + coordinates + " in shipboard for level " + level);
    }
}
