package it.polimi.ingsw.model.gamePhases.adventure;

import it.polimi.ingsw.GamesHandler;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.DeckFactory;
import it.polimi.ingsw.model.game.Game;
import it.polimi.ingsw.model.gamePhases.AdventureGamePhase;
import it.polimi.ingsw.model.gamePhases.exceptions.IncorrectGamePhaseTypeException;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.playerInput.PIRs.PIRAddLoadables;
import it.polimi.ingsw.model.shipboard.LoadableType;
import it.polimi.ingsw.model.shipboard.tiles.exceptions.TooMuchLoadException;
import it.polimi.ingsw.model.shipboard.tiles.exceptions.UnsupportedLoadableItemException;
import it.polimi.ingsw.network.GameClientMock;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
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

		//add stuff on players shipboard to test rearrangement
		Player p0 = game.getGameData().getPlayer(p -> p.getConnectionUUID().equals(clients[0].getClientUUID()));
		Player p1 = game.getGameData().getPlayer(p -> p.getConnectionUUID().equals(clients[1].getClientUUID()));

		assert p0 != null && p1 != null;
		AtomicInteger goodsOnShip0 = new AtomicInteger();
		p0.getShipBoard()
				.getVisitorCalculateCargoInfo()
				.getGoodsInfo()
				.getLocationsWithAvailableSpace(1)
				.forEach((coords, tile) -> {
					try {
						tile.loadItems(LoadableType.BLUE_GOODS, 1);
						goodsOnShip0.addAndGet(1);
					} catch (TooMuchLoadException | UnsupportedLoadableItemException e) {
						throw new RuntimeException(e);
					}
				});

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


		//player 0 is asked if they want to rearrange goods.
		int goodsOnShip = p0.getShipBoard().getVisitorCalculateCargoInfo().getGoodsInfo().getAllLoadedItems().size();
		assert goodsOnShip == goodsOnShip0.get() : "Goods on the ship is not expected value " + goodsOnShip0.get();
		clients[0].simulateCommand("choose", "0");
		Thread.sleep(10);
		PIRAddLoadables pir = (PIRAddLoadables) game.getGameData().getPIRHandler().getPlayerPIR(p0);
		System.out.println(pir.getCLIRepresentation());
		//The goods on the shipboard should be empty

		assert p0.getShipBoard().getVisitorCalculateCargoInfo().getGoodsInfo().getAllLoadedItems().isEmpty() :
				"During shipboard goods re-arrangement, expected goods on the shipboard should be 0, but instead is "
				+ p0.getShipBoard().getVisitorCalculateCargoInfo().getGoodsInfo().getAllLoadedItems().size();

		pir = (PIRAddLoadables) game.getGameData().getPIRHandler().getPlayerPIR(p0);

		assert pir != null;
		//the client places all of the goods into cargo holds, minus the last one. It gets skipped
		clients[0].simulateCommand("allocate", "(9,8)", "BLUE_GOODS", "2");
		clients[0].simulateCommand("allocate", "(6,7)", "BLUE_GOODS", "1");
		clients[0].simulateCommand("confirm");
		Thread.sleep(10);
		assert p0.getShipBoard().getVisitorCalculateCargoInfo().getGoodsInfo().getAllLoadedItems().size() == 3 :
				"During shipboard goods re-arrangement, expected final goods on the shipboard should be 3, but instead is "
						+ p0.getShipBoard().getVisitorCalculateCargoInfo().getGoodsInfo().getAllLoadedItems().size();
		assert pir.getFloatingLoadables().size() == 1 : "Floating loadables should be 1";

		assert false : "The rest of the test is not finished. Ignore.";

		System.out.println(pir.getCLIRepresentation());
		clients[0].simulateCommand("allocate", "(8,6)", "RED_GOODS", "6");
		assert pir.getFloatingLoadables().size() == 2;
		clients[0].simulateCommand("allocate", "(8,6)", "RED_GOODS", "1");
		clients[0].simulateCommand("allocate", "(7,9)", "RED_GOODS", "1");
		//the request is processed locally, so effects happen only at the end of the pir.
		//the player has space only for 1 loadable so the other one is left out
		assert pir.getFloatingLoadables().size() == 1;

		pir = (PIRAddLoadables) game.getGameData().getPIRHandler().getPlayerPIR(p1);
		System.out.println(pir.getCLIRepresentation());

		clients[1].simulateCommand("endTurn");

		Thread.sleep(10);
	}



}
