package it.polimi.ingsw.cards.warzone;

import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.player.Player;

import java.util.function.Consumer;

public class WarPunishmentLoseGoods implements WarPunishment {

    private final int lostGoods;

    public WarPunishmentLoseGoods(int lostGoods) {
        this.lostGoods = lostGoods;
    }

    @Override
    public void apply(Player player, GameData gameData, Consumer<Player> onFinish) {
        player.getShipBoard().loseBestGoods(lostGoods);
        onFinish.accept(player);
    }
}
