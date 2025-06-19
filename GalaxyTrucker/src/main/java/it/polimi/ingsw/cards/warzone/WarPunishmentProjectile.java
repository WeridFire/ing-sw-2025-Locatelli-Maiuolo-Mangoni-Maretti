package it.polimi.ingsw.cards.warzone;

import it.polimi.ingsw.cards.projectile.Projectile;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.integrity.PlayerProjectileDefendUtils;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.playerInput.PIRUtils;
import it.polimi.ingsw.playerInput.PIRs.PIRMultipleChoice;
import it.polimi.ingsw.task.customTasks.TaskMultipleChoice;

import java.util.function.Consumer;

public class WarPunishmentProjectile implements WarPunishment {

    private final Projectile[] projectiles;

    public WarPunishmentProjectile(Projectile[] projectiles) {
        this.projectiles = projectiles;
    }


    private void handleProjectile(Player player, GameData game, int projIdx, Consumer<Player> onFinish){
        Projectile proj = null;
        if(projIdx >= 0 && projIdx < projectiles.length){
            proj = projectiles[projIdx];
        }
        if(proj == null){
            onFinish.accept(player);
            return;
        }
        Projectile currentProjectile = proj;
        game.getTaskStorage().addTask(new TaskMultipleChoice(
                player.getUsername(),
                30,
                "Do you want to roll the dice?",
                new String[]{"Yes"},
                0,
                (p, c) -> {
                    currentProjectile.roll2D6();
                    PlayerProjectileDefendUtils.runPlayerProjectileDefendRequest(
                            p,
                            currentProjectile,
                            game,
                            (pl, defended) -> {
                                //After projectile interactions are performed (whether the player wants to defend or no)
                                if(!defended){
                                    Player nextPlayer = game.getNextPlayerInFlight(pl);
                                    player.getShipBoard().hit(
                                            currentProjectile.getDirection(),
                                            currentProjectile.getCoord(),
                                            pl,
                                            (p1) -> {
                                                if(!p1.isEndedFlight()){
                                                    //player survived, continue hits.
                                                    this.handleProjectile(p1, game, projIdx+1, onFinish);
                                                }else{
                                                    //player died. Proceed to next war level by calling on finish.
                                                    onFinish.accept(p1);
                                                }
                                            }
                                    );
                                }else{
                                    //next projectile
                                    this.handleProjectile(pl, game, projIdx+1, onFinish);
                                }
                            }
                    );
                }
        ));
    }



    @Override
    public void apply(Player player, GameData gameData, Consumer<Player> onFinish) {
        handleProjectile(player, gameData, 0, onFinish);
    }
}