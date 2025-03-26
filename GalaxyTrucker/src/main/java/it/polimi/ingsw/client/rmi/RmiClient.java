package src.main.java.it.polimi.ingsw.client.rmi;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class RmiClient implements VirtualView{

	final VirtualServer server;

	public RmiClient(VirtualServer server) throws RemoteException {
		this.server = server;
	}

	private void run() throws RemoteException {
		this.server.connect(this);
		this.runCli();
	}

	private void runCli() throws RemoteException {
		Scanner scan = new Scanner(System.in);
		while (true) {
			System.out.print("> ");
			int command = scan.nextInt();

			if (command == 0) {
				server.reset();
			} else {
				//Handle a possible interaction. The interaction should be callable on the server, because this is RMI.
				//Ideally we should start here by getting a list of available games. So server should expose a method
				//for that.
			}
		}
	}

	public static void main(String[] args) throws RemoteException, NotBoundException {
		final String serverName = "GalaxyTruckerServer";
		Registry registry = LocateRegistry.getRegistry(args[0], 1234);
		VirtualServer server = (VirtualServer) registry.lookup(serverName);
		new RmiClient(server).run();
	}

	@Override
	public void showUpdate(ServerUpdate update) throws RemoteException {
		// Attenzione alle data races
		System.out.print("\n[UPDATE] " + update + "\n> ");
	}

	@Override
	public void reportError(String details) throws RemoteException {
		System.err.print("\n[ERROR] " + details + "\n> ");
	}

}
