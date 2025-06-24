package it.polimi.ingsw.model.shipboard.exceptions;

public class AlreadyEndedAssemblyException extends Exception {
    public AlreadyEndedAssemblyException(String message) {
        super(message);
    }
    public AlreadyEndedAssemblyException() {
        this("Attempt to end assembly multiple times");
    }
}
