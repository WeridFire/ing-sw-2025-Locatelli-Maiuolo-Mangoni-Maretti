package it.polimi.ingsw.game.exceptions;

import it.polimi.ingsw.player.Player;

public class PlayerNotInGameException extends Exception {
    public PlayerNotInGameException(Player p) {
        this(p.getUsername());
    }

    public PlayerNotInGameException(String username) {
        super("Could not find player " + username);
    }
}
