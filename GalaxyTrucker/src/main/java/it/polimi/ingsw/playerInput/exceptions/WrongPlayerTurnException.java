package it.polimi.ingsw.playerInput.exceptions;

import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.playerInput.PIRType;

public class WrongPlayerTurnException extends Exception {
	public WrongPlayerTurnException(Player realPlayer, Player wrongPlayer, PIRType turnType) {
		super("The current turn of type " + turnType + " is reserved for player " + realPlayer.getUsername() + " and not" +
				" player " + wrongPlayer.getUsername());
	}

	public WrongPlayerTurnException(Player wrongPlayer) {
		super("There is no active turn for the player " + wrongPlayer.getUsername() + ".");
	}
}
