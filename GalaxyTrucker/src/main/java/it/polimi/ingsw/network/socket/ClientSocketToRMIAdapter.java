package src.main.java.it.polimi.ingsw.network.socket;

import src.main.java.it.polimi.ingsw.network.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

public class ClientSocketToRMIAdapter implements IClient {

	final GameServer gameServer;
	final BufferedReader input;
	final PrintWriter output;

	/**
	 * This adapter handles all socket connections on the server. It handles both INCOMING MESSAGES (parsing client-made
	 * messages on the server, and then running them) and OUTCOMING MESSAGES (serializing outgoing messages and sending
	 * them to the client).
	 * @param input The input buffer, for the incoming channel
	 * @param output The output buffer, for the outgoing channel.
	 */
	public ClientSocketToRMIAdapter(BufferedReader input, PrintWriter output) {
		this.gameServer = GameServer.getInstance();
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
			SocketMessage message = null;
			try{
				byte[] decodedMessage = Base64.getDecoder().decode(line);
				message = SocketMessage.deserialize(decodedMessage);
			}catch(ClassNotFoundException | IOException e){
				System.err.println("Could not deserialize message: " + line);
				e.printStackTrace();
			}
			if(message != null){
				System.out.println("Received new command: " + message.getType());
				switch (message.getType()) {
					case PING -> getServer().ping(this);
					case JOIN_GAME -> getServer()
									.joinGame(
											this,
											(UUID) message.getArgs().getFirst(),
											(String) message.getArgs().get(1));
					case CREATE_GAME -> getServer().createGame(
											this,
											(String) message.getArgs().getFirst());
				}
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
		String encodedMessage = Base64.getEncoder().encodeToString(clientUpdate.serialize());
		output.println(encodedMessage);
		output.flush();
	}
}
