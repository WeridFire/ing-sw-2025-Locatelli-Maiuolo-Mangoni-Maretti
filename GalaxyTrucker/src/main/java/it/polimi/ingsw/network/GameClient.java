package src.main.java.it.polimi.ingsw.network;



import src.main.java.it.polimi.ingsw.network.rmi.RmiClient;
import src.main.java.it.polimi.ingsw.network.rmi.RmiServer;
import src.main.java.it.polimi.ingsw.network.socket.SocketClient;

import java.io.*;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.Scanner;
import java.util.UUID;

public class GameClient implements IClient{

	private boolean useRMI;
	private RmiClient rmiClient = null;
	private SocketClient socketClient = null;

	public GameClient(boolean useRMI, String host, Integer port) throws IOException, NotBoundException {
		this.useRMI = useRMI;
		if(useRMI){
			final String serverName = "GalaxyTruckerServer";
			Registry registry = LocateRegistry.getRegistry(host, port);
			IServer server = (IServer) registry.lookup(serverName);
			rmiClient = new RmiClient(server, this);
		}else{
			Socket serverSocket = new Socket(host, port);
			InputStreamReader socketRx = new InputStreamReader(serverSocket.getInputStream());
			OutputStreamWriter socketTx = new OutputStreamWriter(serverSocket.getOutputStream());
			socketClient = new SocketClient(new BufferedReader(socketRx), new BufferedWriter(socketTx), this);
			new Thread(() -> {
				try {
					socketClient.runVirtualServer();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}).start();
		}
		runCli();
	}

	public IClient getClient(){
		if(useRMI){
			return rmiClient;
		}else{
			return socketClient;
		}
	}

	private void runCli() throws RemoteException {
		Scanner scan = new Scanner(System.in);
		while (true) {
			System.out.print("> ");
			int command = scan.nextInt();
			switch(command) {
				case 0:
					System.out.println("Connecting via RMI.");
					rmiClient.getServer().connect(rmiClient);
					break;
				case 1:
					getServer().ping(getClient());
					break;
			}
		}
	}

	@Override
	public IServer getServer() throws RemoteException {
		return getClient().getServer();
	}

	@Override
	public void updateClient(ClientUpdate clientUpdate) {
		System.out.println("Received new client update!");
		if(clientUpdate.getCurrentGame() == null){
			System.out.println("You are currently not in any game.");
		}
		System.out.println("Available games: ");
		clientUpdate.getAvailableGames().forEach((game) -> System.out.println(game.getId()));
		//We probably will need to define some nice printers inside of each model element to display prettily to the CLI
	}

	public static void main(String[] args) throws IOException, NotBoundException {
		new GameClient(false, "0.0.0.0", 1234);
	}
}
