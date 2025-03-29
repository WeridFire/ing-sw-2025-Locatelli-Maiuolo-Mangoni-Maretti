package src.main.java.it.polimi.ingsw.cards.enemy;

import src.main.java.it.polimi.ingsw.GamesHandler;
import src.main.java.it.polimi.ingsw.cards.Card;
import src.main.java.it.polimi.ingsw.enums.PowerType;
import src.main.java.it.polimi.ingsw.game.GameData;
import src.main.java.it.polimi.ingsw.player.Player;
import src.main.java.it.polimi.ingsw.playerInput.PlayerActivateTilesRequest;
import src.main.java.it.polimi.ingsw.playerInput.PlayerRemoveLoadableRequest;
import src.main.java.it.polimi.ingsw.playerInput.PlayerTurnUtils;
import src.main.java.it.polimi.ingsw.shipboard.LoadableType;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public abstract class EnemyCard extends Card {

    /**
     * How many days are removed upon taking the prize of this enemy.
     */
    private int lostDays;
    /**
     * The firepower necessary to beat this enemy.
     */
    private int firePower;

    /**
     * @param firePower firepower of this enemy
     * @param lostDays days required to loot this enemy
     * @param textureName the texture of the card
     * @param level the level this card is part of
     */
    public EnemyCard(int firePower, int lostDays, String textureName, int level) {
        super(textureName, level);
        this.firePower = firePower;
        this.lostDays = lostDays;
    }

    /**
     * Method to assign the loot of the defeated ship to a player
     * @param player player that is getting the loot
     */
    public abstract void givePrize(Player player, GameData game);

    /**
     * Method to punish the player that gets defeated by this enemy
     * @param player player on which the method is currently acting upon
     */
    public abstract void applyPunishment(Player player, GameData game);

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
    public void playEffect(GameData game) {
        for(Player p : game.getPlayers()){
            float totalFirePower = PlayerTurnUtils.runPlayerPowerTilesActivationInteraction(p, game, PowerType.FIRE);
            if(totalFirePower > getFirePower()){
                givePrize(p, game);
                break;
            }else if(totalFirePower < getFirePower()) {
                applyPunishment(p, game);
            }
        }
    }
}
