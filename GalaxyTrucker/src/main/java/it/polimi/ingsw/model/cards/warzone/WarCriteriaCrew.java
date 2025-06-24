package it.polimi.ingsw.model.cards.warzone;

import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.shipboard.LoadableType;
import it.polimi.ingsw.view.cli.ANSI;

public class WarCriteriaCrew implements WarCriteria {

    @Override
    public String getName() {
        return "Fewest " + ANSI.BACKGROUND_BLACK + ANSI.RED + "Crew Figures" + ANSI.RESET;
    }

    @Override
    public int compare(Player p1, Player p2) {
        int crew1 = p1.getShipBoard().getVisitorCalculateCargoInfo().getCrewInfo().countAll(LoadableType.CREW_SET);
        int crew2 = p2.getShipBoard().getVisitorCalculateCargoInfo().getCrewInfo().countAll(LoadableType.CREW_SET);
        return Integer.compare(crew1, crew2);
    }
}
