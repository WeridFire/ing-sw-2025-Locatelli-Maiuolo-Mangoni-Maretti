package src.main.java.it.polimi.ingsw.network.rmi;

import src.main.java.it.polimi.ingsw.network.ClientUpdate;
import src.main.java.it.polimi.ingsw.network.GameClient;
import src.main.java.it.polimi.ingsw.network.IClient;
import src.main.java.it.polimi.ingsw.network.IServer;

import java.rmi.RemoteException;

public class RmiClient implements IClient {

	final IServer server;
	final GameClient gameClient;

	/**
	 * The RMI Client. Allows the server to access it and make calls on it, by exposing methods such as updateClient.
	 * @param server A reference to the RMI (proxy) server.
	 * @param gameClient A reference to the generic game client.
	 * @throws RemoteException
	 */
	public RmiClient(RmiServer server, GameClient gameClient) throws RemoteException {
		this.server = server;
		server.connect(this);
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
	public void updateClient(ClientUpdate clientUpdate) {
		//We delegate the generic gameClient to handle the update.
		gameClient.updateClient(clientUpdate);
	}

}
