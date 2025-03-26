package src.main.java.it.polimi.ingsw.shipboard.visitors;

import src.main.java.it.polimi.ingsw.shipboard.LoadableType;
import src.main.java.it.polimi.ingsw.shipboard.tiles.*;
import src.main.java.it.polimi.ingsw.shipboard.tiles.exceptions.NotFixedTileException;
import src.main.java.it.polimi.ingsw.util.Coordinates;

import java.util.HashMap;
import java.util.Map;

public class VisitorCalculateThrustPower implements TileVisitor {
    private boolean bonus;  // brown alien
    private float baseThrustPower;
    private final Map<Coordinates, Float> doubleEngines;

    public VisitorCalculateThrustPower() {
        bonus = false;
        baseThrustPower = 0;
        doubleEngines = new HashMap<>();
    }


    @Override
    public void visitStructural(StructuralTile tile) { }

    @Override
    public void visitLifeSupportSystem(LifeSupportSystemTile tile) { }

    @Override
    public void visitCargoHold(CargoHoldTile tile) { }

    @Override
    public void visitBatteryComponent(BatteryComponentTile tile) { }

    @Override
    public void visitCannon(CannonTile tile) { }

    @Override
    public void visitShieldGenerator(ShieldGeneratorTile tile) { }

    @Override
    public void visitCabin(CabinTile tile) {
        if (tile.getLoadedItems().contains(LoadableType.BROWN_ALIEN)) {
            bonus = true;
        }
    }

    @Override
    public void visitMainCabin(CabinTile tile) {
        visitCabin(tile);
    }

    @Override
    public void visitEngine(EngineTile tile) {
        if (tile.isDoubleEngine()) {
            try {
                Coordinates location = tile.getCoordinates();
                doubleEngines.put(location, tile.calculateThrustPower());
            } catch (NotFixedTileException e) {
                throw new RuntimeException(e);  // should never happen -> runtime exception
            }
        }
        else {
            baseThrustPower += tile.calculateThrustPower();
        }
    }

    /**
     * @return The ships thrust power (not counting double engines).
     */
    public float getThrustPower() {
        return baseThrustPower + ((bonus && (baseThrustPower > 0)) ? 2f : 0f);
    }

}