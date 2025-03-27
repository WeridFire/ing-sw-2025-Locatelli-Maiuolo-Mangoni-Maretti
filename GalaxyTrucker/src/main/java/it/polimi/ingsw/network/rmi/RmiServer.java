package src.main.java.it.polimi.ingsw.network.rmi;

import src.main.java.it.polimi.ingsw.GamesHandler;
import src.main.java.it.polimi.ingsw.game.Game;
import src.main.java.it.polimi.ingsw.network.GameServer;
import src.main.java.it.polimi.ingsw.network.IClient;
import src.main.java.it.polimi.ingsw.network.IServer;

import java.util.HashSet;
import java.util.Set;
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
		gamesServer.registerClient(client);
	}

	@Override
	public void sendAvailableGamesToClient(IClient client) {
		client.showUpdate((new HashSet<>(gamesHandler.getGames())).toString());
	}

	@Override
	public void joinGame(UUID gameId, String username) {
		gamesHandler.joinGame(username, gameId);
	}
}
