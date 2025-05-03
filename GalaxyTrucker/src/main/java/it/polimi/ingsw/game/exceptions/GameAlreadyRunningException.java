package it.polimi.ingsw.game.exceptions;

import java.util.UUID;

public class GameAlreadyRunningException extends Exception{

	public GameAlreadyRunningException(UUID gameId) {
		super("The GamesHandler detected a game with the same UUID already running. Cannot resume the game.");
	}
}
