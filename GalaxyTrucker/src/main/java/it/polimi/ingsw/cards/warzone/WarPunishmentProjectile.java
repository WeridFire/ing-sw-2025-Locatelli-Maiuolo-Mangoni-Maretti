package it.polimi.ingsw.cards.warzone;

import it.polimi.ingsw.cards.projectile.Projectile;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.playerInput.PIRUtils;
import it.polimi.ingsw.playerInput.PIRs.PIRMultipleChoice;
import it.polimi.ingsw.shipboard.exceptions.NoTileFoundException;
import it.polimi.ingsw.shipboard.exceptions.OutOfBuildingAreaException;

public class WarPunishmentProjectile implements WarPunishment {

    private final Projectile[] projectiles;

    public WarPunishmentProjectile(Projectile[] projectiles) {
        this.projectiles = projectiles;
    }

    @Override
    public void apply(Player player, GameData gameData) {
        for(Projectile proj : projectiles){

            gameData.getPIRHandler().setAndRunTurn(new PIRMultipleChoice(
                    player,
                    30,
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