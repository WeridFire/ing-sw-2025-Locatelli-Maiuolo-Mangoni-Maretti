package it.polimi.ingsw.cards.enemy;

import it.polimi.ingsw.cards.EpidemicCard;
import it.polimi.ingsw.cards.planets.Planet;
import it.polimi.ingsw.cards.planets.PlanetsCard;
import it.polimi.ingsw.shipboard.LoadableType;
import it.polimi.ingsw.view.cli.CLIFrame;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlanetsCardTest {
    @Test
    void testGetCLIRepresentation() {
        List<LoadableType> l = new ArrayList<>();
        l.add(LoadableType.RED_GOODS);
        l.add(LoadableType.BLUE_GOODS);
        l.add(LoadableType.GREEN_GOODS);
        l.add(LoadableType.YELLOW_GOODS);

        // Create a pirate card with specific properties for testing visualization
        PlanetsCard testcard = new PlanetsCard(
                new Planet[]{
                        new Planet(
                            l.stream().toList()
                        ),
                        new Planet(
                                l.stream().toList()
                                ),
                        new Planet(
                                l.stream().toList()
                        ),
                        new Planet(
                                l.stream().toList()
                        ),
                },
                5,
                "",
                3
        );

        // Get the CLI representation
        CLIFrame frame = testcard.getCLIRepresentation();

        System.out.println(frame);
    }
}
