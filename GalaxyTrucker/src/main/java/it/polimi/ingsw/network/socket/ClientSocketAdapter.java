package src.main.java.it.polimi.ingsw.network.socket;

import src.main.java.it.polimi.ingsw.game.Game;
import src.main.java.it.polimi.ingsw.network.IClient;
import src.main.java.it.polimi.ingsw.network.IServer;
import src.main.java.it.polimi.ingsw.network.rmi.RmiServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;
import java.util.UUID;

public class ClientSocketAdapter implements IClient {

	final RmiServer server;
	final BufferedReader input;
	final PrintWriter output;

	public ClientSocketAdapter(RmiServer server, BufferedReader input, PrintWriter output) {
		this.server = server;
		this.input = input;
		this.output = output;
	}

	//HANDLES INCOMING MESSAGES
	public void runVirtualView() throws IOException {
		String line;
		while ((line = input.readLine()) != null) {
			// Trovare un modo più corretto per invocare metodi
			// sul controller in base al messaggio che arriva!!!
			switch (line) {
				case "getGames":
					output.println(server.getGames());
					break;
			}
		}
	}

	@Override
	public IServer getServer() {
		return server;
	}

	@Override
	public void notifyError(String error) {
		output.println(error);
	}
}
