package src.main.java.it.polimi.ingsw.client.rmi;

import src.main.java.it.polimi.ingsw.GamesHandler;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class RmiServer implements VirtualServer {

	final Map<UUID, VirtualView> clients = new HashMap<>();

	final BlockingQueue<ServerUpdate> updates = new ArrayBlockingQueue<>(10);

	private void broadcastUpdateThread() throws InterruptedException, RemoteException {
		while (true) {
			ServerUpdate update = updates.take();
			synchronized (this.clients) {
				for(var c : clients.values()) {
					c.showUpdate(update);
				}
			}
		}
	}

	public RmiServer() {
		//idk if this is necessary
		GamesHandler.getInstance();
	}

	public static void main(String[] args) throws RemoteException {
		final String serverName = "GalaxyTruckerServer";

		VirtualServer server = new RmiServer();
		VirtualServer stub = (VirtualServer) UnicastRemoteObject.exportObject(server, 0);
		Registry registry = LocateRegistry.createRegistry(1234);
		registry.rebind(serverName, stub);
		System.out.println("server bound.");
	}

	@Override
	public void connect(VirtualView client) throws RemoteException{
		synchronized (this.clients) {
			this.clients.put(UUID.randomUUID(), client);
		}
	}

	@Override
	public void reset() throws RemoteException {
		System.err.println("reset request received");
		//boolean result = this.controller.reset(); here we reset the GameHandler, assuming the server does a hard stop.
		//then we notify all clients.
		synchronized (this.clients) {
			if (true) { //if (result)
				for (var c : this.clients.values()) {
					c.showUpdate(new ServerUpdate(ServerUpdate.UpdateType.GAME_RESET));
				}
			} else {
				for (var c : this.clients.values()) {
					c.reportError("already reset");
				}
			}
		}
	}

}
