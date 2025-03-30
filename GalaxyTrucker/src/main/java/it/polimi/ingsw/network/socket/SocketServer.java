package src.main.java.it.polimi.ingsw.network.socket;

import src.main.java.it.polimi.ingsw.network.ClientUpdate;
import src.main.java.it.polimi.ingsw.network.GameServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;

public class SocketServer {

	final ServerSocket listenSocket;

	/**
	 * Creates a socket server. The socket server will occupy a thread and constantly listen for incoming connections.
	 * Whenever a connection is detected it instances a new thread that handles it.
	 * @param listenSocket The socket object.
	 * @throws IOException
	 */
	public SocketServer(ServerSocket listenSocket) throws IOException {
		this.listenSocket = listenSocket;
		run();
	}

	/**
	 * The socket server on a separate thread will listen to all incoming connections. For each connection it will
	 * allocate a new thread and an handler for it. Also each connection gets registered on the gameserver with a uuid.
	 * @throws IOException
	 */
	private void run() throws IOException {
		Socket clientSocket = null;
		while ((clientSocket = this.listenSocket.accept()) != null) {
			InputStreamReader socketRx = new InputStreamReader(clientSocket.getInputStream());
			OutputStreamWriter socketTx = new OutputStreamWriter(clientSocket.getOutputStream());

			ClientSocketToRMIAdapter handler = new ClientSocketToRMIAdapter(
																	new BufferedReader(socketRx),
																	new PrintWriter(socketTx));
			UUID connectionUUID = GameServer.getInstance().registerClient(handler);
			//Confirm connection and send notify client with assigned UUID
			handler.updateClient(new ClientUpdate(connectionUUID));
			System.out.println("Detected a new connection.");
			new Thread(() -> {
				try {
					handler.runVirtualView();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}).start();
		}
	}
}
