package it.polimi.ingsw.network;

import it.polimi.ingsw.game.Game;
import it.polimi.ingsw.network.exceptions.AlreadyRunningServerException;
import it.polimi.ingsw.network.messages.ClientUpdate;
import it.polimi.ingsw.network.rmi.RmiServer;
import it.polimi.ingsw.network.socket.SocketServer;
import it.polimi.ingsw.player.Player;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

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
	private GameServer(int rmiPort, int socketPort) throws AlreadyRunningServerException {
		final String serverName = "GalaxyTruckerServer";

		// try starting servers (RMI and Socket) to know if ports are allowed
		// RMI
		try {
			rmiServer = new RmiServer();
			IServer stub = (IServer) UnicastRemoteObject.exportObject(rmiServer, 0);
			Registry registry = LocateRegistry.createRegistry(rmiPort);
			registry.rebind(serverName, stub);
			System.out.println("RMI server bound on port " + rmiPort + " with name " + serverName + ".");
		} catch (ExportException e) {
			String errorMessage = "RMI server can't be exported: failure in binding on port "
					+ rmiPort + " with name " + serverName +
					".\nThat's probably because an other instance of the Server is already running.";
			System.err.println(errorMessage);
			throw new AlreadyRunningServerException(errorMessage);
		} catch (Exception e) { // Catching all exceptions to see what's going wrong
			e.printStackTrace();
		}
		// Socket
		try {
			ServerSocket listenSocket = new ServerSocket(socketPort);
			socketServer = new SocketServer(listenSocket);
			System.out.println("Socket server bound on port " + socketPort + ".");
		} catch (BindException e) {
			String errorMessage = "Socket server can't bind on port " + socketPort + ".";
			System.err.println(errorMessage);
			throw new AlreadyRunningServerException(errorMessage);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// NOTE: RMIServer is already "running" since the non-blocking function rebind,
		// however it needs to be initialized
		executor.submit(() -> rmiServer.init());
		// run the ServerSocket on a separate thread
		executor.submit(() -> {
			try {
				socketServer.run();
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

	public static boolean isRunning() {
		return instance != null;
	}

	public static GameServer getInstance() {
		try {
			start();
		} catch (AlreadyRunningServerException e) {
			// no problem
		}
		return instance;
	}

	public static void start() throws AlreadyRunningServerException {
		if (isRunning()) throw new AlreadyRunningServerException("Server is already running.");
		instance = new GameServer(1111, 1234);
	}

	public static void main(String[] args) {
        try {
            start();
        } catch (AlreadyRunningServerException e) {
            e.printStackTrace();
        }
    }

	public void broadcastUpdate(Game game) throws RemoteException {
		for (Player player: game.getGameData().getPlayers(Player::isConnected)){
			IClient client = clients.get(player.getConnectionUUID());
			if (client != null){
				client.updateClient(new ClientUpdate(player.getConnectionUUID()));
			}
		}
	}

	/**
	 * like broadcastUpdate but it specifies which clients should and which should NOT refresh their view
	 */
	public void broadcastUpdateRefreshOnly(Game game, Set<Player> playersToRefreshView) throws RemoteException {
		for (Player player: game.getGameData().getPlayers()){
			IClient client = clients.get(player.getConnectionUUID());
			if (client != null){
				client.updateClient(new ClientUpdate(player.getConnectionUUID(), playersToRefreshView.contains(player)));
			}
		}
	}


}
