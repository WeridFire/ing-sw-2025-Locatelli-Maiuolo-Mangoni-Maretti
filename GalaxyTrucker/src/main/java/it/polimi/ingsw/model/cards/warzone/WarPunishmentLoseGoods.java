package it.polimi.ingsw.model.cards.warzone;

import it.polimi.ingsw.model.game.GameData;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.view.cli.ANSI;

public class WarPunishmentLoseGoods implements WarPunishment {

    private final int lostGoods;

    public WarPunishmentLoseGoods(int lostGoods) {
        this.lostGoods = lostGoods;
    }

    @Override
    public String getDetails() {
        return "lose your " + ANSI.BACKGROUND_BLACK + ANSI.RED + lostGoods + " most valuable goods" + ANSI.RESET;
    }

    @Override
    public void apply(Player player, GameData gameData) {
        player.getShipBoard().loseBestGoods(lostGoods);
    }
}
