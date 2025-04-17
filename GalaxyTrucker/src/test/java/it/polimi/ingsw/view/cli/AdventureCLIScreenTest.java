package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.enums.GameLevel;
import org.junit.jupiter.api.Test;

public class AdventureCLIScreenTest {

    @Test
    public void testBoard() {
        AdevntureCLIScreen c = new AdevntureCLIScreen("Piedi");
        CLIFrame frame = c.getBoardFrame(GameLevel.TWO);

    }
}
