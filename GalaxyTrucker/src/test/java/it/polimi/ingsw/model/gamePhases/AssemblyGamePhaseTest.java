package it.polimi.ingsw.model.gamePhases;

import it.polimi.ingsw.GamesHandler;
import it.polimi.ingsw.model.game.Game;
import it.polimi.ingsw.model.gamePhases.exceptions.IncorrectGamePhaseTypeException;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.playerInput.PIRs.PIRMultipleChoice;
import it.polimi.ingsw.model.shipboard.LoadableType;
import it.polimi.ingsw.network.GameClientMock;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import static it.polimi.ingsw.MegaTest.syncClients;
import static it.polimi.ingsw.TestingUtils.setupUntilAssemblePhase;

public class AssemblyGamePhaseTest {

	private GameClientMock[] clients;
	private AdventureGamePhase adventureGamePhase;
	private Game game;
	AtomicReference<Throwable> error = new AtomicReference<>();


	@Test
	public void ShipFillingTest() throws InterruptedException, IncorrectGamePhaseTypeException {

		clients = setupUntilAssemblePhase(4, error);
		game = GamesHandler.getInstance().getGames().getFirst();
		Thread.sleep(1000);

		Arrays.stream(clients).forEach((c) ->
				{
					c.simulateCommand("cheat", "shipboard");
					c.simulateCommand("finish");
				}
		);

		// integrity problem of cheat shipboard
		for(GameClientMock c : clients){
			Thread.sleep(100);
			c.simulateCommand("endTurn");
		}
		syncClients(clients, error);
		for(GameClientMock c : clients){
			Thread.sleep(100);
			c.simulateCommand("endTurn");
		}
		syncClients(clients, error);
		Thread.sleep(500);

		//choice of crew to place

		//everyone places brown alien in cabin that allows both.
		for(GameClientMock c : clients){
			Thread.sleep(100);
			c.simulateCommand("choose", "2");
			Thread.sleep(100);

			//on the next PIR we should have 2 options

			Player player = game.getGameData().getPlayer(p -> p.getConnectionUUID().equals(c.getClientUUID()));
			PIRMultipleChoice pir = (PIRMultipleChoice) game.getGameData().getPIRHandler().getPlayerPIR(player);
			assert pir.getPossibleOptions().length == 2;
			//place humans (TOTAL 6 HUMANS)
			c.simulateCommand("choose", "0");
		}

		for(GameClientMock c : clients){
			Player player = game.getGameData().getPlayer(p -> p.getConnectionUUID().equals(c.getClientUUID()));
			assert player
					.getShipBoard()
					.getVisitorCalculatePowers()
					.getInfoFirePower()
					.getBonus(1) == 0f : "The player has loaded no purple aliens but somehow has " +
					"bonus different than 0...";

			assert player
					.getShipBoard()
					.getVisitorCalculatePowers()
					.getInfoThrustPower()
					.getBonus(1) == 2f : "The player has loaded brown aliens but isn't receiving the" +
					" bonus.";

			assert player.getShipBoard().getVisitorCalculateCargoInfo().getCrewInfo().count(LoadableType.HUMAN) == 6 :
					"the player should have 6 humans because he filled up his shipboard with aliens.";
		}


	}

}
