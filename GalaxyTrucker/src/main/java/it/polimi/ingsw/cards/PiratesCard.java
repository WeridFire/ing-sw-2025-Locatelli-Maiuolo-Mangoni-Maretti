package src.main.java.it.polimi.ingsw.cards;

import src.main.java.it.polimi.ingsw.GamesHandler;
import src.main.java.it.polimi.ingsw.cards.projectile.Projectile;
import src.main.java.it.polimi.ingsw.enums.PowerType;
import src.main.java.it.polimi.ingsw.game.Game;
import src.main.java.it.polimi.ingsw.player.Player;

import java.util.UUID;

public class PiratesCard extends EnemyCard{

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
    }

    @Override
    public void applyPunishment(Player player) {
        //TO BE DONE
    }

    /**
     * Iterates through each player and checks if they can beat the pirate. If so awards the prize and stops.
     * If not applies punishment to player and proceeds to the next player.
     * @param gameId The UUID of the game associated to this card, to access the game handler.
     */
    @Override
    public void playEffect(UUID gameId) {
        for(Player p : GamesHandler.getInstance().getGame(gameId).getGameData().getPlayers()){
            //TBD Implement logic of asking player what power they wanna use
            if(p.getShipBoard().getStatistics().getFreePower(PowerType.FIRE) > getFirePower()){
                givePrize(p);
                break;
            }else if(p.getShipBoard().getStatistics().getFreePower(PowerType.FIRE) < getFirePower()){
                applyPunishment(p);
            }
        }
    }
}
