package it.polimi.ingsw.model.game.exceptions;

public class PlayerAlreadyInGameException extends Exception {
    public PlayerAlreadyInGameException(String username) {
        super("A player with the username " + username + " already exists");
    }
}
