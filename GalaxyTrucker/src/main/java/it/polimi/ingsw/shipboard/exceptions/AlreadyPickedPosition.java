package it.polimi.ingsw.shipboard.exceptions;

public class AlreadyPickedPosition extends Exception {
    public AlreadyPickedPosition(String message) {
        super(message);
    }
    public AlreadyPickedPosition() { this("Attempt to pick an already selected position"); }
}
