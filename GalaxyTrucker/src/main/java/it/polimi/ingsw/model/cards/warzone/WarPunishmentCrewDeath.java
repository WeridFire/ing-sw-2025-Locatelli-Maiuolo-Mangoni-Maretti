package it.polimi.ingsw.model.cards.warzone;

import it.polimi.ingsw.model.game.GameData;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.playerInput.PIRs.PIRDelay;
import it.polimi.ingsw.model.playerInput.PIRs.PIRRemoveLoadables;
import it.polimi.ingsw.model.shipboard.LoadableType;
import it.polimi.ingsw.util.Default;
import it.polimi.ingsw.view.cli.ANSI;

import java.util.Set;

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

        PIRDelay pirDelay = new PIRDelay(player, Default.PIR_SHORT_SECONDS,
                "You lost in the WarZone! You need to remove" + crewAmount + "of your crew from the ship", null
                );
        gameData.getPIRHandler().setAndRunTurn(
                pirDelay
        );
        PIRRemoveLoadables pirRemoveLoadables = new PIRRemoveLoadables(player, Default.PIR_SECONDS, LoadableType.CREW_SET, crewAmount);
        gameData.getPIRHandler().setAndRunTurn(
                pirRemoveLoadables
        );
    }
}