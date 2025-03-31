package src.main.java.it.polimi.ingsw.playerInput.exceptions;

import src.main.java.it.polimi.ingsw.player.Player;
import src.main.java.it.polimi.ingsw.playerInput.PlayerTurnType;

public class WrongPlayerTurnException extends Exception {
	public WrongPlayerTurnException(Player realPlayer, Player wrongPlayer, PlayerTurnType turnType) {
		super("The current turn of type " + turnType + " is reserved for player " + realPlayer.getUsername() + " and not" +
				" player " + wrongPlayer.getUsername());
	}
}
