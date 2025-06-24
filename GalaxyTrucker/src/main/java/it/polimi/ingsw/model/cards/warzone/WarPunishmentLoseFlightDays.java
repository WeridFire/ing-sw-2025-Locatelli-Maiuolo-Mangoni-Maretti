package it.polimi.ingsw.model.cards.warzone;

import it.polimi.ingsw.model.game.GameData;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.view.cli.ANSI;

public class WarPunishmentLoseFlightDays implements WarPunishment {

    private final int lostDays;

    public WarPunishmentLoseFlightDays(int lostDays) {
        this.lostDays = lostDays;
    }

    @Override
    public String getDetails() {
        return "lose " + ANSI.BACKGROUND_BLACK + ANSI.RED + lostDays + " flight days" + ANSI.RESET;
    }

    @Override
    public void apply(Player player, GameData gameData) {
        gameData.movePlayerBackward(player, lostDays);
    }
}
