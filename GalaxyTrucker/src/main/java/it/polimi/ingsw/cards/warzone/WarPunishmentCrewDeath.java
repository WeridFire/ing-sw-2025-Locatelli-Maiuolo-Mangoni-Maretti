package it.polimi.ingsw.cards.warzone;

import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.player.Player;

public class WarPunishmentCrewDeath implements WarPunishment {

    private final int crewAmount;

    public WarPunishmentCrewDeath(int crewAmount) {
        this.crewAmount = crewAmount;
    }

    @Override
    public void apply(Player player, GameData gameData) {
        // TODO: implement method so that it asks the player where they wants their crew to be removed.
    }
}