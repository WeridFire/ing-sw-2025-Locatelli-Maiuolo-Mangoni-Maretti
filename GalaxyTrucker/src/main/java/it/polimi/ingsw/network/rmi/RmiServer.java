package src.main.java.it.polimi.ingsw.network.rmi;

import src.main.java.it.polimi.ingsw.GamesHandler;
import src.main.java.it.polimi.ingsw.network.ClientUpdate;
import src.main.java.it.polimi.ingsw.network.GameServer;
import src.main.java.it.polimi.ingsw.network.IClient;
import src.main.java.it.polimi.ingsw.network.IServer;

import java.util.UUID;

public class RmiServer implements IServer {

	final GamesHandler gamesHandler;
	final GameServer gameServer;

	/**
	 * Creates a RMI server. All the methods call on this will affect the actual model. This is the junction between
	 * normal RMI and SOCKET connections.
	 * @param gameServer a reference to the generic gameServer.
	 */
	public RmiServer(GameServer gameServer) {
		this.gamesHandler = GamesHandler.getInstance();
		this.gameServer = gameServer;
	}

	@Override
	public void connect(IClient client) {
		UUID clientUUID = gameServer.registerClient(client);
		//Confirm connection and notify the assigned UUID.
		client.updateClient(new ClientUpdate(clientUUID));
	}

	@Override
	public void requestUpdate(IClient client) {
		UUID connectionUUID = gameServer.getUUIDbyConnection(client);
		client.updateClient(new ClientUpdate(connectionUUID));
	}

	@Override
	public void joinGame(IClient client, UUID gameId, String username) {
		UUID connectionUUID = gameServer.getUUIDbyConnection(client);
		gamesHandler.joinGame(username, gameId, connectionUUID);
		//after the user has been added into the game, we can notify the client with the new information.
		client.updateClient(new ClientUpdate(connectionUUID));
	}
}
