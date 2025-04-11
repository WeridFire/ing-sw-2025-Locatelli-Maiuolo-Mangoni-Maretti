package it.polimi.ingsw.gamePhases;

import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.gamePhases.exceptions.CommandNotAllowedException;
import it.polimi.ingsw.gamePhases.exceptions.TimerIsAlreadyRunningException;
import it.polimi.ingsw.network.exceptions.CantFindClientException;
import it.polimi.ingsw.player.Player;

import java.rmi.RemoteException;
import java.util.UUID;

public class LobbyGamePhase extends PlayableGamePhase{

	/**
	 * Constructs a new PlayableGamePhase.
	 *
	 * @param gameId        The unique identifier of the game.
	 * @param gamePhaseType The type of the game phase.
	 * @param gameData      The game data.
	 */
	public LobbyGamePhase(UUID gameId, GameData gameData) {
		super(gameId, GamePhaseType.LOBBY, gameData);
	}

	@Override
	public void playLoop() throws RemoteException, CantFindClientException, InterruptedException {
		synchronized (gameData.getUnorderedPlayers()){
			while (gameData.getPlayers().size() < gameData.getRequiredPlayers()){
				gameData.getUnorderedPlayers().wait();
			}
		}
	}

	@Override
	public void startTimer(Player p) throws TimerIsAlreadyRunningException, CommandNotAllowedException {

	}
}
