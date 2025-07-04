package it.polimi.ingsw.model.game.exceptions;

import java.util.UUID;

public class GameNotFoundException extends Exception {
    public GameNotFoundException(UUID gameId) {
        super("Could not find a game with UUID " + gameId.toString());
    }
}
