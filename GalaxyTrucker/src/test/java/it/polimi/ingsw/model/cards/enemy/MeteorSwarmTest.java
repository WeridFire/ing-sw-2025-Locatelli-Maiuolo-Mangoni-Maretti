package it.polimi.ingsw.model.cards.enemy;

import it.polimi.ingsw.GamesHandler;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.meteorstorm.MeteorSwarmCard;
import it.polimi.ingsw.model.cards.projectile.Projectile;
import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.model.game.Game;
import it.polimi.ingsw.model.gamePhases.AdventureGamePhase;
import it.polimi.ingsw.network.GameClientMock;
import it.polimi.ingsw.view.cli.CLIFrame;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import static it.polimi.ingsw.TestingUtils.setupUntilAdventurePhase;
import static it.polimi.ingsw.TestingUtils.setupUntilAssemblePhase;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MeteorSwarmTest {



    @Test
    void testGetCLIRepresentation() {
        // Create a pirate card with specific properties for testing visualization
        MeteorSwarmCard testCard = new MeteorSwarmCard(
                new Projectile[]{
                        Projectile.createSmallMeteor(Direction.EAST),
                        Projectile.createLargeMeteor(Direction.SOUTH),
                        Projectile.createLightCannonFire(Direction.WEST),
                        Projectile.createHeavyCannonFire(Direction.NORTH)
                },
                "",
                3
        );

        // Get the CLI representation
        CLIFrame frame = testCard.getCLIRepresentation();

        System.out.println(frame);
    }

    @Test
    void testDiceFrame(){
        MeteorSwarmCard testCard = new MeteorSwarmCard(
                new Projectile[]{
                        Projectile.createSmallMeteor(Direction.EAST),
                        Projectile.createLargeMeteor(Direction.SOUTH),
                        Projectile.createLightCannonFire(Direction.WEST),
                        Projectile.createHeavyCannonFire(Direction.NORTH)
                },
                "",
                3
        );


    }
}
