package it.polimi.ingsw.network;

import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.enums.Rotation;
import it.polimi.ingsw.shipboard.LoadableType;
import it.polimi.ingsw.util.Coordinates;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface IServer extends Remote {

	void connect(IClient client) throws RemoteException;
	void createGame(IClient client, String username) throws RemoteException;
	void joinGame(IClient client, UUID gameId, String username) throws RemoteException;
	void quitGame(IClient client) throws RemoteException;
	void ping(IClient client) throws RemoteException;
	void activateTiles(IClient client, Set<Coordinates> tilesToActivate) throws RemoteException;
	void allocateLoadables(IClient client, Map<Coordinates, List<LoadableType>> cargoToAdd) throws RemoteException;
	void forceEndTurn(IClient client) throws RemoteException;

	//Lobby phase
	void updateGameSettings(IClient client, GameLevel level, int minPlayers) throws RemoteException;

	// Assemble Phase
	void flipHourglass(IClient client) throws RemoteException;
	void drawTile(IClient client) throws RemoteException;
	void discardTile(IClient client) throws RemoteException;
	void reserveTile(IClient client) throws RemoteException;
	void pickTile(IClient client, Integer id) throws RemoteException;
	void placeTile(IClient client, Coordinates coordinates, Rotation rotation) throws RemoteException;
	void finishAssembling(IClient client) throws RemoteException;
}
