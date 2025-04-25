package it.polimi.ingsw.cards.enemy;

import it.polimi.ingsw.cards.meteorstorm.MeteorSwarmCard;
import it.polimi.ingsw.cards.projectile.Projectile;
import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.view.cli.CLIFrame;
import org.junit.jupiter.api.Test;

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
