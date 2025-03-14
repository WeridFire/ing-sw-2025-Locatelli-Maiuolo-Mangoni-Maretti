package it.polimi.ingsw.cards;

import src.main.java.it.polimi.ingsw.enums.Direction;

public class Projectile {

    /**
     * Determines if the projectile bounces on flat sides of the ship.
     */
    private boolean bouncy;

    /**
     * Determines if you can defend from the projectile with a shield.
     */
    private boolean shieldDefendable;

    /**
     * Determines if you can defend from the projectile with a cannon.
     */
    private boolean fireDefendable;

    /**
     * Direction from which the projectile is coming from.
     */
    private Direction direction;

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
    private static Projectile createSmallMeteor(Direction direction) {
        return new Projectile(true, true, false, direction);
    }

    private static Projectile createBigMeteor(Direction direction) {
        return new Projectile(false, false, true, direction);
    }

    private static Projectile createSmallLaser(Direction direction) {
        return new Projectile(false, true, false, direction);
    }

    private static Projectile createBigLaser(Direction direction) {
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
}
