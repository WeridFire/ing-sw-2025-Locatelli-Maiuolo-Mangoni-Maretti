package it.polimi.ingsw.model.cards.enemy;

import it.polimi.ingsw.model.cards.enemy.PiratesCard;
import it.polimi.ingsw.model.cards.projectile.Projectile;
import it.polimi.ingsw.model.game.exceptions.PlayerAlreadyInGameException;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.shipboard.tiles.MainCabinTile;
import it.polimi.ingsw.util.Default;
import it.polimi.ingsw.view.cli.CLIFrame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import it.polimi.ingsw.GamesHandler;
import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.model.game.Game;
import it.polimi.ingsw.model.game.GameData;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PiratesCardTest {

    Player player1;
    PiratesCard piratesCard;
    Game testGame;
    GameData testGameData;

    @BeforeEach
    void setUp() throws PlayerAlreadyInGameException {
        piratesCard = new PiratesCard(7, new Projectile[]{
                Projectile.createHeavyCannonFire(Direction.NORTH),
                Projectile.createLightCannonFire(Direction.NORTH),
                Projectile.createHeavyCannonFire(Direction.NORTH),
        }, 6, 2, "GT-cards_II_IT_013.jpg", 2);
        testGame = GamesHandler.getInstance().createGame("SpaceTruckKing", UUID.randomUUID(),
                MainCabinTile.Color.BLUE);
        testGameData = testGame.getGameData();
        player1 = testGameData.getPlayers().getFirst();
        player1.setPosition(0);
    }


    @Test
    void SevenPlusZeroShouldEqualSeven() {

        System.out.println("Test givePrize");
        int oldSeconds = Default.PIR_SECONDS;
        Default.PIR_SECONDS = 1;
        piratesCard.givePrize(player1, testGameData);
        Default.PIR_SECONDS = oldSeconds;
        assertEquals(7, player1.getCredits());

    }

    @Test
    void applyPunishment() {
    }

    @Test
    void testGetCLIRepresentation() {
        // Create a pirate card with specific properties for testing visualization
        PiratesCard testCard = new PiratesCard(
                15,  // bounty
                new Projectile[]{
                        Projectile.createSmallMeteor(Direction.EAST),
                        Projectile.createLargeMeteor(Direction.SOUTH),
                        Projectile.createLightCannonFire(Direction.WEST),
                        Projectile.createHeavyCannonFire(Direction.NORTH)
                },
                8,   // firepower
                3,   // lost days
                "test-image.jpg",
                2    // card ID
        );

        // Get the CLI representation
        CLIFrame frame = testCard.getCLIRepresentation();

        System.out.println(frame);
    }
}