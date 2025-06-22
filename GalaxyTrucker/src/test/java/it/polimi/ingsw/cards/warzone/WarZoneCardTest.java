package it.polimi.ingsw.cards.warzone;

import it.polimi.ingsw.GamesHandler;
import it.polimi.ingsw.cards.projectile.Projectile;
import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.game.Game;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.game.exceptions.PlayerAlreadyInGameException;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.shipboard.tiles.MainCabinTile;
import it.polimi.ingsw.view.cli.CLIFrame;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class WarZoneCardTest {

        private WarLevel warLevelMock1;
        private WarLevel warLevelMock2;
        private WarLevel[] warLevels;
        private WarZoneCard warZoneCard;
        private Game testGame;
        private GameData testGameData;
        private Player player1;

        @BeforeEach
        void setUp() throws PlayerAlreadyInGameException {
             warZoneCard = new WarZoneCard(new WarLevel[]{
                    new WarLevel(WarFactory.createCrewCriteria(), WarFactory.createLostDaysPunishment(3)),
                    new WarLevel(WarFactory.createThrustCriteria(), WarFactory.createCrewDeathPunishment(2)),
                    new WarLevel(WarFactory.createFireCriteria(), WarFactory.createProjectilePunishment(
                            new Projectile[]{
                                    Projectile.createLightCannonFire(Direction.SOUTH),
                                    Projectile.createLightCannonFire(Direction.SOUTH)}))}, "GT-cards_I_IT_0116", 0);

            testGame = GamesHandler.getInstance().createGame("SpaceTruckKing", UUID.randomUUID(),
                    MainCabinTile.Color.BLUE);
            testGameData = testGame.getGameData();
            player1 = testGameData.getPlayers().getFirst();
            player1.setPosition(0);
        }

        @Test
        void testConstructorSetsFieldsCorrectly() {
            assertNotNull(warZoneCard);
            assertEquals("GT-cards_I_IT_0116", warZoneCard.getTextureName());
            assertEquals(GameLevel.TESTFLIGHT, warZoneCard.getLevel());
        }

        @Test
        void testGetCLIRepresentationNotNull() {
            CLIFrame frame = warZoneCard.getCLIRepresentation();
            assertNotNull(frame);
            System.out.println(frame);
        }
}
