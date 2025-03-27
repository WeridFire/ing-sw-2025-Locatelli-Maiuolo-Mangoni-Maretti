package src.main.java.it.polimi.ingsw.network.rmi;

import src.main.java.it.polimi.ingsw.GamesHandler;
import src.main.java.it.polimi.ingsw.network.ClientUpdate;
import src.main.java.it.polimi.ingsw.network.GameServer;
import src.main.java.it.polimi.ingsw.network.IClient;
import src.main.java.it.polimi.ingsw.network.IServer;

import java.util.UUID;

public class RmiServer implements IServer {

	final GamesHandler gamesHandler;
	final GameServer gamesServer;

	public RmiServer(GameServer gameServer) {
		this.gamesHandler = GamesHandler.getInstance();
		this.gamesServer = gameServer;
	}

	@Override
	public void connect(IClient client) {
		UUID clientUUID = gamesServer.registerClient(client);
		//Confirm connection and notify the assigned UUID.
		client.updateClient(new ClientUpdate(clientUUID));
	}

	@Override
	public void requestUpdate(IClient client) {
		client.updateClient(new ClientUpdate(gamesServer.getUUIDbyConnection(client)));
	}

	@Override
	public void joinGame(UUID connectionUUID, UUID gameId, String username) {
		gamesHandler.joinGame(username, gameId, connectionUUID);
		//after the user has been added into the game, we can notify the
	}
}
