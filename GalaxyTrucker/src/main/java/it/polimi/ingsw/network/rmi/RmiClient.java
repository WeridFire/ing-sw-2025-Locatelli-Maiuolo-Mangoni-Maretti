package it.polimi.ingsw.network.rmi;

import it.polimi.ingsw.GamesHandler;
import it.polimi.ingsw.network.GameServer;
import it.polimi.ingsw.network.messages.ClientUpdate;
import it.polimi.ingsw.network.GameClient;
import it.polimi.ingsw.network.IClient;
import it.polimi.ingsw.network.IServer;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RmiClient extends UnicastRemoteObject implements IClient {

	private IServer server;
	private GameClient gameClient;

	/**
	 * The RMI Client.
	 * Allows the server to access it and make calls on it, by exposing methods such as updateClient.
	 * @param server A reference to the RMI (proxy) server.
	 * @param gameClient A reference to the generic game client.
	 */
	public RmiClient(IServer server, GameClient gameClient) throws RemoteException {
		init(server, gameClient);
	}

	/**
	 * An empty RMI Client: needs to be initialized. Useful to avoid circular dependence from GameClient.
	 * Allows the server to access it and make calls on it, by exposing methods such as updateClient.
	 */
	public RmiClient() throws RemoteException { }

	/**
	 * Initialization method to call right after creating an empty instance of RmiClient,
	 * to avoid GameClient circular dependence.
	 * @see #RmiClient(IServer, GameClient)
	 */
	public void init(IServer server, GameClient gameClient) {
		this.server = server;
		this.gameClient = gameClient;
	}

	@Override
	public IServer getServer() {
		return server;
	}

	/**
	 * Sends an update to the client. The client will process this update it received.
	 * @param clientUpdate
	 */
	@Override
	public void updateClient(ClientUpdate clientUpdate) throws RemoteException {
		gameClient.updateClient(clientUpdate);
	}

	@Override
	public void pingClient() throws RemoteException {
		return;
	}

}
