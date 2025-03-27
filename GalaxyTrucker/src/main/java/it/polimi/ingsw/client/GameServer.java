package src.main.java.it.polimi.ingsw.client;

import src.main.java.it.polimi.ingsw.client.rmi.RmiServer;
import src.main.java.it.polimi.ingsw.client.rmi.VirtualServer;
import src.main.java.it.polimi.ingsw.client.socket.SocketServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameServer {

	private VirtualServer rmiServer;
	private SocketServer socketServer;
	private final ExecutorService executor = Executors.newFixedThreadPool(2);

	public GameServer(int rmiPort, int socketPort) {
		//Start the RMI server on a separate thread.
		executor.submit(() -> {
			final String serverName = "GalaxyTruckerServer";
			rmiServer = new RmiServer();
			try {
				VirtualServer stub = (RmiServer) UnicastRemoteObject.exportObject(rmiServer, 0);
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
				socketServer = new SocketServer(listenSocket);
				socketServer.run();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}



	public static void main(String[] args) throws RemoteException {
		new GameServer(1234, 8080);
	}
}
