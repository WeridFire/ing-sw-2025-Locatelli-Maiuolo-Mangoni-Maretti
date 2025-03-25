package src.main.java.it.polimi.ingsw.shipboard.visitors;

import src.main.java.it.polimi.ingsw.shipboard.LoadableType;
import src.main.java.it.polimi.ingsw.shipboard.tiles.*;
import src.main.java.it.polimi.ingsw.shipboard.tiles.exceptions.NotFixedTileException;
import src.main.java.it.polimi.ingsw.util.Coordinates;

import java.util.HashMap;
import java.util.Map;

public class VisitorCalculateFirePower implements TileVisitor {
    private boolean bonus;  // purple alien
    private float baseFirePower;
    private final Map<Coordinates, Float> doubleCannons;

    VisitorCalculateFirePower() {
        bonus = false;
        baseFirePower = 0;
        doubleCannons = new HashMap<>();
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
    public void visitEngine(EngineTile tile) { }

    @Override
    public void visitShieldGenerator(ShieldGeneratorTile tile) { }

    @Override
    public void visitCabin(CabinTile tile) {
        if (tile.getLoadedItems().contains(LoadableType.PURPLE_ALIEN)) {
            bonus = true;
        }
    }

    @Override
    public void visitMainCabin(CabinTile tile) {
        visitCabin(tile);
    }

    @Override
    public void visitCannon(CannonTile tile) {
        if (tile.isDoubleCannon()) {
            try {
                Coordinates location = tile.getCoordinates();
                doubleCannons.put(location, tile.calculateFirePower());
            } catch (NotFixedTileException e) {
                throw new RuntimeException(e);  // should never happen -> runtime exception
            }
        }
        else {
            baseFirePower += tile.calculateFirePower();
        }
    }

    /**
     * Delegated to ask player which double cannons to activate.
     * Then calculates the total firepower based on that info.
     * @return The calculated firepower
     */
    public float getFirePower() {
        // TODO: ask the player which double cannons to activate
        float doubleCannonsPower = 0f;  // suppose no double cannon is activated
        // the following is correct
        float cannonsPower = baseFirePower + doubleCannonsPower;
        return cannonsPower + ((bonus && (cannonsPower > 0)) ? 2f : 0f);
    }


}
