package src.main.java.it.polimi.ingsw.network.socket;

import src.main.java.it.polimi.ingsw.network.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;

public class ClientSocketToRMIAdapter implements IClient {

	final GameServer gameServer;
	final BufferedReader input;
	final PrintWriter output;

	public ClientSocketToRMIAdapter(GameServer gameServer, BufferedReader input, PrintWriter output) {
		this.gameServer = gameServer;
		this.input = input;
		this.output = output;
	}

	//HANDLES MESSAGES FROM CLIENT TO SERVER
	public void runVirtualView() throws IOException {
		String line;
		while ((line = input.readLine()) != null) {
			// TODO: add serialization and deserialization!
			switch (line) {
				case "getGames":
					//we delegate the RmiServer to handle the request. We pass ourself, so that if the RMI
					//needs to update the client, it will use us to update via socket.
					getServer().requestUpdate(this);
					break;
				case "joinGame":
					getServer().joinGame(gameServer.getUUIDbyConnection(this), UUID.fromString("should be parsed"), "should be parsed");
					break;
			}
		}
	}

	@Override
	public IServer getServer() {
		return gameServer.getRmiServer();
	}

	@Override
	public void updateClient(ClientUpdate clientUpdate) {
		//TODO: IMPLEMENT PROPER SERIALIZATION
		output.println(clientUpdate.toString());
	}

}
