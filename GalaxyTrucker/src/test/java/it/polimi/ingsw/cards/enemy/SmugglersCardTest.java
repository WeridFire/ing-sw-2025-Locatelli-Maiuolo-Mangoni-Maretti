package it.polimi.ingsw.cards.enemy;

import it.polimi.ingsw.cards.planets.Planet;
import it.polimi.ingsw.cards.planets.PlanetsCard;
import it.polimi.ingsw.shipboard.LoadableType;
import it.polimi.ingsw.view.cli.CLIFrame;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class SmugglersCardTest {
    @Test
    void testGetCLIRepresentation() {
        LoadableType[] l = new LoadableType[]{
                LoadableType.RED_GOODS,
                LoadableType.BLUE_GOODS,
                LoadableType.GREEN_GOODS,
                LoadableType.YELLOW_GOODS,
        };

        // Create a pirate card with specific properties for testing visualization
        SmugglersCard testcard = new SmugglersCard(
               1,
                l,
                1,
                3,
                "",
                2
        );

        // Get the CLI representation
        CLIFrame frame = testcard.getCLIRepresentation();

        System.out.println(frame);
    }

}
