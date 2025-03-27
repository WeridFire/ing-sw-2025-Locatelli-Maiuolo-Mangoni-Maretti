package src.main.java.it.polimi.ingsw.network.socket;

import src.main.java.it.polimi.ingsw.network.IClient;
import src.main.java.it.polimi.ingsw.network.IServer;
import src.main.java.it.polimi.ingsw.network.rmi.RmiServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;

public class ClientSocketToRMIAdapter implements IClient {

	final RmiServer server;
	final BufferedReader input;
	final PrintWriter output;

	public ClientSocketToRMIAdapter(RmiServer server, BufferedReader input, PrintWriter output) {
		this.server = server;
		this.input = input;
		this.output = output;
	}

	//HANDLES INCOMING MESSAGES
	public void runVirtualView() throws IOException {
		String line;
		while ((line = input.readLine()) != null) {
			// TODO: add serialization and deserialization!
			switch (line) {
				case "getGames":
					server.sendAvailableGamesToClient(this);
					break;
				case "joinGame":
					server.joinGame(UUID.fromString("should be parsed"), "should be parsed");
					break;
			}
		}
	}

	@Override
	public IServer getServer() {
		return null;
		//We can return null, as the server will never call "getServer" on the virtual client. Maybe we can
		//make this nicer with multiple interfaces?
	}

	@Override
	public void notifyError(String error) {
		output.println("[ERROR] " + error);
	}

	@Override
	public void showUpdate(String update) {
		output.println("[UPDATE] " + update);
	}
}
