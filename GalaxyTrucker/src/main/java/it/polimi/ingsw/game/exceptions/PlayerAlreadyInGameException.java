package src.main.java.it.polimi.ingsw.game.exceptions;

public class PlayerAlreadyInGameException extends RuntimeException {
    public PlayerAlreadyInGameException(String message) {
        super(message);
    }
}
