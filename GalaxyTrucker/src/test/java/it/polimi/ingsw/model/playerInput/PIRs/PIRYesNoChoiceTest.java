package it.polimi.ingsw.model.playerInput.PIRs;

import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.playerInput.PIRs.PIRYesNoChoice;
import it.polimi.ingsw.model.shipboard.tiles.MainCabinTile;
import it.polimi.ingsw.util.Default;
import it.polimi.ingsw.view.cli.CLIFrame;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class PIRYesNoChoiceTest {

	@Test
	void testPIRMultipleChoiceCLI() {
		// Dummy player for testing
		Player testPlayer = new Player("PlayerTest", UUID.randomUUID(), MainCabinTile.Color.BLUE);

		PIRYesNoChoice pir = new PIRYesNoChoice(testPlayer, 1, "Do you want to activate the shields?", true);

		// Get its CLI representation
		CLIFrame representation = pir.getCLIRepresentation();

		// Print it to the console
		System.out.println(representation);
	}

}
