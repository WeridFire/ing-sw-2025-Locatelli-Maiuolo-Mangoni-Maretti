package src.main.java.it.polimi.ingsw.client.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class PlayerSocketHandler {

	final BufferedReader input;
	final PrintWriter output;
	final SocketServer server;

	public PlayerSocketHandler(SocketServer server, BufferedReader input, PrintWriter output){
		this.input = input;
		this.output = output;
		this.server = server;
	}

	public void reportError(String details) {
		this.output.println("\n[ERROR] - ");
		this.output.println(details);
		this.output.flush();
	}

	public void runVirtualView() throws IOException {
		String line;
		while ((line = input.readLine()) != null) {
			server.getRmiJunction().processSocket(new SocketMessage("PARSE IN HERE",
					List.of("MESSAGES")), this);
		}
	}

}
