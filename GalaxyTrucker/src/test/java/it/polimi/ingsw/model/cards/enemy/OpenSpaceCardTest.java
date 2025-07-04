package it.polimi.ingsw.model.cards.enemy;

import it.polimi.ingsw.model.cards.OpenSpaceCard;
import it.polimi.ingsw.view.cli.CLIFrame;
import org.junit.jupiter.api.Test;

public class OpenSpaceCardTest {
    @Test
    void testGetCLIRepresentation() {
        // Create a pirate card with specific properties for testing visualization
        OpenSpaceCard testcard = new OpenSpaceCard(
                "",
                2
        );

        // Get the CLI representation
        CLIFrame frame = testcard.getCLIRepresentation();

        System.out.println(frame);
    }
}
