package it.polimi.ingsw.cards.enemy;

import it.polimi.ingsw.cards.Card;
import it.polimi.ingsw.enums.PowerType;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.playerInput.PIRUtils;
import it.polimi.ingsw.playerInput.exceptions.TileNotAvailableException;
import it.polimi.ingsw.playerInput.exceptions.WrongPlayerTurnException;
import it.polimi.ingsw.shipboard.LoadableType;
import it.polimi.ingsw.shipboard.exceptions.NoTileFoundException;
import it.polimi.ingsw.shipboard.exceptions.OutOfBuildingAreaException;
import it.polimi.ingsw.shipboard.tiles.exceptions.NotEnoughItemsException;
import it.polimi.ingsw.shipboard.visitors.VisitorCalculatePowers;
import it.polimi.ingsw.task.customTasks.TaskActivateTiles;
import it.polimi.ingsw.task.customTasks.TaskRemoveLoadables;
import it.polimi.ingsw.view.cli.CLIFrame;
import it.polimi.ingsw.view.cli.CLIScreen;

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
     * Method to assign the loot of the defeated ship to a player
     * @param player player that is getting the loot
     */
    public abstract void givePrize(Player player, GameData game);

    /**
     * Method to punish the player that gets defeated by this enemy
     * @param player player on which the method is currently acting upon
     */
    public abstract void applyPunishment(Player player, GameData game) ;

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
    public void startCardBehaviour(GameData game) {
        playTask(game, game.getPlayersInFlight().getFirst());
    }

    public void playTask(GameData game, Player player) {
        if(player == null){
            game.getCurrentGamePhase().endPhase();
            return;
        }

        TaskActivateTiles task = new TaskActivateTiles(
                player.getUsername(),
                30,
                PowerType.FIRE,
                (p, coordinatesSet) -> {
                }
        );

        task.setOnFinish((p, coordinatesSet) -> {
            //After player has chosen which tiles to activate
            try {

                //activate tiles
                task.activateTiles(p, coordinatesSet);

                //remove batteries
                game.getTaskStorage().addTask(new TaskRemoveLoadables(
                        p,
                        30,
                        Set.of(LoadableType.BATTERY),
                        coordinatesSet.size(),
                        (p1) -> {

                        }
                ));

                //calculate total power (previously done by PIRs)
                //moved it here since facing an enemy is the only instance of the game
                //where it's needed to calculate the firepower
                VisitorCalculatePowers.CalculatorPowerInfo powerInfo = player.getShipBoard().getVisitorCalculatePowers().getInfoPower(PowerType.FIRE);

                //activated firepower
                float totalFirePower = (float) powerInfo.getLocationsToActivate().entrySet().stream()
                        .filter(entry -> coordinatesSet.contains(entry.getKey()))
                        .mapToDouble(Map.Entry::getValue)
                        .sum();

                //add base firepower
                totalFirePower += powerInfo.getBasePower();

                //add bonus if purple alien is present
                totalFirePower += powerInfo.getBonus(totalFirePower);

                if(totalFirePower > firePower){
                    givePrize(player, game);
                    game.getCurrentGamePhase().endPhase();
                }
                else if(totalFirePower == firePower)
                {
                    playTask(game, game.getNextPlayerInFlight(player));
                }
                else
                {
                    applyPunishment(player, game);
                    playTask(game, game.getNextPlayerInFlight(player));
                }

            } catch (WrongPlayerTurnException | NotEnoughItemsException | TileNotAvailableException e) {
                throw new RuntimeException(e);
            }
        });

        game.getTaskStorage().addTask(task);
    }



}
