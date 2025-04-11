package it.polimi.ingsw.cards.enemy;

import it.polimi.ingsw.cards.EpidemicCard;
import it.polimi.ingsw.cards.StarDustCard;
import it.polimi.ingsw.view.cli.CLIFrame;
import org.junit.jupiter.api.Test;

public class EpidemicCardTest {
    @Test
    void testGetCLIRepresentation() {
        // Create a pirate card with specific properties for testing visualization
        EpidemicCard testcard = new EpidemicCard(
                "",
                2
        );

        // Get the CLI representation
        CLIFrame frame = testcard.getCLIRepresentation();

        System.out.println(frame);
    }
}
