package it.polimi.ingsw.cards.enemy;

import it.polimi.ingsw.cards.projectile.Projectile;
import it.polimi.ingsw.enums.AnchorPoint;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.integrity.PlayerProjectileDefendUtils;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.playerInput.PIRs.PIRMultipleChoice;
import it.polimi.ingsw.playerInput.PIRUtils;
import it.polimi.ingsw.playerInput.PIRs.PIRYesNoChoice;
import it.polimi.ingsw.shipboard.exceptions.NoTileFoundException;
import it.polimi.ingsw.shipboard.exceptions.OutOfBuildingAreaException;
import it.polimi.ingsw.task.customTasks.TaskMultipleChoice;
import it.polimi.ingsw.task.customTasks.TaskYesNoChoice;
import it.polimi.ingsw.view.cli.ANSI;
import it.polimi.ingsw.view.cli.CLIFrame;

import java.util.ArrayList;
import java.util.List;

import static it.polimi.ingsw.view.cli.CLIScreen.getScreenFrame;

public class PiratesCard extends EnemyCard{

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
		super(firePower, lostDays, "PIRATES", textureName, level);
        this.prizeBounty = prizeBounty;
        this.punishHits = punishHits;

	}

    /**
     * New givePrize method using tasks
     * @param player the player who's getting the prize
     * @param gameData current game data
     */
    @Override
    public void givePrizeTask(Player player, GameData gameData)
    {
        gameData.getTaskStorage().addTask(new TaskYesNoChoice(
                player.getUsername(),
                30,
                "You will receive " + prizeBounty +" credits, but you will lose "
                        + getLostDays() + " days.",
                false,
                (p, choice) -> {
                    if(TaskYesNoChoice.isChoiceYes(choice)){
                        p.addCredits(prizeBounty);
                        gameData.movePlayerBackward(p, getLostDays());
                    }
                    gameData.getCurrentGamePhase().endPhase();
                }
        ));
    }

    @Override
    public void applyPunishmentTask(Player player, GameData game){
        handleProjectile(player, game, 0);
    }


    public void handleProjectile(Player player, GameData game, int projIdx){
        Projectile proj = null;
        if(projIdx >= 0 && projIdx < punishHits.length){
            proj = punishHits[projIdx];
        }
        if(proj == null){
            //We have finished all the hits of the player. Proceed to next.
            this.playTask(game, game.getNextPlayerInFlight(player));
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
                                                    this.handleProjectile(p1, game, projIdx+1);
                                                }else{
                                                    //player died. Go to next player
                                                    this.playTask(game, nextPlayer);
                                                }
                                            }
                                    );
								}else{
                                    this.handleProjectile(pl, game, projIdx+1);
                                }
                            }
                    );
                }
        ));
    }

    /**
     * Generates a CLI representation of the implementing object.
     *
     * @return A {@link CLIFrame} containing the CLI representation.
     */
    @Override
    public CLIFrame getCLIRepresentation(){
        /**
         * sembrano in obliquo i bordi ma è
         * perche è un commento
         *
         * +--------------+
         * |   PIRATES    |
         * | lost days: x |
         * | firepower: x |
         * | bounty: x    |
         * |              |
         * | hits:        |
         * | ............ |
         * | ............ |
         * +--------------+
         * */

        List<String> cardInfoLines = new ArrayList<>();
        cardInfoLines.add(
                ANSI.BLACK + "Lost days: " + getLostDays() + ANSI.RESET
        );
        cardInfoLines.add(
                ANSI.BLACK + "Firepower: " + getFirePower() + ANSI.RESET
        );
        cardInfoLines.add(
                ANSI.BLACK + "Bounty: " + prizeBounty + ANSI.RESET
        );
        cardInfoLines.add(
                " "
        );
        cardInfoLines.add(
                ANSI.BLACK + "hits: " + ANSI.RESET
        );

        StringBuilder line = new StringBuilder();
        for (int i = 0; i < punishHits.length; i++) {
            if (i % 2 == 0 && i != 0) {
                cardInfoLines.add(ANSI.BLACK + line.toString());
                line = new StringBuilder();
            }
            line.append(punishHits[i].toUnicodeString()).append("  ");
        }
        cardInfoLines.add(ANSI.BLACK + line.toString());

        CLIFrame infoFrame = new CLIFrame(cardInfoLines.toArray(new String[0]));

        return super.getCLIRepresentation()
                .merge(infoFrame, AnchorPoint.CENTER, AnchorPoint.CENTER, 0, 0);
    }

}
