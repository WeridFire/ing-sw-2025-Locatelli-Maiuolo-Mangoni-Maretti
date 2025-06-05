package it.polimi.ingsw.shipboard.integrity;

import it.polimi.ingsw.player.Player;

import java.io.Serializable;
import java.util.function.Consumer;

public interface IShipIntegrityListener extends Serializable {
    void update(IntegrityProblem integrityProblem, Consumer<Player> postCheck);
}
