package src.main.java.it.polimi.ingsw.client.rmi;

import src.main.java.it.polimi.ingsw.GamesHandler;
import src.main.java.it.polimi.ingsw.game.Game;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class RmiServer implements VirtualServer {

	final Map<UUID, VirtualView> clients = new HashMap<>();
	final GamesHandler gamesHandler;

	public RmiServer() {
		this.gamesHandler = GamesHandler.getInstance();
	}


	@Override
	public void connect(VirtualView client) throws RemoteException{
		synchronized (this.clients) {
			this.clients.put(UUID.randomUUID(), client);
		}
	}


}
