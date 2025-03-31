package src.main.java.it.polimi.ingsw.network.socket;

import src.main.java.it.polimi.ingsw.network.ClientUpdate;
import src.main.java.it.polimi.ingsw.network.GameClient;
import src.main.java.it.polimi.ingsw.network.IClient;
import src.main.java.it.polimi.ingsw.network.IServer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Base64;

public class SocketClient implements IClient {
	final BufferedReader input;
	final IServer server;
	final GameClient gameClient;

	/**
	 * Creates a Socket Client, which will accept messages from the server and parse them. Will then forward these
	 * parsed messages to the GameClient.
	 * @param input The input stream, from where the server will communicate.
	 * @param output The output stream, used to create the ServerSocketHandler.
	 * @param gameClient The game client.
	 */
	public SocketClient(BufferedReader input, BufferedWriter output, GameClient gameClient) {
		this.input = input;
		this.server = new ServerSocketHandler(output);
		this.gameClient = gameClient;
	}


	/**
	 * Blocking function that constantly reads input from the server, if present, and then parses it and forwards
	 * it to the GameClient.
	 * @throws IOException error deserializing messages.
	 */
	public void runVirtualServer() throws IOException {
		String line;
		while ((line = input.readLine()) != null) {
			ClientUpdate clientUpdate = null;
			try {
				byte[] serialized = Base64.getDecoder().decode(line);
				clientUpdate = ClientUpdate.deserialize(serialized);
				updateClient(clientUpdate);
			} catch (IOException  | ClassNotFoundException e) {
				System.err.println("Error deserializing message: " + line);
				e.printStackTrace();
			}
		}
	}


	@Override
	public IServer getServer() {
		return server;
	}

	/**
	 * Sends an update to the client via SOCKET, which will process it.
	 * @param clientUpdate The update.
	 */
	@Override
	public void updateClient(ClientUpdate clientUpdate) {
		//Now to be completely symmetric with the server, it would come spontaneous to let the RMI handle this call.
		//Basically we deserialized the function and now we send it to the RMI to process properly. HOWEVER,
		// the RMICLIENT does not exist!! There can only be either the RMI or the SOCKET, not BOTH! So we have to move
		// The implementation of this function in the generic GameClient, and both the SOCKET and RMI will call it!
		gameClient.updateClient(clientUpdate);
	}

}
