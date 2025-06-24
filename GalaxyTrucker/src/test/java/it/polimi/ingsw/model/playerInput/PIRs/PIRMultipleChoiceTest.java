package it.polimi.ingsw.model.playerInput.PIRs;

import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.playerInput.PIRs.PIRMultipleChoice;
import it.polimi.ingsw.model.shipboard.tiles.MainCabinTile;
import it.polimi.ingsw.view.cli.CLIFrame;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class PIRMultipleChoiceTest {

	@Test
	void testPIRMultipleChoiceCLI() {
		// Dummy player for testing
		Player testPlayer = new Player("PlayerTest", UUID.randomUUID(), MainCabinTile.Color.BLUE);

		// Example options for the PIR
		String[] options = new String[] {
				"This is the first option",
				"This is the second option",
				"There can be many options!",
				"You can only select one."
		};

		// Create the PIRMultipleChoice instance
		PIRMultipleChoice pir = new PIRMultipleChoice(
				testPlayer,
				30, // cooldown in seconds
				"What option do you prefer?",
				options,
				0 // default choice if no response
		);

		// Get its CLI representation
		CLIFrame representation = pir.getCLIRepresentation();

		// Print it to the console
		System.out.println(representation);
	}

	@Test
	void testPIRMultipleChoiceCLIVeryLongMessage() {
		// Dummy player for testing
		Player testPlayer = new Player("PlayerTest", UUID.randomUUID(), MainCabinTile.Color.BLUE);

		// Example options for the PIR
		String[] options = new String[] {
				"This is the first option",
				"This is the second option",
				"There can be many options!",
				"You can only select one."
		};

		// Create the PIRMultipleChoice instance
		PIRMultipleChoice pir = new PIRMultipleChoice(
				testPlayer,
				30, // cooldown in seconds
				"What option do you prefer?" + " bla".repeat(69),
				options,
				0 // default choice if no response
		);

		// Get its CLI representation
		CLIFrame representation = pir.getCLIRepresentation();

		// Print it to the console
		System.out.println(representation);
	}


}
