package it.polimi.ingsw.model.cards.warzone;

import it.polimi.ingsw.model.game.GameData;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.view.cli.ANSI;

public class WarPunishmentCrewDeath implements WarPunishment {

    private final int crewAmount;

    public WarPunishmentCrewDeath(int crewAmount) {
        this.crewAmount = crewAmount;
    }

    @Override
    public String getDetails() {
        return "lose " + ANSI.BACKGROUND_BLACK + ANSI.RED + crewAmount + " crew members" + ANSI.RESET;
    }

    @Override
    public void apply(Player player, GameData gameData) {
        // TODO: implement method so that it asks the player where they wants their crew to be removed.
        System.out.println("Execution of " + crewAmount + " crew members for player " + player.getUsername());
        player.getShipBoard().loseCrew(crewAmount);
    }
}