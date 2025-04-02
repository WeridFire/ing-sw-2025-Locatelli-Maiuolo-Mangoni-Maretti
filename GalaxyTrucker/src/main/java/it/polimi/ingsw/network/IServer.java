package it.polimi.ingsw.network;

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

	// Assemble Phase
	void startTimer(IClient client) throws RemoteException;
	void drawTile(IClient client) throws RemoteException;
}
