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
        float delta = p2.getShipBoard().getVisitorCalculateCargoInfo().getCrewInfo().countAll(LoadableType.CREW_SET) -
                p1.getShipBoard().getVisitorCalculateCargoInfo().getCrewInfo().countAll(LoadableType.CREW_SET);
        if (delta == 0) {
            return Integer.compare(p1.getOrder(), p2.getOrder());
            // order and not position because when a player has a
            // better position it's like it has worse power (for equal powers)
        }
        return (delta > 0) ? 1 : -1;
    }
}
