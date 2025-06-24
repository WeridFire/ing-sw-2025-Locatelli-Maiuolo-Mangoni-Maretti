package it.polimi.ingsw.model.game.exceptions;

import it.polimi.ingsw.model.player.Player;

public class PlayerNotInGameException extends Exception {
    public PlayerNotInGameException(Player p) {
        this(p.getUsername());
    }

    public PlayerNotInGameException(String username) {
        super("Could not find player " + username);
    }
}
