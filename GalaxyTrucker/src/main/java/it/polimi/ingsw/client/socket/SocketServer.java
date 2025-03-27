package src.main.java.it.polimi.ingsw.client.socket;

import src.main.java.it.polimi.ingsw.GamesHandler;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketServer {

	final Map<UUID, PlayerSocketHandler> clients = new HashMap<>();
	final GamesHandler gamesHandler;
	final ServerSocket listenSocket;
	private final ExecutorService executor = Executors.newCachedThreadPool();
	private final RmiJunction rmiJunction;

	public SocketServer(ServerSocket listenSocket) throws IOException {
		this.gamesHandler = GamesHandler.getInstance();
		this.listenSocket = listenSocket;
		this.rmiJunction = new RmiJunction();
	}

	public void run() throws IOException {
		//Maybe we should make run private, and where this is called pass in the executor the whole class for it to be
		//run automatically?
		Socket clientSocket = null;
		while ((clientSocket = this.listenSocket.accept()) != null) {
			InputStreamReader socketRx = new InputStreamReader(clientSocket.getInputStream());
			OutputStreamWriter socketTx = new OutputStreamWriter(clientSocket.getOutputStream());

			PlayerSocketHandler handler = new PlayerSocketHandler(this, new BufferedReader(socketRx),
																	new PrintWriter(socketTx));

			synchronized (this.clients) {
				clients.put(UUID.randomUUID(), handler);
			}

			executor.submit(() -> {
				try {
					handler.runVirtualView();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
		}
	}

	public RmiJunction getRmiJunction() {
		return rmiJunction;
	}
}
