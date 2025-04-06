package it.polimi.ingsw.cards.warzone;

import it.polimi.ingsw.cards.projectile.Projectile;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.player.Player;

public class WarPunishmentProjectile implements WarPunishment {

    private final Projectile[] projectiles;

    public WarPunishmentProjectile(Projectile[] projectiles) {
        this.projectiles = projectiles;
    }

    @Override
    public void apply(Player player, GameData gameData) {
        // TODO: implement hitting the player accordingly to the projectiles.
    }
}