package it.polimi.ingsw.shipboard.tiles;

import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.shipboard.SideType;
import it.polimi.ingsw.shipboard.visitors.TileVisitor;

import java.util.Arrays;

public class EngineTile extends PowerableTile {

    private static final Float[] maskDirectionsMultiplier =
            Direction.sortedArray(0f, 0f, 0f, 1f).toArray(Float[]::new);

    /**
     * Constructs an engine tile with the specified sides, and if it needs battery to be used.
     *
     * @param sides         An array defining the sides of the tile.
     *                      Each index corresponds to a direction {@code d},
     *                      where {@code sides[d.v]} represents the tile's side in that direction.
     *                      Use {@link SideType#ENGINE} to notify the presence of a cannon in that direction.
     * @param batteryNeeded Indicates if the engine needs battery to be used ({@code true}) or not ({@code false}).
     *                      Note: the engine needs battery if and only if it's a double engine.
     */
    public EngineTile(SideType[] sides, boolean batteryNeeded) {
        super(sides,
                Arrays.stream(sides)
                        .map(side -> (side == SideType.ENGINE))
                        .toArray(Boolean[]::new),
                batteryNeeded);
        setCLISymbol(batteryNeeded ? "2E" : "1e");
    }

    @Override
    public void accept(TileVisitor visitor) {
        visitor.visitEngine(this);
    }

    /**
     * Calculates the thrust power of this cannon tile,
     * taking in consideration direction and if it's its double version (if and only if it needs battery).
     *
     * @return This engine thrust power.
     */
    public float calculateThrustPower() {
        float basePower = 0;
        for (Direction direction : Direction.values()) {
            basePower += hasPower(direction) ? maskDirectionsMultiplier[direction.getValue()] : 0;
        }
        return basePower * (isBatteryNeeded() ? 2f : 1f);
    }

    /**
     * Retrieve info about whether this is a double engine or not.
     * @return {@code true} if this engine is double, {@code false} if is single.
     */
    public boolean isDoubleEngine() {
        return isBatteryNeeded();
    }

}
