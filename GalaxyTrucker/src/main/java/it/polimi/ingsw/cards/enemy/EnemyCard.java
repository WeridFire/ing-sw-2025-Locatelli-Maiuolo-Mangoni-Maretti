package src.main.java.it.polimi.ingsw.cards.enemy;

import src.main.java.it.polimi.ingsw.GamesHandler;
import src.main.java.it.polimi.ingsw.cards.Card;
import src.main.java.it.polimi.ingsw.enums.PowerType;
import src.main.java.it.polimi.ingsw.player.Player;

import java.util.UUID;

public abstract class EnemyCard extends Card {

    /**
     * How many days are removed upon taking the prize of this enemy.
     */
    private int lostDays;
    /**
     * The fire power necessary to beat this enemy.
     */
    private int firePower;

    /**
     * @param firePower firepower of this enemy
     * @param lostDays days required to loot this enemy
     * @param textureName the texture of the card
     * @param level the level this card is part of
     * @param gameId the ID of the game this card is part of.
     */
    public EnemyCard(int firePower, int lostDays, String textureName, int level, UUID gameId) {
        super(textureName, level, gameId);
        this.firePower = firePower;
        this.lostDays = lostDays;
    }

    /**
     * Method to assign the loot of the defeated ship to a player
     * @param player player that is getting the loot
     */
    public abstract void givePrize(Player player);

    /**
     * Method to punish the player that gets defeated by this enemy
     * @param player player on which the method is currently acting upon
     */
    public abstract void applyPunishment(Player player);

    /**
     *
     * @return this enemy firepower
     */
    public int getFirePower() {
        return firePower;
    }

    /**
     *
     * @return necessary days to loot this enemy
     */
    public int getLostDays() {
        return lostDays;
    }

    @Override
    public void playEffect(UUID gameId) {
        for(Player p : GamesHandler.getInstance().getGame(gameId).getGameData().getPlayers()){
            //TODO: Implement logic of asking player what power they wanna use
            if(p.getShipBoard().getStatistics().getFreePower(PowerType.FIRE) > getFirePower()){
                givePrize(p);
                break;
            }else if(p.getShipBoard().getStatistics().getFreePower(PowerType.FIRE) < getFirePower()){
                applyPunishment(p);
            }
        }
    }
}
