package it.polimi.ingsw.model.cards.warzone;

import it.polimi.ingsw.model.cards.projectile.Projectile;
import it.polimi.ingsw.model.game.GameData;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.playerInput.PIRUtils;
import it.polimi.ingsw.model.playerInput.PIRs.PIRMultipleChoice;
import it.polimi.ingsw.model.shipboard.exceptions.NoTileFoundException;
import it.polimi.ingsw.model.shipboard.exceptions.OutOfBuildingAreaException;
import it.polimi.ingsw.util.Default;
import it.polimi.ingsw.view.cli.ANSI;

public class WarPunishmentProjectile implements WarPunishment {

    private final Projectile[] projectiles;

    public WarPunishmentProjectile(Projectile[] projectiles) {
        this.projectiles = projectiles;
    }

    @Override
    public String getDetails() {
        StringBuilder details = new StringBuilder().append(ANSI.BACKGROUND_BLACK + ANSI.RED);
        for (Projectile p: projectiles) {
            details.append(p.toUnicodeString()).append("  ");
        }
        return details.toString().stripTrailing() + ANSI.RESET;
    }

    @Override
    public void apply(Player player, GameData gameData) {
        for(Projectile proj : projectiles){

            gameData.getPIRHandler().setAndRunTurn(new PIRMultipleChoice(
                    player,
                    Default.PIR_SECONDS,
                    "Do you want to roll the dice?",
                    new String[]{"Yes"},
                    0
            ));

            proj.roll2D6();
            boolean defended = PIRUtils.runPlayerProjectileDefendRequest(player, proj, gameData);
            if(!defended) {
                try {
                    player.getShipBoard().hit(proj.getDirection(), proj.getCoord());
                } catch (NoTileFoundException | OutOfBuildingAreaException e) {
                    throw new RuntimeException(e);  // should never happen -> runtime exception
                }
            }
        }
    }
}