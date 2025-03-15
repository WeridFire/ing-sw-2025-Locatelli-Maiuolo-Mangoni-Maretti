package src.main.java.it.polimi.ingsw.shipboard.exceptions;

import src.main.java.it.polimi.ingsw.enums.GameLevel;
import src.main.java.it.polimi.ingsw.util.Coordinates;

public class OutOfBuildingAreaException extends Exception {
    public OutOfBuildingAreaException(String message) {
        super(message);
    }

    public OutOfBuildingAreaException(GameLevel level, Coordinates coordinates) {
        super("Invalid coordinates " + coordinates + " in shipboard for level " + level);
    }
}
