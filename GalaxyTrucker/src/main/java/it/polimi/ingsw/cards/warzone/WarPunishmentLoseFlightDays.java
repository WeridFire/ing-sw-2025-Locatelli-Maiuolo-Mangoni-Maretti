package it.polimi.ingsw.cards.warzone;

import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.player.Player;

public class WarPunishmentLoseFlightDays implements WarPunishment {

    private final int lostDays;

    public WarPunishmentLoseFlightDays(int lostDays) {
        this.lostDays = lostDays;
    }

    @Override
    public void apply(Player player, GameData gameData) {
        gameData.movePlayerBackward(player, lostDays);
    }
}
