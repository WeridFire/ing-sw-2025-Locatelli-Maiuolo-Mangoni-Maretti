package src.main.java.it.polimi.ingsw.network.socket;

import src.main.java.it.polimi.ingsw.network.ClientUpdate;
import src.main.java.it.polimi.ingsw.network.GameClient;
import src.main.java.it.polimi.ingsw.network.IClient;
import src.main.java.it.polimi.ingsw.network.IServer;

import java.io.BufferedReader;
import java.io.BufferedWriter;

public class SocketClient implements IClient {
	final BufferedReader input;
	final IServer server;
	final GameClient gameClient;

	public SocketClient(BufferedReader input, BufferedWriter output, GameClient gameClient) {
		this.input = input;
		this.server = new ServerSocketAdapter(output);
		this.gameClient = gameClient;
	}

	@Override
	public IServer getServer() {
		return server;
	}

	@Override
	public void updateClient(ClientUpdate clientUpdate) {
		gameClient.setConnectionUUID(clientUpdate.getClientUUID());
	}

}
