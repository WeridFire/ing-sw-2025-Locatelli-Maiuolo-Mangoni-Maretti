package it.polimi.ingsw.network.socket;

import it.polimi.ingsw.network.messages.ClientUpdate;
import it.polimi.ingsw.network.GameClient;
import it.polimi.ingsw.network.IClient;
import it.polimi.ingsw.network.IServer;
import it.polimi.ingsw.util.Default;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.rmi.RemoteException;
import java.util.Base64;

public class SocketClient implements IClient {
	private BufferedReader input;
	private IServer server;
	private GameClient gameClient;

	/**
	 * The Socket Client.
	 * Accept messages from the server and parse them. Will then forward these parsed messages to the GameClient.
	 * @param input The input stream, from where the server will communicate.
	 * @param output The output stream, used to create the ServerSocketHandler.
	 * @param gameClient The game client.
	 */
	public SocketClient(BufferedReader input, BufferedWriter output, GameClient gameClient) {
		init(input, output, gameClient);
	}

	/**
	 * An empty Socket Client: needs to be initialized. Useful to avoid circular dependence from GameClient.
	 * Accept messages from the server and parse them. Will then forward these parsed messages to the GameClient.
	 */
	public SocketClient() throws RemoteException { }

	/**
	 * Initialization method to call right after creating an empty instance of SocketClient,
	 * to avoid GameClient circular dependence.
	 * @see #SocketClient(BufferedReader, BufferedWriter, GameClient) 
	 */
	public void init(BufferedReader input, BufferedWriter output, GameClient gameClient) {
		this.input = input;
		this.server = new ServerSocketHandler(output);
		this.gameClient = gameClient;
	}


	/**
	 * Blocking function that waits initial handshake from the server to ensure the connection begun correctly.
	 * @throws IOException timeout reached, or other incompatibility errors
	 */
	public void handshake(Socket socket) throws IOException {
		try {
			socket.setSoTimeout(Default.SOCKET_HANDSHAKE_TIMEOUT_MS);
			String line = input.readLine();  // wait server handshake
			if (line == null) {
				throw new IOException("Server closed connection");
			}
			if (!line.equals(Default.SOCKET_HANDSHAKE_MESSAGE)) {
				throw new IOException("Client is not compatible with Server");
			}
		} catch (SocketTimeoutException e) {
			throw new IOException("Timed out waiting for server handshake", e);
		} finally {
			socket.setSoTimeout(0);  // reset no timeout
		}
	}

	/**
	 * Blocking function that constantly reads input from the server, if present, and then parses it and forwards
	 * it to the GameClient.
	 * @throws IOException error deserializing messages.
	 */
	public void runVirtualServer() throws IOException {
		String line;
		try {
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
		} catch (SocketException e) {
			gameClient.getView().showError(e.getMessage(),
					"The server abandoned you in deep space\n Please exit and connect to a new server instance");
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

	@Override
	public void pingClient() throws RemoteException {

	}

}
