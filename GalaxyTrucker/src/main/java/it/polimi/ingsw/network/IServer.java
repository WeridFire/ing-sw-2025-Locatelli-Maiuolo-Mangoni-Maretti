package src.main.java.it.polimi.ingsw.network;

import src.main.java.it.polimi.ingsw.shipboard.LoadableType;
import src.main.java.it.polimi.ingsw.util.Coordinates;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface IServer extends Remote {

	void connect(IClient client) throws RemoteException;
	void requestUpdate(IClient client) throws RemoteException;
	void joinGame(IClient client, UUID gameId, String username) throws RemoteException;
	void quitGame(IClient client) throws RemoteException;
	void drawComponent(IClient client) throws RemoteException;
	void ping(IClient client) throws RemoteException;
	void activateTiles(IClient client, Set<Coordinates> tilesToActivate) throws RemoteException;
	void allocateLoadable(IClient client, LoadableType loadable, Coordinates location) throws RemoteException;
	void forceEndTurn(IClient client) throws RemoteException;
}
