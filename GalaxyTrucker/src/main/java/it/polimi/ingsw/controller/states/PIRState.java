package it.polimi.ingsw.controller.states;

import it.polimi.ingsw.model.playerInput.PIRType;
import it.polimi.ingsw.model.playerInput.PIRs.PIR;
import it.polimi.ingsw.model.shipboard.LoadableType;
import it.polimi.ingsw.util.Coordinates;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PIRState extends CommonState {

    private static final Map<Coordinates, List<LoadableType>> localCargo = new HashMap<>();
    public static Map<Coordinates, List<LoadableType>> getLocalCargo() {
        return localCargo;
    }

    public static boolean isPIRActive() {
        return getGameData() != null &&
                getGameData().getPIRHandler() != null &&
                getGameData().getPIRHandler().getPlayerPIR(getPlayer()) != null;
    }

    public static PIR getActivePIR() {
        if (!isPIRActive()) return null;
        return getGameData().getPIRHandler().getPlayerPIR(getPlayer());
    }

    public static PIRType getActivePIRType() {
        PIR activePIR = getActivePIR();
        return (activePIR == null) ? null : activePIR.getPIRType();
    }

}
