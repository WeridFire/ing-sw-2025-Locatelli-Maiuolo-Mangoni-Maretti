package src.main.java.it.polimi.ingsw.network;

import src.main.java.it.polimi.ingsw.enums.GameState;

import java.rmi.Remote;

public interface IClient extends Remote {

	IServer getServer();

	void notifyError(String error);
	void showUpdate(String update);
}
