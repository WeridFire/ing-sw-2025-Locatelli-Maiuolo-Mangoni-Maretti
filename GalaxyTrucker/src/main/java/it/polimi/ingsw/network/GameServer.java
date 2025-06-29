package it.polimi.ingsw.network;

import it.polimi.ingsw.GamesHandler;
import it.polimi.ingsw.model.game.Game;
import it.polimi.ingsw.network.exceptions.AlreadyRunningServerException;
import it.polimi.ingsw.network.messages.ClientUpdate;
import it.polimi.ingsw.network.rmi.RmiServer;
import it.polimi.ingsw.network.socket.SocketServer;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.util.Default;
import it.polimi.ingsw.util.Logger;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class GameServer{

	private final int socketPort;
	private SocketServer socketServer;
	private final int rmiPort;
	private RmiServer rmiServer;
	private final Map<UUID, IClient> clients = new HashMap<>();

	private final ExecutorService executor = Executors.newFixedThreadPool(3);
	private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

	private static GameServer instance;

	/**
	 * Logic for the server. Starts both the Socket and the RMI servers, on different ports.
	 * @param rmiPort The port for the RMI server.
	 * @param socketPort The port for the SOCKET server.
	 */
	private GameServer(int rmiPort, int socketPort) throws AlreadyRunningServerException {
		this.rmiPort = rmiPort;
		this.socketPort = socketPort;

		// try starting servers (RMI and Socket) to know if ports are allowed
		// RMI
		try {
			rmiServer = new RmiServer();
			IServer stub = (IServer) UnicastRemoteObject.exportObject(rmiServer, 0);
			Registry registry = LocateRegistry.createRegistry(rmiPort);
			registry.rebind(Default.RMI_SERVER_NAME, stub);
			System.out.println("RMI server bound on port " + rmiPort + " with name " + Default.RMI_SERVER_NAME + ".");
		} catch (ExportException e) {
			String errorMessage = "RMI server can't be exported: failure in binding on port "
					+ rmiPort + " with name " + Default.RMI_SERVER_NAME +
					".\nThat's probably because an other instance of the Server is already running.\n" + e.getMessage();
			System.err.println(errorMessage);
			throw new AlreadyRunningServerException(errorMessage);
		} catch (Exception e) { // Catching all exceptions to see what's going wrong
			e.printStackTrace();
			return;
		}
		// Socket
		try {
			ServerSocket listenSocket = new ServerSocket(socketPort);
			socketServer = new SocketServer(listenSocket);
			System.out.println("Socket server bound on port " + socketPort + ".");
		} catch (BindException e) {
			String errorMessage = "Socket server can't bind on port " + socketPort + ".\n" + e.getMessage();
			System.err.println(errorMessage);
			throw new AlreadyRunningServerException(errorMessage);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		// notify server ready
		Logger.info("Server is ready -> " + getCompleteAddress());
	}

	private void init() {
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

		scheduler.scheduleAtFixedRate(() -> {
			try {
				checkConnectedClients();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}, 0, 3, TimeUnit.SECONDS);
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

	public IClient getClient(UUID connectionUUID){
		return this.clients.get(connectionUUID);
	}


	public void checkConnectedClients() {
		List<UUID> markedToRemove = new ArrayList<>();
		HashSet<UUID> gamesToUpdate = new HashSet<>();  // map to avoid duplicated updates

		this.clients.forEach((uuid, client) -> {
			try {
				client.pingClient();
			} catch (RemoteException e) {
				System.out.println("Client " + uuid + " did not respond to ping. Marking as disconnected.");
				Game g = GamesHandler.getInstance().findGameByClientUUID(uuid);
				Player p = GamesHandler.getInstance().getPlayerByConnection(uuid);
				if (g != null && p != null) {
					g.disconnectPlayer(p);
					gamesToUpdate.add(g.getId());
				}
				markedToRemove.add(uuid);
			}
		});

		// Remove clients that were marked for removal
		for (UUID uuid : markedToRemove) {
			this.clients.remove(uuid);
		}
		// update games
		gamesToUpdate.forEach(uuid -> {
            try {
                broadcastUpdateAllRefreshMenuAndGame(uuid);
            } catch (RemoteException e) {
				// in case of an exception no problem, not damaged clients will have to do simple "ping" command
            }
        });
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

	public static String getLocalIPAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "127.0.0.1";
        }
    }

	public int getRMIPort() {
		return rmiPort;
	}

	public int getSocketPort() {
		return socketPort;
	}

	public String getCompleteAddress() {
		return getLocalIPAddress() + " [RMI :" + getRMIPort() + " | Socket :" + getSocketPort() + "]";
	}

	public static GameServer getInstance() {
		try {
			start();
		} catch (AlreadyRunningServerException e) {
			// no problem
		}
		return instance;
	}

	public static void start(int rmiPort, int socketPort) throws AlreadyRunningServerException {
		if (isRunning()) throw new AlreadyRunningServerException("Server is already running.");
		instance = new GameServer(rmiPort, socketPort);
		instance.init();
	}

	public static void start() throws AlreadyRunningServerException {
		start(Default.RMI_PORT, Default.SOCKET_PORT);
	}

	public static void main(String[] args) {
        try {
            start();
        } catch (AlreadyRunningServerException e) {
            e.printStackTrace();
        }
    }

	public static void shutdown() {
		if (instance == null) return;
		try {
			instance.executor.shutdown();
			instance.scheduler.shutdown();
			if (!instance.executor.awaitTermination(5, TimeUnit.SECONDS)) {
				instance.executor.shutdownNow();
			}
			if (!instance.scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
				instance.scheduler.shutdownNow();
			}
		} catch (InterruptedException e) {
			instance.executor.shutdownNow();
			instance.scheduler.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * Broadcasts a {@link ClientUpdate} to all connected clients.
	 * The {@code refreshCondition} predicate determines, for each client,
	 * whether the client should refresh its view upon receiving the update.
	 *
	 * @param refreshCondition a predicate that takes the client's {@link UUID} and {@link IClient} instance,
	 *                         returning {@code true} if the client should refresh its view
	 * @throws RemoteException if a remote communication error occurs during client notification
	 */
	public void broadcastUpdateAllRefreshOnlyIf(BiPredicate<UUID, IClient> refreshCondition) throws RemoteException {
		for (Map.Entry<UUID, IClient> entry : clients.entrySet()) {
			UUID uuid = entry.getKey();
			IClient client = entry.getValue();
			try{
				client.updateClient(new ClientUpdate(uuid, refreshCondition.test(uuid, client)));
			}catch(RemoteException e){
				//The client is no longer connected. The cleanup thread from game server will discover it.
			}
		}
	}

	/**
	 * Broadcasts a {@link ClientUpdate} to all connected clients.
	 * All the clients not in a game nor lobby will refresh their view,
	 * but also all the players connected to the provided game will refresh their view
	 *
	 * @param gameUUID the game in which to update all clients view
	 * @throws RemoteException if a remote communication error occurs during client notification
	 */
	public void broadcastUpdateAllRefreshMenuAndGame(UUID gameUUID) throws RemoteException {
		broadcastUpdateAllRefreshOnlyIf((clientUUID, clientInterface) -> {
			// refresh only the clients in this lobby and those in main menu
			Game playerGame = GamesHandler.getInstance().findGameByClientUUID(clientUUID);
			if (playerGame == null) return true;
			else return playerGame.getId().equals(gameUUID);
		});
	}

	/**
	 * Broadcasts a {@link ClientUpdate} to all connected players in the given game.
	 * This notifies them of an update without explicitly requesting a view refresh:
	 * all the clients will receive the update triggering a view refresh.
	 *
	 * @param game the game whose connected players will be updated
	 * @throws RemoteException if a remote communication error occurs during client notification
	 */
	public void broadcastUpdate(Game game) throws RemoteException {
		for (Player player: game.getGameData().getPlayers(Player::isConnected)){
			IClient client = clients.get(player.getConnectionUUID());
			if (client != null){
				try{
					client.updateClient(new ClientUpdate(player.getConnectionUUID()));
				}catch(RemoteException e){
					//The client is no longer connected. The cleanup thread from game server will discover it.
				}
			}
		}
	}

	/**
	 * Broadcasts a {@link ClientUpdate} to all connected players in the given game,
	 * determining which clients should refresh their view using the provided filter predicate.
	 * <p>
	 * Only clients corresponding to players that satisfy the predicate will trigger a view refresh.
	 *
	 * @param game the game whose players will be updated
	 * @param filter a predicate determining which players' clients should refresh
	 * @throws RemoteException if a remote communication error occurs during client notification
	 */
	public void broadcastUpdateRefreshOnlyIf(Game game, Predicate<Player> filter) throws RemoteException {
		for (Player player: game.getGameData().getPlayers(Player::isConnected)){
			IClient client = clients.get(player.getConnectionUUID());
			if (client != null){
				try{

					client.updateClient(new ClientUpdate(player.getConnectionUUID(), filter.test(player)));
				}catch(RemoteException e){
					//The client is no longer connected. The cleanup thread from game server will discover it.
				}
			}
		}
	}

	/**
	 * Broadcasts a {@link ClientUpdate} to all connected players in the given game,
	 * specifying explicitly which clients should refresh their views.
	 * <p>
	 * Clients not included in {@code playersToRefreshView} will receive the update without triggering a view refresh.
	 *
	 * @param game the game whose players will be updated
	 * @param playersToRefreshView the subset of players whose clients should refresh their view
	 * @throws RemoteException if a remote communication error occurs during client notification
	 */
	public void broadcastUpdateRefreshOnly(Game game, Set<Player> playersToRefreshView) throws RemoteException {
		broadcastUpdateRefreshOnlyIf(game, playersToRefreshView::contains);
	}

	/**
	 * Broadcasts a {@link ClientUpdate} to all connected players in the given game,
	 * and requests a view refresh for the shipboard's owner and any player currently spectating it.
	 * <p>
	 * A player is considered to be spectating a shipboard if their {@link Player#getSpectating()} matches
	 * the {@link Player#getUsername()} of the specified {@code targetShipboard}.
	 *
	 * @param game the game in which the update should be broadcasted
	 * @param targetShipboard the player whose shipboard is being spectated
	 * @throws RemoteException if a remote communication error occurs during client notification
	 */
	public void broadcastUpdateShipboardSpectators(Game game, Player targetShipboard) throws RemoteException {
		broadcastUpdateRefreshOnlyIf(game, p ->
				p.equals(targetShipboard) || p.getSpectating().equals(targetShipboard.getUsername()));
	}


}
