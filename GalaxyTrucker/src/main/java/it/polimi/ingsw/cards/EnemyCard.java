package it.polimi.ingsw.cards;

import src.main.java.it.polimi.ingsw.cards.Card;

public abstract class EnemyCard extends Card {

    /**
     * This enemy firepower
     */
    private int firePower;

    /**
     * Days lost looting the wreckage of your enemy's ship
     */
    private int lostDays;

    /**
     * @param firePower firepower of this enemy
     * @param lostDays days required to loot this enemy
     */
    public EnemyCard(int firePower, int lostDays) {
        this.firePower = firePower;
        this.lostDays = lostDays;
    }

    /**
     * Method to assign the loot of the defeated ship to a player
     * !!!Needs Implementation!!!
     * @param player player that is getting the loot
     */
    public void givePrize(Player player){
    }

    /**
     * Method to punish the player that gets defeated by this enemy
     * !!!Needs Implementation!!!
     * @param player player on which the method is currently acting upon
     */
    public void applyPunishment(Player player){
    }

    /**
     *
     * @return this enemy firepower
     */
    public int getFirePower() {
        return firePower;
    }

    /**
     * Contains the card effect
     * !!!Needs Implementation!!!
     */
    public void playEffect(){
    }
}
