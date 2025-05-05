package it.polimi.ingsw.network;



import it.polimi.ingsw.network.messages.ClientUpdate;
import it.polimi.ingsw.network.rmi.RmiClient;
import it.polimi.ingsw.network.socket.SocketClient;
import it.polimi.ingsw.view.cli.CLIScreenHandler;

import java.io.*;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class GameClient implements IClient{

	private boolean useRMI;
	private RmiClient rmiClient = null;
	private SocketClient socketClient = null;

	/**
	 * The main game client. Based on the selected options, this client will instance a connection using either RMI or
	 * soket protocol, onto the desired host & port. This gameClient will also handle all the method calls from the
	 * server on the client (basically just the updates).
	 * @param useRMI If to use RMI (false will use socket)
	 * @param host The host IP address
	 * @param port The port on the host
	 * @throws IOException
	 * @throws NotBoundException The RMI server is not present
	 */
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
	}

	/**
	 * @return The low-level client the Game is currently using, either RMI or Socket.
	 */
	public IClient getClient(){
		if(useRMI){
			return rmiClient;
		}else{
			return socketClient;
		}
	}

	@Override
	public IServer getServer() throws RemoteException {
		return getClient().getServer();
	}

	@Override
	public void updateClient(ClientUpdate clientUpdate) {
		CLIScreenHandler.getInstance().setLastUpdate(clientUpdate);
	}

	public static void main(String[] args) throws IOException, NotBoundException {
		GameClient gameClient = new GameClient(Boolean.parseBoolean(args[0]), args[1], Integer.parseInt(args[2]));
		CLIScreenHandler.init(gameClient);
		gameClient.getServer().connect(gameClient.getClient());
		CLIScreenHandler.getInstance().runCli();
	}

	public static void main(GameClient gameClient) throws IOException, NotBoundException {
		CLIScreenHandler.init(gameClient);
		gameClient.getServer().connect(gameClient.getClient());
		CLIScreenHandler.getInstance().runCli();
	}
}
