package it.polimi.ingsw.cards.projectile;

import it.polimi.ingsw.enums.Direction;

import java.io.Serializable;
import java.util.Random;

public class Projectile implements Serializable {

    /**
     * Determines if the projectile bounces on flat sides of the ship.
     */
    private final boolean bouncy;

    /**
     * Determines if you can defend from the projectile with a shield.
     */
    private final boolean shieldDefensible;

    /**
     * Determines if you can defend from the projectile with a cannon.
     */
    private final boolean fireDefensible;

    /**
     * Direction from which the projectile is coming from.
     */
    private final Direction direction;

    /**
     * Coordinate from which the projectile is coming from.
     */
    private int coord;

    /**
     * Private constructor, will be called by public factory methods to create specific projectiles
     */
    private Projectile(boolean bouncy, boolean shieldDefensible, boolean fireDefensible, Direction direction) {
        this.bouncy = bouncy;
        this.shieldDefensible = shieldDefensible;
        this.fireDefensible = fireDefensible;
        this.direction = direction;
    }

    /**
     * Valid for the next 4 methods
     * @param direction Direction from which the projectile is coming from
     * @return a Projectile type object with the specified parameters
     */
    public static Projectile createSmallMeteor(Direction direction) {
        return new Projectile(true, true, false, direction);
    }

    public static Projectile createLargeMeteor(Direction direction) {
        return new Projectile(false, false, true, direction);
    }

    public static Projectile createLightCannonFire(Direction direction) {
        return new Projectile(false, true, false, direction);
    }

    public static Projectile createHeavyCannonFire(Direction direction) {
        return new Projectile(false, false, false, direction);
    }

    /**
     * Valid for the next 4 methods
     * Getters of the attributes
     * @return the corresponding attribute value
     */
    public boolean isBouncy() {
        return bouncy;
    }

    public boolean isShieldDefensible() {
        return shieldDefensible;
    }

    public boolean isFireDefensible() {
        return fireDefensible;
    }

    public Direction getDirection() {
        return direction;
    }

    public String toVerboseString() {
        String type = "";
        if (bouncy && shieldDefensible && !fireDefensible) {
            type = "Small Meteor";
        } else if (!bouncy && !shieldDefensible && fireDefensible) {
            type = "Large Meteor";
        } else if (!bouncy && shieldDefensible && !fireDefensible) {
            type = "Light Cannon Fire";
        } else if (!bouncy && !shieldDefensible && !fireDefensible) {
            type = "Heavy Cannon Fire";
        }

        return type + " from " + direction.toVerboseString() + "]";
    }

    public String toEmojiString() {
        String type = "";
        if (bouncy && shieldDefensible && !fireDefensible) {
            type = "☄️"; // Small Meteor
        } else if (!bouncy && !shieldDefensible && fireDefensible) {
            type = "🌑"; // Large Meteor
        } else if (!bouncy && shieldDefensible && !fireDefensible) {
            type = "💥"; // Light Cannon Fire
        } else if (!bouncy && !shieldDefensible && !fireDefensible) {
            type = "🔥"; // Heavy Cannon Fire
        }

        return type + direction.toEmojiString(true) ;
    }

    public String toUnicodeString() {
        String type = "";
        if (bouncy && shieldDefensible && !fireDefensible) {
            type = "●"; // ● Small Meteor
        } else if (!bouncy && !shieldDefensible && fireDefensible) {
            type = "■"; // ■ Large Meteor
        } else if (!bouncy && shieldDefensible && !fireDefensible) {
            type = "*"; // ✹ Light Cannon Fire
        } else if (!bouncy && !shieldDefensible && !fireDefensible) {
            type = "⚡"; // ⚡ Heavy Cannon Fire
        }

        return type + direction.toEmojiString(true) ;
    }

    /**
     * Rolls two dies and assigns the value to coord variable
     */
    public void roll2D6 ()
    {
        Random rand = new Random();
        this.coord = 2 + rand.nextInt(6) + rand.nextInt(6);
    }

    public int getCoord() {
        return coord;
    }
}
