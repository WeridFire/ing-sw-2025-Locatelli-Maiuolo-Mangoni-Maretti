package it.polimi.ingsw.network;

import it.polimi.ingsw.network.messages.ClientUpdate;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IClient extends Remote {

	IServer getServer() throws RemoteException;
	void updateClient(ClientUpdate clientUpdate) throws RemoteException;
	void pingClient() throws RemoteException;
}
