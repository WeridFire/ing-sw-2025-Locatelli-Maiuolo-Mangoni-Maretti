package src.main.java.it.polimi.ingsw.cards.enemy;

import src.main.java.it.polimi.ingsw.cards.projectile.Projectile;
import src.main.java.it.polimi.ingsw.enums.PowerType;
import src.main.java.it.polimi.ingsw.game.GameData;
import src.main.java.it.polimi.ingsw.player.Player;
import src.main.java.it.polimi.ingsw.playerInput.PlayerTurnUtils;

import java.util.List;
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
        for(Projectile p : punishHits){
            List<Boolean> protectedSides = PlayerTurnUtils.runPlayerShieldsActivationInteraction(player, game);
            //TODO: send projectile against shipboard based on activations.
        }
    }


}
