package src.main.java.it.polimi.ingsw.cards.enemy;

import src.main.java.it.polimi.ingsw.cards.projectile.Projectile;
import src.main.java.it.polimi.ingsw.player.Player;

import java.util.UUID;

public class PiratesCard extends EnemyCard {

    /**
     * The amount of cash bounty given to the player that defeats this pirate.
     */
    private int prizeBounty;
    /**
     * The projectiles that punish the player when they are beaten by this pirate.
     */
    private Projectile[] punishHits;

    public PiratesCard(int prizeBounty, Projectile[] punishHits, int firePower,
                       int lostDays, String textureName, int level, UUID gameId){
		super(firePower, lostDays, textureName, level, gameId);
        this.prizeBounty = prizeBounty;
        this.punishHits = punishHits;

	}

    @Override
    public void givePrize(Player player) {
        player.addCredits(prizeBounty);
        movePlayer(player, getLostDays());
    }

    @Override
    public void applyPunishment(Player player) {
        //TODO: hit player with projectile
    }


}
