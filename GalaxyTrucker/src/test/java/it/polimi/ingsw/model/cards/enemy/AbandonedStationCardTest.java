package it.polimi.ingsw.model.cards.enemy;

import it.polimi.ingsw.model.cards.AbandonedStationCard;
import it.polimi.ingsw.model.shipboard.LoadableType;
import it.polimi.ingsw.view.cli.CLIFrame;
import org.junit.jupiter.api.Test;

public class AbandonedStationCardTest {
    @Test
    void testGetCLIRepresentation() {
        // Create a pirate card with specific properties for testing visualization
        AbandonedStationCard testCard = new AbandonedStationCard(
                new LoadableType[] {
                        LoadableType.RED_GOODS,
                        LoadableType.BLUE_GOODS,
                        LoadableType.GREEN_GOODS,
                        LoadableType.YELLOW_GOODS
                },
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
