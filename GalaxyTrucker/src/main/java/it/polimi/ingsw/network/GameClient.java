package it.polimi.ingsw.network;

import it.polimi.ingsw.network.messages.ClientUpdate;
import it.polimi.ingsw.network.rmi.RmiClient;
import it.polimi.ingsw.network.socket.SocketClient;
import it.polimi.ingsw.controller.states.State;
import it.polimi.ingsw.view.View;
import it.polimi.ingsw.view.cli.CLIView;
import it.polimi.ingsw.view.gui.GUIView;

import java.io.*;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class GameClient implements IClient{

	private final boolean useRMI;
	private RmiClient rmiClient = null;
	private SocketClient socketClient = null;
	private final View view;

	/**
	 * The main game client. Based on the selected options, this client will instance a connection using either RMI or
	 * socket protocol, onto the desired host & port. This gameClient will also handle all the method calls from the
	 * server on the client (basically just the updates).
	 *
	 * @param useRMI {@code true} to use RMI, {@code false} to use Socket.
	 * @param host The host IP address
	 * @param port The port on the host
	 * @param useGUI {@code true} to use GUI, {@code false} to use TUI.
	 *
	 * @throws IOException Signals that some sort of I/O exception has occurred
	 * @throws NotBoundException The RMI server is not present
	 */
	public GameClient(boolean useRMI, String host, Integer port, boolean useGUI) throws IOException, NotBoundException {
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

		view = useGUI ? new GUIView(this) : new CLIView(this);
		view.init();
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
	public IServer getServer() {
        try {
            return getClient().getServer();
        } catch (RemoteException e) {
            view.showError(e.getMessage());
			return null;
        }
    }

	@Override
	public void updateClient(ClientUpdate clientUpdate) {
		State.getInstance().setLastUpdate(clientUpdate);
	}

	public View getView() {
		return view;
	}

	public static void main(String[] args) throws IOException, NotBoundException {
		boolean useRMI = (args.length > 0) ? Boolean.parseBoolean(args[0]) : false;
		GameClient gameClient = new GameClient(
				useRMI,
				(args.length > 1) ? args[1] : "localhost",
				(args.length > 2) ? Integer.parseInt(args[2]) : (useRMI ? 1111 : 1234),
				(args.length > 3) ? Boolean.parseBoolean(args[3]) : false
		);

		start(gameClient);
	}

	/**
	 * Attach the gameClient's view in the observer pattern, connect the client to its server
	 * and run the view (blocking function).
	 *
	 * @param gameClient the client to start.
	 * @throws RemoteException thrown only by {@link IServer#connect(IClient)}.
	 */
	public static void start(GameClient gameClient) throws RemoteException {
		// attach the view in observer pattern
		State.getInstance().attachView(gameClient.view);
		// connect the client to the server
		gameClient.getServer().connect(gameClient.getClient());
        // run the view (blocking function)
		gameClient.view.run();
	}
}
