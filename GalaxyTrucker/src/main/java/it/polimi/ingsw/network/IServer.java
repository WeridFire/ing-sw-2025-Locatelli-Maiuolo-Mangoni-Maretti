package src.main.java.it.polimi.ingsw.network;

import src.main.java.it.polimi.ingsw.game.Game;

import java.rmi.Remote;
import java.util.Set;
import java.util.UUID;

public interface IServer extends Remote {

	void connect(IClient client);

	Set<Game> getGames();

	Game joinGame(UUID gameId, String username);
}
