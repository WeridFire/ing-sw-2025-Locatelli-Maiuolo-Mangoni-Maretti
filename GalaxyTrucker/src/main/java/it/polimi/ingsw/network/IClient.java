package src.main.java.it.polimi.ingsw.network;

import java.rmi.Remote;

public interface IClient extends Remote {

	IServer getServer();

	void notifyError(String error);

}
