package src.main.java.it.polimi.ingsw.cards;

import src.main.java.it.polimi.ingsw.player.Player;

public class PiratesCard extends EnemyCard{

    /**
     * Bounty awarded to the player defeating the pirates
     */
    private final int prizeBounty;

    /**
     * Hits hitting the players that were defeated by the pirateship
     */
    private final Projectile[] projectile;

    public PiratesCard(int firePower, int lostDays, int prizeBounty, Projectile[] projectile) {
        super(firePower, lostDays);
        this.prizeBounty = prizeBounty;
        this.projectile = projectile;
    }

    @Override
    public void givePrize(Player player) {
        super.givePrize(player);
    }

    @Override
    public void applyPunishment(Player player) {
        super.applyPunishment(player);
    }
}
