package it.polimi.ingsw.cards.enemy;

import it.polimi.ingsw.cards.enemy.PiratesCard;
import it.polimi.ingsw.cards.projectile.Projectile;
import it.polimi.ingsw.player.Player;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.support.descriptor.FileSystemSource;
import it.polimi.ingsw.GamesHandler;
import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.game.Game;
import it.polimi.ingsw.game.GameData;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PiratesCardTest {


    Player player1 = new Player("SpaceTruckKing", UUID.randomUUID());
    PiratesCard piratesCard = new PiratesCard(7, new Projectile[]{
            Projectile.createHeavyCannonFire(Direction.NORTH),
            Projectile.createLightCannonFire(Direction.NORTH),
            Projectile.createHeavyCannonFire(Direction.NORTH),
    }, 6, 2, "GT-cards_II_IT_013.jpg", 2);
    Game testGame = GamesHandler.getInstance().newGame();
    GameData testData = testGame.getGameData();

    @Test
    void SevenPlusZeroShouldEqualSeven() {

        System.out.println("Test givePrize");
        piratesCard.givePrize(player1, testData);
        assertEquals(7, player1.getCredits());

    }



    @Test
    void applyPunishment() {
    }
}