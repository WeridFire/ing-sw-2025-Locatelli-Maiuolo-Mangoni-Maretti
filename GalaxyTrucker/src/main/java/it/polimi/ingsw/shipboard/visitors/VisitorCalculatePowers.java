package src.main.java.it.polimi.ingsw.shipboard.visitors;

import src.main.java.it.polimi.ingsw.enums.PowerType;
import src.main.java.it.polimi.ingsw.shipboard.LoadableType;
import src.main.java.it.polimi.ingsw.shipboard.tiles.*;
import src.main.java.it.polimi.ingsw.shipboard.tiles.exceptions.NotFixedTileException;
import src.main.java.it.polimi.ingsw.util.Coordinates;

import java.util.HashMap;
import java.util.Map;

public class VisitorCalculatePowers implements TileVisitor {

    /**
     * Stores information about a power calculation, including base power, activation locations, and bonus.
     */
    public static class CalculatorPowerInfo {
        private boolean bonus;  // Indicates whether an alien bonus applies (e.g. purple/brown alien)
        private float basePower;  // The base power before any modifications: simple tiles (e.g. single cannons/engines)
        private final Map<Coordinates, Float> locationsToActivate;  // Map of tiles that can be activated mapped to their power values

        /**
         * Constructs a CalculatorPowerInfo object with a given map of activable tiles.
         *
         * @param locationsToActivate A map of coordinates where activable power tiles are located mapped to their power values.
         */
        public CalculatorPowerInfo(Map<Coordinates, Float> locationsToActivate) {
            bonus = false;
            basePower = 0;
            this.locationsToActivate = locationsToActivate;
        }

        /**
         * @return The base power value before any bonuses are applied.
         */
        public float getBasePower() {
            return basePower;
        }

        /**
         * Calculates the power bonus based on whether the bonus condition is met.
         *
         * @param totalPowerWithoutBonus The total power before applying the bonus.
         * @return A bonus power value: +2 if {@code totalPowerWithoutBonus > 0} and if it can get the bonus
         * (alien is present), otherwise 0.
         */
        public float getBonus(float totalPowerWithoutBonus) {
            return (bonus && (totalPowerWithoutBonus > 0)) ? 2f : 0f;
        }

        /**
         * Retrieves a map of coordinates where tiles can be activated, along with their respective power values.
         *
         * @return A map containing activatable locations and their power values.
         * @implNote A copy is returned to prevent unintended modifications.
         */
        public Map<Coordinates, Float> getLocationsToActivate() {
            return new HashMap<>(locationsToActivate);
        }
    }

    private final CalculatorPowerInfo infoFirePower;
    private final CalculatorPowerInfo infoThrustPower;

    public VisitorCalculatePowers() {
        infoFirePower = new CalculatorPowerInfo(new HashMap<>());
        infoThrustPower = new CalculatorPowerInfo(new HashMap<>());
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
    public void visitShieldGenerator(ShieldGeneratorTile tile) { }

    @Override
    public void visitCabin(CabinTile tile) {
        if (tile.getLoadedItems().contains(LoadableType.PURPLE_ALIEN)) {
            infoFirePower.bonus = true;
        }
        else if (tile.getLoadedItems().contains(LoadableType.BROWN_ALIEN)) {
            infoThrustPower.bonus = true;
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
                infoFirePower.locationsToActivate.put(location, tile.calculateFirePower());
            } catch (NotFixedTileException e) {
                throw new RuntimeException(e);  // should never happen -> runtime exception
            }
        }
        else {
            infoFirePower.basePower += tile.calculateFirePower();
        }
    }

    @Override
    public void visitEngine(EngineTile tile) {
        if (tile.isDoubleEngine()) {
            try {
                Coordinates location = tile.getCoordinates();
                infoThrustPower.locationsToActivate.put(location, tile.calculateThrustPower());
            } catch (NotFixedTileException e) {
                throw new RuntimeException(e);  // should never happen -> runtime exception
            }
        }
        else {
            infoThrustPower.basePower += tile.calculateThrustPower();
        }
    }

    public CalculatorPowerInfo getInfoFirePower() {
        return infoFirePower;
    }

    public CalculatorPowerInfo getInfoThrustPower() {
        return infoThrustPower;
    }

    /**
     * Retrieves the stored power information based on the given power type.
     *
     * @param powerType The type of power requested. Can be {@link PowerType#FIRE} or {@link PowerType#THRUST}.
     * @return The corresponding {@code CalculatorPowerInfo} for the given power type,
     * or {@code null} if the type is invalid.
     */
    public CalculatorPowerInfo getInfoPower(PowerType powerType) {
        return switch (powerType) {
            case FIRE -> infoFirePower;
            case THRUST -> infoThrustPower;
            default -> null;
        };
    }

}
