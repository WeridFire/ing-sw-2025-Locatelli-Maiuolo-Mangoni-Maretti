package src.main.java.it.polimi.ingsw.network.rmi;

import src.main.java.it.polimi.ingsw.network.ClientUpdate;
import src.main.java.it.polimi.ingsw.network.GameClient;
import src.main.java.it.polimi.ingsw.network.IClient;
import src.main.java.it.polimi.ingsw.network.IServer;

import java.rmi.RemoteException;

public class RmiClient implements IClient {

	final IServer server;
	final GameClient gameClient;

	public RmiClient(RmiServer server, GameClient gameClient) throws RemoteException {
		this.server = server;
		server.connect(this);
		this.gameClient = gameClient;
	}

	@Override
	public IServer getServer() {
		return server;
	}

	@Override
	public void updateClient(ClientUpdate clientUpdate) {
		//Process new update received from the server.
		gameClient.setConnectionUUID(clientUpdate.getClientUUID());
		System.out.println(clientUpdate);
	}

}
