package it.polimi.ingsw.controller.states;

import it.polimi.ingsw.cards.CardsGroup;
import it.polimi.ingsw.cards.exceptions.CardsGroupException;
import it.polimi.ingsw.shipboard.tiles.TileSkeleton;

import java.util.List;
import java.util.Optional;

public class AssembleState extends CommonState {

    private static TileSkeleton tileInHand = null;

    /**
     * update tile in hand if different from previous
     */
    private static void updateTileInHand() {
        if ((tileInHand == null) || (!tileInHand.equals(getPlayer().getTileInHand()))) {
            // note: in here also if both are null, but in that case nothing happens -> no problem
            tileInHand = getPlayer().getTileInHand();
        }
    }

    public static TileSkeleton getTileInHand() {
        updateTileInHand();
        return tileInHand;
    }

    public static boolean isEndedAssembly() {
        return getPlayer().getShipBoard().isEndedAssembly();
    }
    public static boolean isPlacedOneTile() {
        return getPlayer().getShipBoard().getTiles().size() > 1;  // 1 is the main cabin so needs to be > not >=
    }
    public static boolean isShipboardFilled() {
        return getPlayer().getShipBoard().isFilled();
    }
    public static Optional<CardsGroup> getCardGroupInHand() {
        Integer cgIndex = getPlayer().getCardGroupInHand();
        if (cgIndex == null) return Optional.empty();
        else {
            try {
                return Optional.ofNullable(getGameData().getDeck().getGroup(cgIndex));
            } catch (CardsGroupException e) {
                throw new RuntimeException(e);  // should never happen -> runtime exception
            }
        }
    }
    public static String getOccupiedHandMessage() {
        if (getTileInHand() != null) {
            return "tile";
        }
        if (getCardGroupInHand().isPresent()) {
            return "group of cards";
        }
        return null;
    }

    public static List<Boolean> listOfAvailableCardGroups() {
        return getGameData().getDeck().getGroupsAvailability();
    }
}
