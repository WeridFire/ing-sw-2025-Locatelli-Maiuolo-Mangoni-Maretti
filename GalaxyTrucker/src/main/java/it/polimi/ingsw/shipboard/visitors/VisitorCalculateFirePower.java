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

    public VisitorCalculateFirePower() {
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
     * @return the base firepower value
     */
    public float getBaseFirePower() {
        return baseFirePower;
    }

    /**
     * @return {@code true} if it's possible to use the firepower bonus, {@code false} otherwise
     */
    public boolean hasBonus() {
        return bonus;
    }

    /**
     * @return a map of coordinates where double cannons are located, along with their respective firepower values.
     * @implNote The returned map is a copy to ensure encapsulation and prevent unintended modifications.
     */
    public Map<Coordinates, Float> getDoubleCannons() {
        return new HashMap<>(doubleCannons);
    }

}
