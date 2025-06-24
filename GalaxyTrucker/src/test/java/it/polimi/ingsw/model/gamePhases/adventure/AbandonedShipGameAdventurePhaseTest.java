package it.polimi.ingsw.model.gamePhases.adventure;

import it.polimi.ingsw.GamesHandler;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.model.cards.AbandonedShipCard;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.game.Game;
import it.polimi.ingsw.model.gamePhases.AdventureGamePhase;
import it.polimi.ingsw.model.gamePhases.exceptions.IncorrectGamePhaseTypeException;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.shipboard.LoadableType;
import it.polimi.ingsw.network.GameClientMock;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static it.polimi.ingsw.TestingUtils.setupUntilAdventurePhase;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class AbandonedShipGameAdventurePhaseTest {

	private GameClientMock[] clients;
	private AdventureGamePhase adventureGamePhase;
	private Game game;
	AtomicReference<Throwable> error = new AtomicReference<>();


	@Test
	public void AbandonedShipGamePhaseTest() throws InterruptedException, IncorrectGamePhaseTypeException {
		Card card = new AbandonedShipCard(2, 2, 2, null, 0);
		clients = setupUntilAdventurePhase(2, error, card);
		game = GamesHandler.getInstance().getGames().getFirst();
		Thread.sleep(1000);
		assert Objects.equals(game.getGameData().getDeck().getCurrentCard().getTitle(), card.getTitle());
		//everyone approves the new card notification
		for(GameClientMock client : clients) {
			Thread.sleep(20);
			client.simulateCommand("endTurn");
		}
		//player 0 chooses to loot abandoned ship
		Thread.sleep(1000);
		clients[0].simulateCommand("choose", "0");
		Thread.sleep(1000);
		Player player = game.getGameData().getPlayer(p -> p.getConnectionUUID().equals(clients[0].getClientUUID()));
		int crew = player.getShipBoard().getVisitorCalculateCargoInfo().getCrewInfo().countAll(LoadableType.CREW_SET);
		clients[0].simulateCommand("endTurn");
		player.getShipBoard().resetVisitors();
		int newCrew = player.getShipBoard().getVisitorCalculateCargoInfo().getCrewInfo().countAll(LoadableType.CREW_SET);
		assert newCrew == crew-2;
		Thread.sleep(1000);
		assert (game.getGameData().getCurrentGamePhaseType() == GamePhaseType.ADVENTURE)
				&& (game.getGameData().getPlayersInFlight().size() == 2);
	}



}
