package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.enums.GameLevel;
import org.junit.jupiter.api.Test;

public class AdventureCLIScreenTest {

    @Test
    public void testBoard() {
        AdventureCLIScreen c = new AdventureCLIScreen("Piedi");
        System.out.println(c.getCLIRepresentation().toString());
    }
}
