package it.polimi.ingsw.cards.warzone;

import it.polimi.ingsw.enums.PowerType;
import it.polimi.ingsw.player.Player;

public class WarCriteriaPower implements WarCriteria {

    private PowerType powerType;

    public WarCriteriaPower(PowerType powerType) {
        this.powerType = powerType;
    }

    @Override
    public int compare(Player p1, Player p2) {
        // TODO: ask the player to activate cannons/engines if they have not been asked to yet in this WarZone,
        //  then use the total fire/thrust power
        float delta = p2.getShipBoard().getVisitorCalculatePowers().getInfoPower(powerType).getBasePower() -
                p1.getShipBoard().getVisitorCalculatePowers().getInfoPower(powerType).getBasePower();
        if (delta == 0) {
            return Integer.compare(p2.getPosition(), p1.getPosition());
            // inverted because when a player has a better position it's like it has worse power (for equal powers)
        }
        return (delta > 0) ? 1 : -1;
    }
}
