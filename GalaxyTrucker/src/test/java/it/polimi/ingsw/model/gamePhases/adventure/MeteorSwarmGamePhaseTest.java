package it.polimi.ingsw.model.gamePhases.adventure;

import it.polimi.ingsw.GamesHandler;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.model.cards.AbandonedShipCard;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.DeckFactory;
import it.polimi.ingsw.model.game.Game;
import it.polimi.ingsw.model.gamePhases.AdventureGamePhase;
import it.polimi.ingsw.model.gamePhases.exceptions.IncorrectGamePhaseTypeException;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.playerInput.PIRs.PIRDelay;
import it.polimi.ingsw.model.shipboard.LoadableType;
import it.polimi.ingsw.network.GameClientMock;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static it.polimi.ingsw.TestingUtils.setupUntilAdventurePhase;

class MeteorSwarmGamePhaseTest {

	private GameClientMock[] clients;
	private AdventureGamePhase adventureGamePhase;
	private Game game;
	AtomicReference<Throwable> error = new AtomicReference<>();


	@Test
	public void MeteorSwarmGamePhaseTest() throws InterruptedException, IncorrectGamePhaseTypeException {
		Card card = DeckFactory.createTutorialDeck().stream().filter(c -> c.getTitle().equals("METEOR SWARM")).findFirst().orElse(null);
		assert card != null;
		clients = setupUntilAdventurePhase(2, error, card);
		game = GamesHandler.getInstance().getGames().getFirst();

		//add stuff on players shipboard to test rearrangement
		Player p0 = game.getGameData().getPlayer(p -> p.getConnectionUUID().equals(clients[0].getClientUUID()));
		Player p1 = game.getGameData().getPlayer(p -> p.getConnectionUUID().equals(clients[1].getClientUUID()));

		Thread.sleep(1000);
		assert Objects.equals(game.getGameData().getDeck().getCurrentCard().getTitle(), card.getTitle());

		//everyone approves the new card notification
		for(GameClientMock client : clients) {
			Thread.sleep(20);
			client.simulateCommand("endTurn");
		}
		Thread.sleep(1000);
		PIRDelay pir = (PIRDelay) game.getGameData().getPIRHandler().getPlayerPIR(p1);
		System.out.println(pir.getCLIRepresentation());


	}



}
