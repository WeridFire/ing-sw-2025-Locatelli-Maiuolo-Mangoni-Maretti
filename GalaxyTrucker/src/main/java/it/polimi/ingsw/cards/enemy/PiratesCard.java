package it.polimi.ingsw.cards.enemy;

import it.polimi.ingsw.cards.projectile.Projectile;
import it.polimi.ingsw.enums.PowerType;
import it.polimi.ingsw.enums.Rotation;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.playerInput.PlayerTurnUtils;

import java.util.List;
import java.util.UUID;

public class PiratesCard extends EnemyCard {

    /**
     * The amount of cash bounty given to the player that defeats this pirate.
     */
    private final int prizeBounty;
    /**
     * The projectiles that punish the player when they are beaten by this pirate.
     */
    private final Projectile[] punishHits;

    public PiratesCard(int prizeBounty, Projectile[] punishHits, int firePower,
                       int lostDays, String textureName, int level){
		super(firePower, lostDays, textureName, level);
        this.prizeBounty = prizeBounty;
        this.punishHits = punishHits;

	}

    @Override
    public void givePrize(Player player, GameData game) {
        player.addCredits(prizeBounty);
        game.movePlayerBackward(player, getLostDays());
    }

    @Override
    public void applyPunishment(Player player, GameData game) {
        for(Projectile proj : punishHits){
            boolean defended = PlayerTurnUtils.runPlayerProjectileDefendRequest(player, proj, game);
            if(!defended){
                //hittt
            }

            //TODO: send projectile against shipboard based on activations.
        }
    }


}
