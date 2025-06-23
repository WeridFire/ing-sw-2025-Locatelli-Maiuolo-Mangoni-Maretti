package it.polimi.ingsw.model.shipboard.integrity;

import java.io.Serializable;

public interface IShipIntegrityListener extends Serializable {
    void update(IntegrityProblem integrityProblem);
}
