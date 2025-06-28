package it.polimi.ingsw.model.cards.warzone;

import it.polimi.ingsw.model.cards.projectile.Projectile;
import it.polimi.ingsw.model.game.GameData;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.playerInput.PIRUtils;
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
    public void apply(Player player, GameData gameData) throws InterruptedException {
        for(Projectile proj : projectiles){
            PIRUtils.runProjectile(player, proj, gameData, false, "War Zone Projectiles");
        }
    }
}