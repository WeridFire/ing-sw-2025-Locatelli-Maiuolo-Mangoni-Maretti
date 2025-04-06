package it.polimi.ingsw.cards.projectile;

import it.polimi.ingsw.enums.Direction;

public class Projectile {

    /**
     * Determines if the projectile bounces on flat sides of the ship.
     */
    private final boolean bouncy;

    /**
     * Determines if you can defend from the projectile with a shield.
     */
    private final boolean shieldDefendable;

    /**
     * Determines if you can defend from the projectile with a cannon.
     */
    private final boolean fireDefendable;

    /**
     * Direction from which the projectile is coming from.
     */
    private final Direction direction;

    /**
     * Private constructor, will be called by public factory methods to create specific projectiles
     */
    private Projectile(boolean bouncy, boolean shieldDefendable, boolean fireDefendable, Direction direction) {
        this.bouncy = bouncy;
        this.shieldDefendable = shieldDefendable;
        this.fireDefendable = fireDefendable;
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

    public boolean isShieldDefendable() {
        return shieldDefendable;
    }

    public boolean isFireDefendable() {
        return fireDefendable;
    }

    public Direction getDirection() {
        return direction;
    }

    public String toVerboseString() {
        String type = "";
        if (bouncy && shieldDefendable && !fireDefendable) {
            type = "Small Meteor";
        } else if (!bouncy && !shieldDefendable && fireDefendable) {
            type = "Large Meteor";
        } else if (!bouncy && shieldDefendable && !fireDefendable) {
            type = "Light Cannon Fire";
        } else if (!bouncy && !shieldDefendable && !fireDefendable) {
            type = "Heavy Cannon Fire";
        }

        return type + " from " + direction.toVerboseString() + "]";
    }

    public String toEmogiString() {
        String type = "";
        if (bouncy && shieldDefendable && !fireDefendable) {
            type = "☄️"; // Small Meteor
        } else if (!bouncy && !shieldDefendable && fireDefendable) {
            type = "🌑"; // Large Meteor
        } else if (!bouncy && shieldDefendable && !fireDefendable) {
            type = "💥"; // Light Cannon Fire
        } else if (!bouncy && !shieldDefendable && !fireDefendable) {
            type = "🔥"; // Heavy Cannon Fire
        }

        return type + direction.toEmogiString() ;
    }
}
