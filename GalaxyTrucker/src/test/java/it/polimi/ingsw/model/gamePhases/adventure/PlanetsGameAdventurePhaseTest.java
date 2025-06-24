package it.polimi.ingsw.model.gamePhases.adventure;

import it.polimi.ingsw.GamesHandler;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.model.cards.AbandonedShipCard;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.DeckFactory;
import it.polimi.ingsw.model.cards.planets.PlanetsCard;
import it.polimi.ingsw.model.game.Game;
import it.polimi.ingsw.model.gamePhases.AdventureGamePhase;
import it.polimi.ingsw.model.gamePhases.exceptions.IncorrectGamePhaseTypeException;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.playerInput.PIRs.PIRAddLoadables;
import it.polimi.ingsw.model.shipboard.LoadableType;
import it.polimi.ingsw.network.GameClientMock;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static it.polimi.ingsw.TestingUtils.setupUntilAdventurePhase;

class PlanetsGameAdventurePhaseTest {

	private GameClientMock[] clients;
	private AdventureGamePhase adventureGamePhase;
	private Game game;
	AtomicReference<Throwable> error = new AtomicReference<>();


	@Test
	public void PlanetsGameAdventurePhaseTest() throws InterruptedException, IncorrectGamePhaseTypeException {

		Card card = DeckFactory.createTutorialDeck().stream().filter(c -> c.getTitle().equals("PLANETS")).findFirst().orElse(null);
		assert card != null;
		clients = setupUntilAdventurePhase(2, error, card);
		game = GamesHandler.getInstance().getGames().getFirst();
		Thread.sleep(1000);
		assert Objects.equals(game.getGameData().getDeck().getCurrentCard().getTitle(), card.getTitle());
		//everyone approves the new card notification
		for(GameClientMock client : clients) {
			Thread.sleep(20);
			client.simulateCommand("endTurn");
		}
		//player 0 chooses to land on planet 1
		Thread.sleep(10);
		clients[0].simulateCommand("choose", "1");
		Thread.sleep(10);

		//player 1 chooses to land on planet 1 (same index because it's taken account in the PIR)
		clients[1].simulateCommand("choose", "1");
		Thread.sleep(10);

		Player p0 = game.getGameData().getPlayer(p -> p.getConnectionUUID().equals(clients[0].getClientUUID()));
		Player p1 = game.getGameData().getPlayer(p -> p.getConnectionUUID().equals(clients[1].getClientUUID()));
		assert p0 != null && p1 != null;
		PIRAddLoadables pir = (PIRAddLoadables) game.getGameData().getPIRHandler().getPlayerPIR(p0);
		assert pir != null;
		System.out.println(pir.getCLIRepresentation());
		clients[0].simulateCommand("allocate", "(8,6)", "RED_GOODS", "6");
		assert pir.getFloatingLoadables().size() == 2;
		clients[0].simulateCommand("allocate", "(8,6)", "RED_GOODS", "1");
		clients[0].simulateCommand("allocate", "(7,9)", "RED_GOODS", "1");
		//the request is processed locally, so effects happen only at the end of the pir.
		assert pir.getFloatingLoadables().isEmpty();

		pir = (PIRAddLoadables) game.getGameData().getPIRHandler().getPlayerPIR(p1);
		System.out.println(pir.getCLIRepresentation());

		clients[1].simulateCommand("allocate", "(8,6)", "BLUE_GOODS", "1");
		clients[1].simulateCommand("allocate", "(7,9)", "RED_GOODS", "1");
		clients[1].simulateCommand("allocate", "(7,9)", "BLUE_GOODS", "1");

		Thread.sleep(10);

	}



}
