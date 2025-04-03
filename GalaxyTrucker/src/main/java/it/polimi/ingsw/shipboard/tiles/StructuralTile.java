package it.polimi.ingsw.shipboard.tiles;

import it.polimi.ingsw.shipboard.SideType;
import it.polimi.ingsw.shipboard.visitors.TileVisitor;

/**
 * Represents a structural tile, which defines the shape and connectivity
 * of a structure but does not provide additional functionality.
 */
public class StructuralTile extends TileSkeleton {

    /**
     * Constructs a StructuralTile with the specified sides.
     *
     * @param sides An array defining the sides of the tile.
     *              Each index corresponds to a direction {@code d},
     *              where {@code sides[d.v]} represents the tile's side in that direction.
     */
    public StructuralTile(SideType[] sides) {
        super(sides);
        setCLISymbol("╳╳");
    }

    /**
     * Accepts a visitor for processing this tile.
     *
     * @param visitor The visitor handling this tile.
     */
    @Override
    public void accept(TileVisitor visitor) {
        visitor.visitStructural(this);
    }
}

