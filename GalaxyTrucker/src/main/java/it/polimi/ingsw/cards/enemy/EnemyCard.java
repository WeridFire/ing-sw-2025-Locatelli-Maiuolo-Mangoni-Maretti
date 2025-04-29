package it.polimi.ingsw.cards.enemy;

import it.polimi.ingsw.cards.Card;
import it.polimi.ingsw.enums.PowerType;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.playerInput.PIRUtils;
import it.polimi.ingsw.shipboard.exceptions.NoTileFoundException;
import it.polimi.ingsw.shipboard.exceptions.OutOfBuildingAreaException;
import it.polimi.ingsw.view.cli.CLIFrame;
import it.polimi.ingsw.view.cli.CLIScreen;

public abstract class EnemyCard extends Card {

    /**
     * How many days are removed upon taking the prize of this enemy.
     */
    private final int lostDays;
    /**
     * The firepower necessary to beat this enemy.
     */
    private final int firePower;

    /**
     * @param firePower firepower of this enemy
     * @param lostDays days required to loot this enemy
     * @param title the name of the card
     * @param textureName the texture of the card
     * @param level the level this card is part of
     */
    public EnemyCard(int firePower, int lostDays, String title, String textureName, int level) {
        super(title, textureName, level);
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
    public abstract void applyPunishment(Player player, GameData game) throws NoTileFoundException, OutOfBuildingAreaException;

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
    public void playEffect(GameData game) throws NoTileFoundException, OutOfBuildingAreaException {
        for(Player p : game.getPlayers()){
            float totalFirePower = PIRUtils.runPlayerPowerTilesActivationInteraction(p, game, PowerType.FIRE);
            if(totalFirePower > getFirePower()){
                givePrize(p, game);
                break;
            }else if(totalFirePower < getFirePower()) {
                applyPunishment(p, game);
            }
        }
    }
}
