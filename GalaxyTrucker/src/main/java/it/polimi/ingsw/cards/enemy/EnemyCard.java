package it.polimi.ingsw.cards.enemy;

import it.polimi.ingsw.cards.Card;
import it.polimi.ingsw.enums.PowerType;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.shipboard.LoadableType;
import it.polimi.ingsw.shipboard.visitors.VisitorCalculatePowers;
import it.polimi.ingsw.task.customTasks.TaskActivateTiles;
import it.polimi.ingsw.task.customTasks.TaskRemoveLoadables;

import java.util.Map;
import java.util.Set;

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
     * New method using tasks
     * @param player player on which the method is currently acting upon
     */
    public abstract void applyPunishmentTask(Player player, GameData game);

    /**
     * New method using tasks
     * @param player player that is getting the loot
     */
     public abstract void givePrizeTask(Player player, GameData game);

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
    public void startCardBehaviour(GameData game)    {
        playTask(game, game.getPlayersInFlight().getFirst());
    }


    private void fightWithExtraPower(GameData game, Player player, float extraPower){
        VisitorCalculatePowers.CalculatorPowerInfo powerInfo = player.getShipBoard().getVisitorCalculatePowers().getInfoPower(PowerType.FIRE);
        float totalFirePower = powerInfo.getBasePower() + extraPower;
        totalFirePower += powerInfo.getBonus(totalFirePower);
        if(totalFirePower > getFirePower()){
            givePrizeTask(player, game);
        }else if (totalFirePower == getFirePower()){
            playTask(game, game.getNextPlayerInFlight(player));
        }else{
            /**
             * Call to next tasks for next player is supposedly handled inside of the call.
             */
            applyPunishmentTask(player, game);
        }
    }


    public void playTask(GameData game, Player player){
        if(player == null){
            game.getCurrentGamePhase().endPhase();
            return;
        }

        game.getTaskStorage().addTask(new TaskActivateTiles(player.getUsername(), 30, PowerType.FIRE,
                (p, coordsToActivate) -> {
                    int batteriesAmount = coordsToActivate.size();
                    if(batteriesAmount <= 0){
                        fightWithExtraPower(game, p, 0f);
                        return;
                    }
                    game.getTaskStorage().addTask(new TaskRemoveLoadables(p,
                            30, Set.of(LoadableType.BATTERY), batteriesAmount,
                            (p1) -> {
                                VisitorCalculatePowers.CalculatorPowerInfo powerInfo = player.getShipBoard().getVisitorCalculatePowers().getInfoPower(PowerType.FIRE);
                                float activatedPower = (float) powerInfo.getLocationsToActivate().entrySet().stream()
                                        .filter(entry -> coordsToActivate.contains(entry.getKey()))
                                        .mapToDouble(Map.Entry::getValue)
                                        .sum();
                                fightWithExtraPower(game, p1, activatedPower);
                    }));
                }));
    }



}
