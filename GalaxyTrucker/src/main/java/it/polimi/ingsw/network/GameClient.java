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
import java.util.Scanner;
import java.util.UUID;

public class GameClient implements IClient{

	private boolean useRMI;
	private RmiClient rmiClient = null;
	private SocketClient socketClient = null;
	private UUID connectionUUID;

	public GameClient(boolean useRMI, String host, Integer port) throws IOException, NotBoundException {
		this.useRMI = useRMI;
		if(useRMI){
			final String serverName = "GalaxyTruckerServer";
			Registry registry = LocateRegistry.getRegistry(host, port);
			RmiServer server = (RmiServer) registry.lookup(serverName);
			rmiClient = new RmiClient(server, this);
			rmiClient.getServer().connect(rmiClient);
		}else{
			Socket serverSocket = new Socket(host, port);
			InputStreamReader socketRx = new InputStreamReader(serverSocket.getInputStream());
			OutputStreamWriter socketTx = new OutputStreamWriter(serverSocket.getOutputStream());
			socketClient = new SocketClient(new BufferedReader(socketRx), new BufferedWriter(socketTx), this);
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
					getServer().requestUpdate(getClient());
					break;
				case 1:
					getServer().joinGame(getClient(), UUID.fromString("UUID HERE"), "placeholder");
					break;
			}
		}
	}

	public void setConnectionUUID(UUID connectionUUID) {
		this.connectionUUID = connectionUUID;
	}

	public static void main(String[] args) throws IOException, NotBoundException {
		new GameClient(Boolean.parseBoolean(args[0]), args[1], Integer.parseInt(args[2]));
	}

	@Override
	public IServer getServer() {
		return getClient().getServer();
	}

	@Override
	public void updateClient(ClientUpdate clientUpdate) {
		//Process new update received from the server.
		setConnectionUUID(clientUpdate.getClientUUID());
		System.out.println(clientUpdate);
	}
}
