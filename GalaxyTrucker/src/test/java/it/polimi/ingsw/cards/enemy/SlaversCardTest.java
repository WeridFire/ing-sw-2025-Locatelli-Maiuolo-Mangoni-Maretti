package it.polimi.ingsw.cards.enemy;

import it.polimi.ingsw.view.cli.CLIFrame;
import org.junit.jupiter.api.Test;

public class SlaversCardTest {
    @Test
    void testGetCLIRepresentation() {
        // Create a pirate card with specific properties for testing visualization
        SlaversCard testCard = new SlaversCard(
                2,
                3,
                6,
                3,
                "5",
                1
        );
        // Get the CLI representation
        CLIFrame frame = testCard.getCLIRepresentation();

        System.out.println(frame);
    }
}
