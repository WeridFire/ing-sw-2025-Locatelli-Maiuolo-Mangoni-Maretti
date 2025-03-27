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

	/**
	 * This adapter handles all socket connections on the server. It handles both INCOMING MESSAGES (parsing client-made
	 * messages on the server, and then running them) and OUTCOMING MESSAGES (serializing outgoing messages and sending
	 * them to the client).
	 * @param gameServer a reference to the generic gameServer.
	 * @param input The input buffer, for the incoming channel
	 * @param output The output buffer, for the outgoing channel.
	 */
	public ClientSocketToRMIAdapter(GameServer gameServer, BufferedReader input, PrintWriter output) {
		this.gameServer = gameServer;
		this.input = input;
		this.output = output;
	}

	/**
	 * Occupies a thread to constantly receive and parse messages. Based on the message content, it will call the RMI
	 * server to execute as necessary the method. It will also pass a reference to itself to the RMI, so that it will
	 * be able to send messages using sockets by accessing it.
	 * @throws IOException
	 */
	public void runVirtualView() throws IOException {
		String line;
		while ((line = input.readLine()) != null) {
			// TODO: add serialization and deserialization!
			switch (line) {
				case "getGames":
					//TODO: CREATE A NEW THREAD FOR EACH MESSAGE, TO BECOME EXACT SAME AS RMI!
					//we delegate the RmiServer to handle the request. We pass ourself, so that if the RMI
					//needs to update the client, it will use us to update via socket.
					getServer().requestUpdate(this);
					break;
				case "joinGame":
					getServer().joinGame(this, UUID.fromString("should be parsed"), "should be parsed");
					break;
			}
		}
	}

	@Override
	public IServer getServer() {
		//In here we shouldn't worry about this function. It is present only because the class extends IClient, but
		//the server will never actually call getServer() on a ClientSocketToRMIAdapter. If it does, we're fucked :).
		return gameServer.getRmiServer();
	}

	/**
	 * Sends a ClientUpdate over the SOCKET protocol, with serialization.
	 * @param clientUpdate The client update.
	 */
	@Override
	public void updateClient(ClientUpdate clientUpdate) {
		//TODO: IMPLEMENT PROPER SERIALIZATION
		output.println(clientUpdate.toString());
	}

}
