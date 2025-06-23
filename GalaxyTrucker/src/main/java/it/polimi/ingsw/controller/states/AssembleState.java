package it.polimi.ingsw.controller.states;

import it.polimi.ingsw.model.cards.CardsGroup;
import it.polimi.ingsw.model.cards.exceptions.CardsGroupException;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.model.gamePhases.AssembleGamePhase;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.shipboard.ShipBoard;
import it.polimi.ingsw.model.shipboard.tiles.TileSkeleton;

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

    public static boolean isSpectatingOther() {
        return !getPlayer().getSpectating().equals(getPlayer().getUsername());
    }
    public static Player getSpectatedPlayer() {
        String spectatedPlayerUsername = getPlayer().getSpectating();
        return getGameData().getPlayer(p ->
                p.getUsername().equals(spectatedPlayerUsername), getPlayer());
    }

    public static ShipBoard getSpectatedShipBoard() {
        return getSpectatedPlayer().getShipBoard();
    }

    public static TileSkeleton[] getSpectatedReservedTiles() {
        return getSpectatedPlayer().getReservedTiles().toArray(new TileSkeleton[0]);
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

    public static boolean isTileReserved(TileSkeleton tile) {
        if (tile == null) return false;
        return getPlayer().getReservedTiles().contains(tile)
                || (getPlayer().isTileInHandFromReserved() && tile.equals(getPlayer().getTileInHand()));
    }

    public static List<Integer> getFinishAvailableIndexes() {
        return getGameData().getAvailableStartingPositionIndexes();
    }

    public static boolean isTimerRunning() {
        if (getGameData().getCurrentGamePhaseType() != GamePhaseType.ASSEMBLE) {
            return false;
        }
        // else: in assemble can cast current game phase
        return ((AssembleGamePhase) getGameData().getCurrentGamePhase()).isTimerRunning();
    }

    public static Integer getTimerSlotIndex() {
        if (getGameData().getCurrentGamePhaseType() != GamePhaseType.ASSEMBLE) {
            return null;
        }
        // else: in assemble can cast current game phase
        return ((AssembleGamePhase) getGameData().getCurrentGamePhase()).getTimerSlotIndex();
    }
}
