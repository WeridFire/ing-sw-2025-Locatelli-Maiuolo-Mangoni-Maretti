package it.polimi.ingsw.controller.states;

import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.model.game.GameData;
import it.polimi.ingsw.network.messages.ClientUpdate;
import it.polimi.ingsw.model.player.Player;

/**
 * Static class, wrapper for useful info about the last update, therefore also about the current game state.<br>
 * From {@code this} and this subclasses, any {@link it.polimi.ingsw.view.IView IView} should retrieve enough info
 * when calculating the gfx elements to show after an internal
 * {@link it.polimi.ingsw.view.IView#onUpdate(ClientUpdate) IView.onUpdate(ClientUpdate)} call.
 */
public class CommonState {

    public static ClientUpdate getLastUpdate() {
        return State.getInstance().getLastUpdate();
    }

    public static Player getPlayer() {
        return getLastUpdate().getClientPlayer();
    }

    public static GameData getGameData() {
        return getLastUpdate().getCurrentGame();
    }

    public static boolean isCurrentPhase(GamePhaseType gamePhaseType) {
        return getGameData() != null && getGameData().getCurrentGamePhaseType() == gamePhaseType;
    }
}
