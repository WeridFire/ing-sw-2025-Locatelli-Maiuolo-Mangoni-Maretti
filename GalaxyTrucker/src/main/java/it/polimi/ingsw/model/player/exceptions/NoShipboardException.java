package it.polimi.ingsw.model.player.exceptions;

public class NoShipboardException extends Exception {
    public NoShipboardException(String message) {
        super(message);
    }
    public NoShipboardException() {
      this("You don't have a shipboard.");
    }
}
