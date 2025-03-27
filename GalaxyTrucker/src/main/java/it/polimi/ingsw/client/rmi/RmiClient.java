package src.main.java.it.polimi.ingsw.client.rmi;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class RmiClient implements VirtualView {

	final RmiServer server;

	public RmiClient(RmiServer server) throws RemoteException {
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
				//perform action-? But all of this needs to be remade.
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
		RmiServer server = (RmiServer) registry.lookup(serverName);
		new RmiClient(server).run();
	}


}
