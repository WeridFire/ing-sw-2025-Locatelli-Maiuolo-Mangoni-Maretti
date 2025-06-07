package it.polimi.ingsw.cards.warzone;

import it.polimi.ingsw.enums.PowerType;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.shipboard.LoadableType;
import it.polimi.ingsw.shipboard.visitors.VisitorCalculatePowers;
import it.polimi.ingsw.task.customTasks.TaskActivateTiles;
import it.polimi.ingsw.task.customTasks.TaskRemoveLoadables;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class WarCriteriaPower implements WarCriteria {

    private PowerType powerType;
    private Map<Player, Float> powerMap;

    public WarCriteriaPower(PowerType powerType) {
        this.powerType = powerType;
    }

    @Override
    public void computeCriteria(GameData game, Consumer<Player> postCompute) {
        askPlayerForPowerActivation(game.getPlayersInFlight().getFirst(), game, postCompute);
    }

    public void askPlayerForPowerActivation(Player player, GameData game, Consumer<Player> next){
        if(player == null){
            //we have asked all players, yay, proceed to retrieving the worst player and call next.
            Player worstPlayer = game.getPlayersInFlight().stream()
                    .min(this).orElse(null);
            next.accept(worstPlayer);
            return;
        }

        game.getTaskStorage().addTask(
                new TaskActivateTiles(player.getUsername(), 30, this.powerType,
                (p, coordsToActivate) -> {
                    int batteriesAmount = coordsToActivate.size();
                    if(batteriesAmount <= 0){
                        powerMap.put(p, 0f);
                        //proceed to next player
                        askPlayerForPowerActivation(game.getNextPlayerInFlight(p), game, next);
                        return;
                    }
                    game.getTaskStorage().addTask(
                            new TaskRemoveLoadables(p,
                            30, Set.of(LoadableType.BATTERY), batteriesAmount,
                            (p1) -> {
                                VisitorCalculatePowers.CalculatorPowerInfo powerInfo = player.getShipBoard().getVisitorCalculatePowers().getInfoPower(PowerType.FIRE);
                                float activatedPower = (float) powerInfo.getLocationsToActivate().entrySet().stream()
                                        .filter(entry -> coordsToActivate.contains(entry.getKey()))
                                        .mapToDouble(Map.Entry::getValue)
                                        .sum();
                                this.powerMap.put(p1, activatedPower);
                                //proceed to next player
                                askPlayerForPowerActivation(game.getNextPlayerInFlight(p1), game, next);
                            }));
                }));
    }

    private float getTotalPower(Player player, float extraPower){

        VisitorCalculatePowers.CalculatorPowerInfo powerInfo = player.getShipBoard()
                                                                    .getVisitorCalculatePowers()
                                                                    .getInfoPower(this.powerType);
        float totalFirePower = powerInfo.getBasePower() + extraPower;
        totalFirePower += powerInfo.getBonus(totalFirePower);
        return totalFirePower;
    }


    @Override
    public int compare(Player p1, Player p2) {
        // Use getTotalPower with 0 extraPower for fair comparison
        float power1 = getTotalPower(p1, powerMap.get(p1));
        float power2 = getTotalPower(p2, powerMap.get(p2));

        if (power1 == power2) {
            // Invert order to break ties: higher order loses to lower order
            return Integer.compare(p2.getOrder(), p1.getOrder());
        }

        return Float.compare(power2, power1); // Descending order: higher power first
    }

}
