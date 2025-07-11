package it.polimi.ingsw.network.socket;

import it.polimi.ingsw.network.messages.ClientUpdate;
import it.polimi.ingsw.network.GameServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.UUID;

public class SocketServer {

	private final ServerSocket listenSocket;

	/**
	 * Creates a socket server. The socket server will occupy a thread and constantly listen for incoming connections.
	 * Whenever a connection is detected it instances a new thread that handles it.
	 * @param listenSocket The socket object.
	 * @throws IOException
	 */
	public SocketServer(ServerSocket listenSocket) {
		this.listenSocket = listenSocket;
	}

	/**
	 * The socket server on a separate thread will listen to all incoming connections. For each connection it will
	 * allocate a new thread and an handler for it. Also each connection gets registered on the gameserver with a uuid.
	 * @throws IOException
	 */
	public void run() throws IOException {
		if (listenSocket == null) return;
		Socket clientSocket;
		while ((clientSocket = this.listenSocket.accept()) != null) {
			InputStreamReader socketRx = new InputStreamReader(clientSocket.getInputStream());
			OutputStreamWriter socketTx = new OutputStreamWriter(clientSocket.getOutputStream());

			ClientSocketHandler handler = new ClientSocketHandler(
					new BufferedReader(socketRx),
					new PrintWriter(socketTx),
					clientSocket
			);
			// ensure no problem in socket connection
			handler.handshake();

			UUID connectionUUID = GameServer.getInstance().registerClient(handler);
			System.out.println("Detected a new connection: " + connectionUUID);
			//Confirm connection and send notify client with assigned UUID
			handler.updateClient(new ClientUpdate(connectionUUID));
			new Thread(() -> {
				try {
					handler.runVirtualView();
				} catch(SocketException e) {
					if(e.getMessage().equals("Connection reset")) {
						//In here we don't handle the disconnection. Instead the GameServer thread will. We simply
						//mark the socket as disconnected by closing it.
						try {
							handler.socket.close();
						} catch (IOException ex) {
							System.err.println("Error while closing a socket that disconnected: " + ex.getMessage());
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}).start();
		}
	}
}
