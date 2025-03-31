package src.main.java.it.polimi.ingsw.network;

import src.main.java.it.polimi.ingsw.network.messages.ClientUpdate;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IClient extends Remote {

	IServer getServer() throws RemoteException;
	void updateClient(ClientUpdate clientUpdate) throws RemoteException;

}
