package src.main.java.it.polimi.ingsw.shipboard.tiles;

import src.main.java.it.polimi.ingsw.shipboard.visitors.TileVisitor;

// TODO: change String to a valid TileSide implemented for this shipboard version
public class StructuralTile extends TileSkeleton<String> {

    /**
     * Construct the tile with specified parameters.
     *
     * @param sides An array of sides, where for each Direction {@code d},
     *              its value {@code d.v <- d.getValue()} is used as index to specify the related side:
     *              {@code sides[d.v]} is the tile's side in direction {@code d}.
     */
    public StructuralTile(String[] sides) {
        super(sides);
    }

    @Override
    public void accept(TileVisitor visitor) {
        visitor.visitStructural(this);
    }
}
