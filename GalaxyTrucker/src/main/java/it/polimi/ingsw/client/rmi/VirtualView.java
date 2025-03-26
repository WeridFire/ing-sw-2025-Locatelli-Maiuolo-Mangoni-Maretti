package src.main.java.it.polimi.ingsw.client.rmi;

import java.rmi.Remote;
// usata da rmi per lanciare eccezioni quando ci sono problemi di comunicazioni
import java.rmi.RemoteException;

public interface VirtualView extends Remote { ;
	void showUpdate(ServerUpdate update) throws RemoteException;
	void reportError(String details) throws RemoteException;
}