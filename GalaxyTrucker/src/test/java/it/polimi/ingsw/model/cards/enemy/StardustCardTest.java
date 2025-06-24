package it.polimi.ingsw.model.cards.enemy;

import it.polimi.ingsw.model.cards.StarDustCard;
import it.polimi.ingsw.view.cli.CLIFrame;
import org.junit.jupiter.api.Test;

public class StardustCardTest {
    @Test
    void testGetCLIRepresentation() {
        // Create a pirate card with specific properties for testing visualization
        StarDustCard testcard = new StarDustCard(
                "",
                2
        );

        // Get the CLI representation
        CLIFrame frame = testcard.getCLIRepresentation();

        System.out.println(frame);
    }
}
