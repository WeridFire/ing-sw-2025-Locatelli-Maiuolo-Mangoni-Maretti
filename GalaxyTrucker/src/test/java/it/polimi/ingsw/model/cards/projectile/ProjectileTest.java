package it.polimi.ingsw.model.cards.projectile;

import it.polimi.ingsw.enums.Direction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProjectileTest {

    Projectile smallM = Projectile.createSmallMeteor(Direction.NORTH);
    Projectile bigM = Projectile.createLargeMeteor(Direction.NORTH);
    Projectile smallC = Projectile.createLightCannonFire(Direction.NORTH);
    Projectile bigC = Projectile.createHeavyCannonFire(Direction.NORTH);

    @Test
    void testPrint(){

        assertEquals("Small Meteor from North]", smallM.toVerboseString());
        assertEquals("Large Meteor from North]", bigM.toVerboseString());
        assertEquals("Light Cannon Fire from North]", smallC.toVerboseString());
        assertEquals("Heavy Cannon Fire from North]", bigC.toVerboseString());

        assertEquals("☄️↓", smallM.toEmojiString());
        assertEquals("🌑↓", bigM.toEmojiString());
        assertEquals("💥↓", smallC.toEmojiString());
        assertEquals("🔥↓", bigC.toEmojiString());

    }
}