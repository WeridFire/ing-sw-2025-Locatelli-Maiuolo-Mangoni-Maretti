package it.polimi.ingsw.model.cards.enemy;

import it.polimi.ingsw.model.cards.AbandonedShipCard;
import it.polimi.ingsw.view.cli.CLIFrame;
import org.junit.jupiter.api.Test;

public class AbandonedShipCardTest {
    @Test
    void testGetCLIRepresentation() {
        // Create a pirate card with specific properties for testing visualization
        AbandonedShipCard testCard = new AbandonedShipCard(
                1,
                2,
                3,
                "",
                3
        );

        // Get the CLI representation
        CLIFrame frame = testCard.getCLIRepresentation();

        System.out.println(frame);
    }
}
