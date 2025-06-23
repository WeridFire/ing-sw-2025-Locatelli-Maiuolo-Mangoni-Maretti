package it.polimi.ingsw.model.shipboard.tiles;

import it.polimi.ingsw.model.shipboard.SideType;
import it.polimi.ingsw.model.shipboard.visitors.TileVisitor;

public class ShieldGeneratorTile extends PowerableTile {

    /**
     * Constructs a shield generator tile with the specified sides, and the protected directions.
     *
     * @param sides         An array defining the sides of the tile.
     *                      Each index corresponds to a direction {@code d},
     *                      where {@code sides[d.v]} represents the tile's side in that direction.
     * @param protectedSides A mask for {@code sides} where each side can implement a shield
     *                       ({@code true} if and only if that side has a shield).
     *                       Same indexing notation with directions' values.
     */
    public ShieldGeneratorTile(SideType[] sides, Boolean[] protectedSides) {
        super(sides, protectedSides, true);
        if(protectedSides[0] && protectedSides[1]){
            //WN
            setCLISymbol("/s");
        }else if(protectedSides[1] && protectedSides[2]){
            setCLISymbol("s\\");
        }else if(protectedSides[2] && protectedSides[3]){
            setCLISymbol("s/");
        }else{
            setCLISymbol("\\s");
        }
    }

    @Override
    public void accept(TileVisitor visitor) {
        visitor.visitShieldGenerator(this);
    }

    @Override
    public String getName() {
        return "Shield " + getCLISymbol();
    }
}
