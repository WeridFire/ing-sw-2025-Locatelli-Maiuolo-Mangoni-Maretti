package it.polimi.ingsw.network;

import it.polimi.ingsw.GamesHandler;
import it.polimi.ingsw.game.Game;
import it.polimi.ingsw.network.exceptions.CantFindClientException;
import it.polimi.ingsw.network.messages.ClientUpdate;
import it.polimi.ingsw.network.rmi.RmiServer;
import it.polimi.ingsw.network.socket.SocketServer;
import it.polimi.ingsw.player.Player;

import java.io.IOException;
import java.net.ServerSocket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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
				ServerSocket listenSocket = new ServerSocket(port);
				socketServer = new SocketServer(listenSocket);
				System.out.println("Socket server bound on port " + socketPort + ".");
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

	public IClient getClientByUUID(UUID uuid) {
		return clients.get(uuid);
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

	public void broadcastUpdate(Game game) throws RemoteException {
		for (Player player: game.getGameData().getPlayers()){
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

	public void broadcastUpdate(UUID gameId) throws RemoteException, CantFindClientException {
		broadcastUpdate(GamesHandler.getInstance().getGame(gameId));
	}

	public void broadcastUpdateRefreshOnly(UUID gameId, Set<Player> playersToRefreshView) throws RemoteException,
			CantFindClientException {
		broadcastUpdateRefreshOnly(GamesHandler.getInstance().getGame(gameId), playersToRefreshView);
	}


}
