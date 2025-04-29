package it.polimi.ingsw.cards.enemy;

import it.polimi.ingsw.cards.projectile.Projectile;
import it.polimi.ingsw.enums.AnchorPoint;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.playerInput.PIRs.PIRMultipleChoice;
import it.polimi.ingsw.playerInput.PIRUtils;
import it.polimi.ingsw.playerInput.PIRs.PIRYesNoChoice;
import it.polimi.ingsw.shipboard.exceptions.NoTileFoundException;
import it.polimi.ingsw.shipboard.exceptions.OutOfBuildingAreaException;
import it.polimi.ingsw.view.cli.ANSI;
import it.polimi.ingsw.view.cli.CLIFrame;
import it.polimi.ingsw.view.cli.CLIScreen;

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

    @Override
    public void givePrize(Player player, GameData gameData) {
        PIRYesNoChoice pirYesOrNoChoice = new PIRYesNoChoice(player,
                                    30,
                                "You will receive " + prizeBounty +" credits, but you will lose "
                                             + getLostDays() + " days.",
                                true);
        boolean wantToAccept = gameData.getPIRHandler().setAndRunTurn(pirYesOrNoChoice);
        if(wantToAccept){
            player.addCredits(prizeBounty);
            gameData.movePlayerBackward(player, getLostDays());
        }
    }

    @Override
    public void applyPunishment(Player player, GameData game) {
        for(Projectile proj : punishHits){

            game.getPIRHandler().setAndRunTurn(new PIRMultipleChoice(
                    player,
                    30,
                    "Do you want to roll the dice?",
					new String[]{"Yes"},
                    0
            ));

            proj.roll2D6();
            boolean defended = PIRUtils.runPlayerProjectileDefendRequest(player, proj, game);
            if(!defended){
                try {
                    player.getShipBoard().hit(proj.getDirection(), proj.getCoord());
                } catch (NoTileFoundException | OutOfBuildingAreaException e) {
                    throw new RuntimeException(e);  // should never happen -> runtime exception
                }
            }
        }
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
