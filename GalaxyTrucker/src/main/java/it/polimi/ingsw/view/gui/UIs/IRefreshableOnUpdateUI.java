package it.polimi.ingsw.view.gui.UIs;

import it.polimi.ingsw.network.messages.ClientUpdate;

public interface IRefreshableOnUpdateUI {
    void refreshOnUpdate(ClientUpdate update);
}
