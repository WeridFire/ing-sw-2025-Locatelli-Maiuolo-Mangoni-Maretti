package it.polimi.ingsw.cards.enemy;

import it.polimi.ingsw.cards.projectile.Projectile;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.playerInput.PIRs.PIRMultipleChoice;
import it.polimi.ingsw.playerInput.PIRUtils;
import it.polimi.ingsw.playerInput.PIRs.PIRYesNoChoice;

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
        PIRYesNoChoice pirYesOrNoChoice = new PIRYesNoChoice(player,
                                    30,
                                "You will receive " + prizeBounty +" credits, but you will lose "
                                             + getLostDays() + " days.",
                                true);
        boolean wantToAccept = game.getPIRHandler().setAndRunTurn(pirYesOrNoChoice);
        if(wantToAccept){
            player.addCredits(prizeBounty);
            game.movePlayerBackward(player, getLostDays());
        }
    }

    @Override
    public void applyPunishment(Player player, GameData game) {
        for(Projectile proj : punishHits){
            //TODO: make player roll coordinates
            boolean defended = PIRUtils.runPlayerProjectileDefendRequest(player, proj, game);
            if(!defended){
                //TODO: HIT PLAYER
            }
        }
    }


}
