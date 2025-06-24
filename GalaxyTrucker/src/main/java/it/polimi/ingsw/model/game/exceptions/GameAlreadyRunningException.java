package it.polimi.ingsw.model.game.exceptions;

import java.util.UUID;

public class GameAlreadyRunningException extends Exception{

	public GameAlreadyRunningException(String message) {
		super(message);
	}

	public GameAlreadyRunningException(UUID gameId) {
		this("The GamesHandler detected a game with the same UUID [" + gameId
				+ "] already running. Cannot resume the game.");
	}
}
