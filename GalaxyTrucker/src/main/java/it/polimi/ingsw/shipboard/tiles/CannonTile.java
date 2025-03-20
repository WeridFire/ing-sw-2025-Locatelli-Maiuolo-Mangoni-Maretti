package src.main.java.it.polimi.ingsw.shipboard.tiles;

import src.main.java.it.polimi.ingsw.enums.Direction;
import src.main.java.it.polimi.ingsw.shipboard.SideType;
import src.main.java.it.polimi.ingsw.shipboard.visitors.TileVisitor;

import java.util.Arrays;

public class CannonTile extends PowerableTile {

    private static final Float[] maskDirectionsMultiplier =
            Direction.sortedArray(0.5f, 1f, 0.5f, 0.5f).toArray(Float[]::new);

    /**
     * Constructs a Tile with the specified sides, and if it needs battery to be used.
     *
     * @param sides         An array defining the sides of the tile.
     *                      Each index corresponds to a direction {@code d},
     *                      where {@code sides[d.v]} represents the tile's side in that direction.
     *                      Use {@link SideType#CANNON} to notify the presence of a cannon in that direction.
     * @param batteryNeeded Indicates if the cannon needs battery to be used ({@code true}) or not ({@code false}).
     *                      Note: the cannon needs battery if and only if it's a double cannon.
     */
    public CannonTile(SideType[] sides, boolean batteryNeeded) {
        super(sides,
                Arrays.stream(sides)
                .map(side -> (side == SideType.CANNON))
                .toArray(Boolean[]::new),
                batteryNeeded);
    }

    @Override
    public void accept(TileVisitor visitor) {
        visitor.visitCannon(this);
    }

    /**
     * Calculates the firepower of this cannon tile,
     * taking in consideration direction and if it's its double version (if and only if it needs battery).
     *
     * @return This cannon firepower.
     */
    public float calculateFirePower() {
        float basePower = 0;
        for (Direction direction : Direction.values()) {
            basePower += hasPower(direction) ? maskDirectionsMultiplier[direction.getValue()] : 0;
        }
        return basePower * (isBatteryNeeded() ? 2f : 1f);
    }

}
