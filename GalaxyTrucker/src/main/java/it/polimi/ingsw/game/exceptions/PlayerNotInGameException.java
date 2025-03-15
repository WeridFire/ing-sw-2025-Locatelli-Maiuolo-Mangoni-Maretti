package src.main.java.it.polimi.ingsw.game.exceptions;

public class PlayerNotInGameException extends RuntimeException {
    public PlayerNotInGameException(String message) {
        super(message);
    }
}
