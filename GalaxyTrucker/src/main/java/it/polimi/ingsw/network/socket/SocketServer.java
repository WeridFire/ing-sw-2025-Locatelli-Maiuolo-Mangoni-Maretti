package src.main.java.it.polimi.ingsw.network.socket;

import src.main.java.it.polimi.ingsw.network.ClientUpdate;
import src.main.java.it.polimi.ingsw.network.GameServer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;

public class SocketServer {

	final ServerSocket listenSocket;
	final GameServer gameServer;

	public SocketServer(ServerSocket listenSocket, GameServer gameServer) {
		this.listenSocket = listenSocket;
		this.gameServer = gameServer;
	}

	private void run() throws IOException {
		Socket clientSocket = null;
		while ((clientSocket = this.listenSocket.accept()) != null) {
			InputStreamReader socketRx = new InputStreamReader(clientSocket.getInputStream());
			OutputStreamWriter socketTx = new OutputStreamWriter(clientSocket.getOutputStream());

			ClientSocketToRMIAdapter handler = new ClientSocketToRMIAdapter(gameServer,
																	new BufferedReader(socketRx),
																	new PrintWriter(socketTx));
			UUID connectionUUID = gameServer.registerClient(handler);
			//Confirm connection and send notify client with assigned UUID
			handler.updateClient(new ClientUpdate(connectionUUID));
			new Thread(() -> {
				try {
					handler.runVirtualView();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}).start();
		}
	}
}
