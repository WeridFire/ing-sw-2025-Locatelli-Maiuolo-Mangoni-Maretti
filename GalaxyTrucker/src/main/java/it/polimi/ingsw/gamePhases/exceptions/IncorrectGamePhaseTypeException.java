package it.polimi.ingsw.gamePhases.exceptions;

import it.polimi.ingsw.enums.GamePhaseType;

public class IncorrectGamePhaseTypeException extends Exception {
    public IncorrectGamePhaseTypeException(GamePhaseType gamePhaseType) {
        super("This action cannot be performed during game phase of type " + gamePhaseType);
    }
}
