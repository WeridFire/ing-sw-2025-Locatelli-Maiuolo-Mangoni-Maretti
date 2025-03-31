package src.main.java.it.polimi.ingsw.network;

import src.main.java.it.polimi.ingsw.network.rmi.RmiServer;
import src.main.java.it.polimi.ingsw.network.socket.SocketServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.rmi.NotBoundException;
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

	private static GameServer instance;

	/**
	 * Logic for the server. Starts both the Socket and the RMI servers, on different ports.
	 * @param rmiPort The port for the RMI server.
	 * @param socketPort The port for the SOCKET server.
	 */
	public GameServer(int rmiPort, int socketPort) {
		final String serverName = "GalaxyTruckerServer";
		//Start the RMI server on a separate thread.
		executor.submit(() -> {
			rmiServer = new RmiServer();
			try {
				rmiServer = new RmiServer();
				IServer stub = (IServer) UnicastRemoteObject.exportObject(rmiServer, 0);
				Registry registry = LocateRegistry.createRegistry(rmiPort);
				registry.rebind(serverName, stub);
				System.out.println("RMI server bound on port " + rmiPort + " with name " + serverName + ".");
			} catch (Exception e) { // Catching all exceptions to see what's going wrong
				e.printStackTrace();
			}
		});

		//Start the ServerSocket on a separate thread,
		executor.submit(() -> {
			int port = socketPort;
			try {
				System.out.println("Socket server bound on port " + socketPort + ".");
				ServerSocket listenSocket = new ServerSocket(port);
				socketServer = new SocketServer(listenSocket);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

	}

	/**
	 * Registers a connection (either RMI or SOCKET) to the database. Assigns an UUID to it.
	 * @param client The client to register and keep track of.
	 * @return The assigned UUID.
	 */
	public UUID registerClient(IClient client) {
		UUID clientUUID = UUID.randomUUID();
		this.clients.put(clientUUID, client);
		return clientUUID;
	}

	/**
	 * A reference to the RMI server, on which server functions can be executed.
	 * @return
	 */
	public RmiServer getRmiServer() {
		return rmiServer;
	}

	/**
	 * Looks in the clients database for the correspondent connection, and returns the UUID.
	 * @param client the client connection
	 * @return The UUID of the connection in the database.
	 */
	public UUID getUUIDbyConnection(IClient client){
		return clients.entrySet()
				.stream()
				.filter(entry -> entry.getValue().equals(client))
				.map(Map.Entry::getKey)
				.findFirst().orElseThrow(() -> new RuntimeException("Could not find an UUID with the given connection."));
	}

	public static GameServer getInstance() {
		if (instance == null) {
			instance = new GameServer(1111, 1234);
		}
		return instance;
	}

	public static void main(String[] args) throws IOException, NotBoundException {
		getInstance();
	}




}
