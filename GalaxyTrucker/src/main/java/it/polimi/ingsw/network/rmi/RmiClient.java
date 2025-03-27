package src.main.java.it.polimi.ingsw.network.rmi;

import src.main.java.it.polimi.ingsw.network.IClient;
import src.main.java.it.polimi.ingsw.network.IServer;

import java.rmi.RemoteException;

public class RmiClient implements IClient {

	final IServer server;

	public RmiClient(RmiServer server) throws RemoteException {
		this.server = server;
		server.connect(this);
	}

	@Override
	public IServer getServer() {
		return server;
	}

	@Override
	public void notifyError(String error) {
		System.out.println(error);
	}

	@Override
	public void showUpdate(String update) {
		System.out.println(update);
	}
}
