package it.polimi.ingsw.network;

import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.enums.Rotation;
import it.polimi.ingsw.model.player.kpf.KeepPlayerFlyingPredicate;
import it.polimi.ingsw.model.shipboard.LoadableType;
import it.polimi.ingsw.model.shipboard.tiles.MainCabinTile;
import it.polimi.ingsw.util.Coordinates;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface IServer extends Remote {

	void connect(IClient client) throws RemoteException;
	void createGame(IClient client, String username, MainCabinTile.Color desiredColor) throws RemoteException;
	void joinGame(IClient client, UUID gameId, String username, MainCabinTile.Color desiredColor) throws RemoteException;
	void quitGame(IClient client) throws RemoteException;
	void ping(IClient client) throws RemoteException;
	void resumeGame(IClient client, UUID gameId) throws RemoteException;


	// PIR related commands
	void pirActivateTiles(IClient client, Set<Coordinates> tilesToActivate) throws RemoteException;
	void pirAllocateLoadables(IClient client, Map<Coordinates, List<LoadableType>> cargoToAdd) throws RemoteException;
	void pirForceEndTurn(IClient client) throws RemoteException;
	void pirRemoveLoadables(IClient client,  Map<Coordinates, List<LoadableType>> cargoToRemove) throws RemoteException;
	void pirSelectMultipleChoice(IClient client, int selection) throws RemoteException;
	// Lobby phase
	void updateGameSettings(IClient client, GameLevel level, int minPlayers) throws RemoteException;

	// Assemble phase
	void flipHourglass(IClient client) throws RemoteException;
	void drawTile(IClient client) throws RemoteException;
	void discardTile(IClient client) throws RemoteException;
	void reserveTile(IClient client) throws RemoteException;
	void pickTile(IClient client, Integer id) throws RemoteException;
	void placeTile(IClient client, Coordinates coordinates, Rotation rotation) throws RemoteException;
	void finishAssembling(IClient client, Integer preferredPosition) throws RemoteException;
	void showCardGroup(IClient client, Integer id) throws RemoteException;
	void hideCardGroup(IClient client) throws RemoteException;

	// Adventure phase
	void requestEndFlight(IClient client, KeepPlayerFlyingPredicate saveFromEndFlight) throws RemoteException;

	//Assemble and Adventure
	void spectatePlayerShipboard(IClient client, String username) throws RemoteException;

	//Debugging and Dev mode
	void useCheat(IClient client, String cheatName) throws RemoteException;
}
