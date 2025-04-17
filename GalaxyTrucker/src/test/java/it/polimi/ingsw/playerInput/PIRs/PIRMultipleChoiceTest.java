package it.polimi.ingsw.playerInput.PIRs;

import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.view.cli.CLIFrame;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class PIRMultipleChoiceTest {

	@Test
	void testPIRMultipleChoiceCLI() {
		// Dummy player for testing
		Player testPlayer = new Player("PlayerTest", UUID.randomUUID());

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


}
