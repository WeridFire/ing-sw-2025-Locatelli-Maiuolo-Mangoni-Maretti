package it.polimi.ingsw.network.socket;

import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.network.*;
import it.polimi.ingsw.network.messages.ClientUpdate;
import it.polimi.ingsw.network.messages.SocketMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Base64;
import java.util.UUID;

public class ClientSocketHandler implements IClient {

	final GameServer gameServer;
	final BufferedReader input;
	final PrintWriter output;

	/**
	 * This adapter handles all socket connections on the server. It handles both INCOMING MESSAGES (parsing client-made
	 * messages on the server, and then running them) and OUTCOMING MESSAGES (serializing outgoing messages and sending
	 * them to the client). Messages arriving here are simply parsed and forwarded to the RMI on the corresponding
	 * method call.
	 * @param input The input buffer, for the incoming channel
	 * @param output The output buffer, for the outgoing channel.
	 */
	public ClientSocketHandler(BufferedReader input, PrintWriter output) {
		this.gameServer = GameServer.getInstance();
		this.input = input;
		this.output = output;
	}

	/**
	 * Blocking function to receive and parse messages. Based on the message content, it will call the RMI
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
				System.out.println("Received new command: " + message.getType() + " args: " + message.getArgs());
				try{
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
						case UPDATE_SETTINGS -> getServer().updateGameSettings(
								this, (GameLevel) message.getArgs().getFirst(), (int) message.getArgs().get(1)
						);
					}
				}catch(IllegalArgumentException e){
					System.err.println("ERROR WHILE PARSING MESSAGE! Message:");
					System.err.println("cmd: " + message.getType() + "args: " + message.getArgs());
					System.err.println("Make sure the arguments are passed in the correct order by the client!");
					e.printStackTrace();
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
