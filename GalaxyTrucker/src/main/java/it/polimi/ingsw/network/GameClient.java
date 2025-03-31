package src.main.java.it.polimi.ingsw.network;



import src.main.java.it.polimi.ingsw.network.rmi.RmiClient;
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
	private final ClientCLIView cliView;

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
		this.cliView = new ClientCLIView(null);
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

	/**
	 * Runs the CLI on the client, allowing player to input commands to send to the server. These commands will be
	 * executed automatically on the desired client.
	 * @throws RemoteException
	 */
	private void runCli() throws RemoteException {
		Scanner scan = new Scanner(System.in);
		while (true) {
			System.out.print("> ");
			String command = scan.nextLine().trim();  // Reading the full line for commands with arguments

			String[] commandParts = command.split(" ");  // Split the command by spaces

			switch (commandParts[0]) {
				case "ping":
					getServer().ping(getClient());
					break;

				case "join":
					if (commandParts.length == 3) {
						UUID uuid = UUID.fromString(commandParts[1]);
						String username = commandParts[2];
						// Call the appropriate method to handle join logic here
						getServer().joinGame(getClient(), uuid, username);
					} else {
						System.out.println("Usage: join <uuid> <username>");
					}
					break;

				case "create":
					if (commandParts.length == 2) {
						String username = commandParts[1];
						// Call the appropriate method to handle create logic here
						getServer().createGame(getClient(), username);
					} else {
						System.out.println("Usage: create <username>");
					}
					break;
				default:
					System.out.println("Invalid command. Available commands are: ping, join <uuid> <username>, create <username>");
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
		cliView.setLastUpdate(clientUpdate);
	}

	public static void main(String[] args) throws IOException, NotBoundException {
		GameClient gameClient = new GameClient(Boolean.parseBoolean(args[0]), args[1], Integer.parseInt(args[2]));
		gameClient.getServer().connect(gameClient.getClient());
		gameClient.runCli();
	}
}
