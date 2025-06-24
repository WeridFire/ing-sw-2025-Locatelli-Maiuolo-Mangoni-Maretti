package it.polimi.ingsw.model.shipboard.visitors;

import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.model.shipboard.tiles.*;

public class VisitorCalculateShieldedSides implements TileVisitor {
    private final Boolean[] protectedSides;

    public VisitorCalculateShieldedSides() {
        protectedSides = Direction.sortedArray(false, false, false, false)
                .toArray(Boolean[]::new);
    }

    @Override
    public void visitStructural(StructuralTile tile) { }
    @Override
    public void visitLifeSupportSystem(LifeSupportSystemTile tile) { }
    @Override
    public void visitCargoHold(CargoHoldTile tile) { }
    @Override
    public void visitCabin(CabinTile tile) { }
    @Override
    public void visitMainCabin(CabinTile tile) { }
    @Override
    public void visitBatteryComponent(BatteryComponentTile tile) { }
    @Override
    public void visitCannon(CannonTile tile) { }
    @Override
    public void visitEngine(EngineTile tile) { }

    @Override
    public void visitShieldGenerator(ShieldGeneratorTile tile) {
        for (Direction direction : Direction.values()) {
            if (tile.hasPower(direction)) {
                protectedSides[direction.getValue()] = true;
            }
        }
    }

    /**
     * Get info about shielded sides.
     * @param direction The direction to check for shield generator presence.
     * @return {@code true} if among the visited tiles a shield is pointing in the provided direction,
     * {@code false} otherwise.
     */
    public boolean hasShieldFacing(Direction direction) {
        return protectedSides[direction.getValue()];
    }

    public Boolean[] getProtectedSides() {
        return protectedSides;
    }
}
