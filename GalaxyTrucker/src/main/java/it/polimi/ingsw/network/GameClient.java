package it.polimi.ingsw.network;

import it.polimi.ingsw.network.messages.ClientUpdate;
import it.polimi.ingsw.network.rmi.RmiClient;
import it.polimi.ingsw.network.socket.SocketClient;
import it.polimi.ingsw.controller.states.State;
import it.polimi.ingsw.util.Default;
import it.polimi.ingsw.util.Logger;
import it.polimi.ingsw.view.View;
import it.polimi.ingsw.view.cli.CLIView;
import it.polimi.ingsw.view.gui.GUIView;

import java.io.*;
import java.net.Socket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class GameClient implements IClient {

	private final IClient specClient;
	private final View view;
	private final State linkedState;

	/**
	 * The main game client.
	 * With this constructor, all the elements that composes this class are already instantiated:
	 * useful for abstraction from those constructions.
	 * This gameClient will also handle all the method calls from the server on the client (basically just the updates).
	 *
	 * @param specClient the low-level client the game will use (RMI / Socket)
	 * @param view the view this client will interact with (GUI / TUI)
	 * @param linkedState the state of the game this client is linked to
	 */
	public GameClient(IClient specClient, View view, State linkedState) {
		this.specClient = specClient;
		this.view = view;
		this.linkedState = linkedState;
	}

	/**
	 * The main game client.
	 * Based on the selected options, this client will instance a connection using either RMI or socket protocol,
	 * onto the desired host & port.
	 * This game client will also handle all the method calls from the server on the client (basically just the updates).
	 *
	 * @param useRMI {@code true} to use RMI, {@code false} to use Socket.
	 * @param host The host IP address
	 * @param port The port on the host
	 * @param useGUI {@code true} to use GUI, {@code false} to use TUI.
	 *
	 * @throws IOException Signals that some sort of I/O exception has occurred,
	 * in particular can also be for ConnectException
	 * @throws NotBoundException The RMI server is not present
	 */
	public static GameClient create(boolean useRMI, String host, Integer port, boolean useGUI)
			throws IOException, NotBoundException {
		GameClient gc;

		// state
		State state = State.getInstance();

		// view
		View view = useGUI ? new GUIView() : new CLIView();

		// low level client
		if (useRMI) {
			Registry registry = LocateRegistry.getRegistry(host, port);
			IServer server = (IServer) registry.lookup(Default.RMI_SERVER_NAME);
			RmiClient rmiClient = new RmiClient();
			// create game client
			gc = new GameClient(rmiClient, view, state);
			// init low level client (RMI)
			rmiClient.init(server, gc);
		} else {
			Socket serverSocket = new Socket(host, port);
			InputStreamReader socketRx = new InputStreamReader(serverSocket.getInputStream());
			OutputStreamWriter socketTx = new OutputStreamWriter(serverSocket.getOutputStream());
			SocketClient socketClient = new SocketClient();
			// create game client
			gc = new GameClient(socketClient, view, state);
			// init low level client (Socket)
			socketClient.init(new BufferedReader(socketRx), new BufferedWriter(socketTx), gc);
			new Thread(() -> {
				try {
					socketClient.runVirtualServer();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}).start();
		}

		// init view
		view.init(gc);

		return gc;
	}

	/**
	 * @return The low-level client the Game is currently using, either RMI or Socket.
	 */
	public IClient getClient(){
		return specClient;
	}

	public View getView() {
		return view;
	}

	public State getLinkedState() {
		return linkedState;
	}

	@Override
	public IServer getServer() {
        try {
            return getClient().getServer();
        } catch (RemoteException e) {
            getView().showError(e.getMessage());
			return null;
        }
    }

	@Override
	public void updateClient(ClientUpdate clientUpdate) {
		getLinkedState().setLastUpdate(clientUpdate);
	}

	@Override
	public void pingClient() throws RemoteException {

	}

	public static void main(String[] args) {
		boolean useRMI = (args.length > 0) ? Boolean.parseBoolean(args[0]) : Default.USE_RMI;
		try {
			GameClient gameClient = GameClient.create(
					useRMI,
					(args.length > 1) ? args[1] : Default.HOST,
					(args.length > 2) ? Integer.parseInt(args[2]) : Default.PORT(useRMI),
					(args.length > 3) ? Boolean.parseBoolean(args[3]) : Default.USE_GUI
			);
			start(gameClient);  // running body
		} catch (java.net.ConnectException | java.rmi.ConnectException e) {
			Logger.warn(e.getMessage() + "\n - The server is not available");
		} catch (IOException | NotBoundException e) {
			e.printStackTrace();
		}
		Logger.error("Client instance is closed. Please restart and connect to a running server instance.");
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
		gameClient.getLinkedState().attachView(gameClient.getView());
		// connect the client to the server
		gameClient.getServer().connect(gameClient.getClient());
        // run the view (blocking function)

		gameClient.getView().run();
	}
}
