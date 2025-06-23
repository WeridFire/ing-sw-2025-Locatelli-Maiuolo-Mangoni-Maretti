package it.polimi.ingsw.model.shipboard.visitors;

import it.polimi.ingsw.enums.PowerType;
import it.polimi.ingsw.model.shipboard.LoadableType;
import it.polimi.ingsw.model.shipboard.tiles.*;
import it.polimi.ingsw.model.shipboard.tiles.exceptions.NotFixedTileException;
import it.polimi.ingsw.util.Coordinates;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VisitorCalculatePowers implements TileVisitor {

    /**
     * Stores information about a power calculation, including base power, activation locations, and bonus.
     */
    public static class CalculatorPowerInfo implements Serializable {
        private boolean bonus;  // Indicates whether an alien bonus applies (e.g. purple/brown alien)
        private float basePower;  // The base power before any modifications: simple tiles (e.g. single cannons/engines)
        private final Map<Coordinates, Float> locationsToActivate;  // Map of tiles that can be activated mapped to their power values
        private final List<Coordinates> locations;  // List of visited tiles coordinates

        /**
         * Constructs a CalculatorPowerInfo object with a given map of activable tiles.
         */
        public CalculatorPowerInfo() {
            bonus = false;
            basePower = 0;
            locationsToActivate = new HashMap<>();
            locations = new ArrayList<>();
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

        /**
         * Retrieve info about the presence of this power in specific coordinates.
         * @param coordinates the coordinates to check for presence.
         * @return {@code true} if the specified coordinates contains this power info, {@code false} otherwise.
         */
        public boolean isPresent(Coordinates coordinates) {
            return locations.contains(coordinates);
        }
    }

    private final CalculatorPowerInfo infoFirePower;
    private final CalculatorPowerInfo infoThrustPower;

    public VisitorCalculatePowers() {
        infoFirePower = new CalculatorPowerInfo();
        infoThrustPower = new CalculatorPowerInfo();
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
        Coordinates location;
        try {
            location = tile.getCoordinates();
        } catch (NotFixedTileException e) {
            throw new RuntimeException(e);  // should never happen -> runtime exception
        }

        if (tile.isDoubleCannon()) {
            infoFirePower.locationsToActivate.put(location, tile.calculateFirePower());
        }
        else {
            infoFirePower.basePower += tile.calculateFirePower();
        }
        infoFirePower.locations.add(location);
    }

    @Override
    public void visitEngine(EngineTile tile) {
        Coordinates location;
        try {
            location = tile.getCoordinates();
        } catch (NotFixedTileException e) {
            throw new RuntimeException(e);  // should never happen -> runtime exception
        }

        if (tile.isDoubleEngine()) {
            infoThrustPower.locationsToActivate.put(location, tile.calculateThrustPower());
        }
        else {
            infoThrustPower.basePower += tile.calculateThrustPower();
        }
        infoThrustPower.locations.add(location);
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
     * @param powerType The type of power requested.
     * @return The corresponding {@code CalculatorPowerInfo} for the given power type.
     */
    public CalculatorPowerInfo getInfoPower(PowerType powerType) {
        return switch (powerType) {
            case FIRE -> infoFirePower;
            case THRUST -> infoThrustPower;
        };
    }

}
