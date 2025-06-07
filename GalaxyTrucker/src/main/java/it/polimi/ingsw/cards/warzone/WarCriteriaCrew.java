package it.polimi.ingsw.cards.warzone;

import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.shipboard.LoadableType;

import java.util.function.Consumer;

public class WarCriteriaCrew implements WarCriteria {
    
    @Override
    public int compare(Player p1, Player p2) {
        int crew1 = p1.getShipBoard().getVisitorCalculateCargoInfo().getCrewInfo().countAll(LoadableType.CREW_SET);
        int crew2 = p2.getShipBoard().getVisitorCalculateCargoInfo().getCrewInfo().countAll(LoadableType.CREW_SET);
        return Integer.compare(crew1, crew2);
    }

    @Override
    public void computeCriteria(GameData game, Consumer<Player> postCompute) {
        Player p = game.getPlayersInFlight().stream()
                .min(this).orElse(null);
        postCompute.accept(p);
    }
}
