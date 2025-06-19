package it.polimi.ingsw.task.exceptions;

import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.task.TaskType;

public class WrongPlayerTurnException extends Exception {

	public WrongPlayerTurnException(String realPlayer, String wrongPlayer, TaskType turnType) {
		super("The current turn of type " + turnType + " is reserved for player " + realPlayer + " and not" +
				" player " + wrongPlayer);
	}

	public WrongPlayerTurnException(Player wrongPlayer) {
		super("There is no active turn for the player " + wrongPlayer.getUsername() + ".");
	}
}
