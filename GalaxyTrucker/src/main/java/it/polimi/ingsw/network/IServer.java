package src.main.java.it.polimi.ingsw.network;

import java.rmi.Remote;
import java.util.UUID;

public interface IServer extends Remote {

	void connect(IClient client);

	void sendAvailableGamesToClient(IClient client);

	void joinGame(UUID gameId, String username);
}
