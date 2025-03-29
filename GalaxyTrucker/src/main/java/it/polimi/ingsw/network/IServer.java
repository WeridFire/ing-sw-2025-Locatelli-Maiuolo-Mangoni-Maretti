package src.main.java.it.polimi.ingsw.network;

import src.main.java.it.polimi.ingsw.util.Coordinates;

import java.rmi.Remote;
import java.util.Set;
import java.util.UUID;

public interface IServer extends Remote {

	void connect(IClient client);
	void requestUpdate(IClient client);
	void joinGame(IClient client, UUID gameId, String username);
	void quitGame(IClient client);
	void drawComponent(IClient client);
	void ping(IClient client);

	void activateTiles(IClient client, Set<Coordinates> tilesToActivate);
}
