package src.main.java.it.polimi.ingsw.network;

import src.main.java.it.polimi.ingsw.network.rmi.RmiServer;
import src.main.java.it.polimi.ingsw.network.socket.SocketServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameServer{

	private SocketServer socketServer;
	private RmiServer rmiServer;
	private final Map<UUID, IClient> clients = new HashMap<>();
	private final ExecutorService executor = Executors.newFixedThreadPool(2);

	public GameServer(int rmiPort, int socketPort) {
		//Start the RMI server on a separate thread.
		executor.submit(() -> {
			final String serverName = "GalaxyTruckerServer";
			rmiServer = new RmiServer(this);
			try {
				RmiServer stub = (RmiServer) UnicastRemoteObject.exportObject(rmiServer, 0);
				Registry registry = LocateRegistry.createRegistry(rmiPort);
				registry.rebind(serverName, stub);
				System.out.println("RMI server bound.");
			} catch (RemoteException e) {
				throw new RuntimeException(e);
			}
		});


		//Start the ServerSocket on a separate thread,
		executor.submit(() -> {
			int port = socketPort;
			try {
				ServerSocket listenSocket = new ServerSocket(port);
				socketServer = new SocketServer(listenSocket, this);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

	public void registerClient(IClient client) {
		this.clients.put(UUID.randomUUID(), client);
	}

	public RmiServer getRmiServer() {
		return rmiServer;
	}
}
