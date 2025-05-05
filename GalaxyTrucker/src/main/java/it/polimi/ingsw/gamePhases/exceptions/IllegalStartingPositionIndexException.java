package it.polimi.ingsw.gamePhases.exceptions;

public class IllegalStartingPositionIndexException extends Exception {
    public IllegalStartingPositionIndexException(String message) {
        super(message);
    }
    public IllegalStartingPositionIndexException(int posIndex) {
      super("Illegal starting position index: " + posIndex);
    }
}
