package it.polimi.ingsw.model.gamePhases.exceptions;

public class IllegalStartingPositionIndexException extends Exception {
    public IllegalStartingPositionIndexException(String message) {
        super(message);
    }
    public IllegalStartingPositionIndexException(int posIndex) {
      super("Illegal starting position index: " + posIndex);
    }
}
