package it.polimi.ingsw.gamePhases;

import it.polimi.ingsw.GamesHandler;
import it.polimi.ingsw.cards.Card;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.controller.cp.exceptions.CommandNotAllowedException;
import it.polimi.ingsw.gamePhases.exceptions.TimerIsAlreadyRunningException;
import it.polimi.ingsw.network.exceptions.CantFindClientException;
import it.polimi.ingsw.player.Player;

import java.rmi.RemoteException;

public class LobbyGamePhase extends PlayableGamePhase{

	/**
	 * Constructs a new PlayableGamePhase.
	 *
	 * @param gameData      The game data.
	 */
	public LobbyGamePhase(GameData gameData) {
		super(GamePhaseType.LOBBY, gameData);
	}

	@Override
	public void playLoop(){

	}

	@Override
	public void startTimer(Player p) throws TimerIsAlreadyRunningException, CommandNotAllowedException {

	}

	@Override
	public void endPhase() {
		//TODO: PROCEED TO ASSEMBLY
	}
}
