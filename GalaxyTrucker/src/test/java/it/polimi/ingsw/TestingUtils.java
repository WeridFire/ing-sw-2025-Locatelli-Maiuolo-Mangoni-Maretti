package it.polimi.ingsw;

import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.Deck;
import it.polimi.ingsw.model.cards.DeckFactory;
import it.polimi.ingsw.model.game.Game;
import it.polimi.ingsw.model.gamePhases.AdventureGamePhase;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.playerInput.PIRType;
import it.polimi.ingsw.model.playerInput.PIRs.PIR;
import it.polimi.ingsw.model.playerInput.exceptions.WrongPlayerTurnException;
import it.polimi.ingsw.network.GameClientMock;
import it.polimi.ingsw.network.GameServer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import static it.polimi.ingsw.MegaTest.syncClients;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class TestingUtils {

	/**
	 * greek alphabet letters in order, useful for mock names.
	 */
	public static final String[] COOL_NAMES = {
			"alpha", "beta", "gamma", "delta", "epsilon", "zeta", "eta", "theta", "iota", "kappa", "lambda", "mu",
			"nu", "xi", "omicron", "pi", "rho", "sigma", "tau", "upsilon", "phi", "chi", "psi", "omega"
	};
	public static String getCoolName(int index) {
		int times = index / COOL_NAMES.length;
		return COOL_NAMES[index % COOL_NAMES.length] + (times > 0 ? ("-" + times) : "");
	}

	/**
	 * Starts the server and connects a set amount of clients to it.
	 * @param clientsAmount The amount of clients to connect.
	 * @param error A reference to the error stdout
	 * @return The array of clients instances.
	 */
	public static GameClientMock[] setupServerAndClients(int clientsAmount, AtomicReference<Throwable> error){
		assertDoesNotThrow(GameServer::start);
		GameClientMock[] clients = new GameClientMock[clientsAmount];
		for (int i = 0; i < clientsAmount; i++) {
			clients[i] = new GameClientMock(getCoolName(i), error).assertRefresh();
		}
		syncClients(clients, error);
		return clients;
	}

	public static GameClientMock[] setupUntilAssemblePhase(int clientsAmount, AtomicReference<Throwable> error) throws InterruptedException {
		GameClientMock[] clients = setupServerAndClients(clientsAmount, error);
		clients[0].simulateCommand("create", clients[0].getMockName());
		Game g = GamesHandler.getInstance().getGames().getFirst();
		syncClients(clients, error);
		assert g.getGameData().getCurrentGamePhaseType() == GamePhaseType.LOBBY;
		clients[0].simulateCommand("settings", "minplayers", String.valueOf(Math.min(clientsAmount, 4)));
		Arrays.stream(clients).forEach(c -> c.simulateCommand("join",
																	String.valueOf(g.getId()),
																	c.getMockName()));
		Thread.sleep(1000);
		assert g.getGameData().getCurrentGamePhaseType() == GamePhaseType.ASSEMBLE;
		syncClients(clients, error);
		return clients;
	}

	public static GameClientMock[] setupUntilAdventurePhase(int clientsAmount, AtomicReference<Throwable> error, Card card) throws InterruptedException {
		GameClientMock[] clients = setupUntilAssemblePhase(clientsAmount, error);

		Game g = GamesHandler.getInstance().getGames().getFirst();
		Arrays.stream(clients).forEach((c) ->
				{
					c.simulateCommand("cheat", "shipboard");
					c.simulateCommand("finish");
				}
		);

		ArrayList<Card> cards = DeckFactory.createTutorialDeck();
		cards.addFirst(card);
		g.getGameData().setDeck(Deck.deterministic(cards, null));

		// void card to refuse mixing deck at start of flight
		g.getGameData().setCurrentGamePhase(new AdventureGamePhase(g.getGameData(), null));
		// DO NOT play loop: it's just to simulate being in flight

		// integrity problem of cheat shipboard
		for(GameClientMock c : clients){
			Thread.sleep(100);
			c.simulateCommand("endTurn");
			Thread.sleep(100);
			c.simulateCommand("endTurn");
			Thread.sleep(100);
			c.simulateCommand("choose", "2");
			Thread.sleep(100);
			c.simulateCommand("choose", "1");
			//the player places 1 purple alien & 1 brown alien
		}

		syncClients(clients, error);
		Thread.sleep(500);

		assert g.getGameData().getCurrentGamePhaseType() == GamePhaseType.ADVENTURE;
		return clients;
	}

}
