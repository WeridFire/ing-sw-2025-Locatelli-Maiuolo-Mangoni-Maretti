package src.main.java.it.polimi.ingsw.shipboard.tiles;

import src.main.java.it.polimi.ingsw.enums.CrewType;
import src.main.java.it.polimi.ingsw.shipboard.SideType;
import src.main.java.it.polimi.ingsw.shipboard.visitors.TileVisitor;

/**
 * Represents a tile that provides life support in a given system.
 * This tile has specific sides and offers life support to a certain crew type.
 */
public class LifeSupportSystemTile extends TileSkeleton<SideType> {

    private final CrewType providedLifeSupport;

    /**
     * Constructs a LifeSupportSystemTile with the specified sides and provided life support type.
     *
     * @param sides               An array of sides defining the structure of the tile.
     * @param providedLifeSupport The type of crew that benefits from this life support system.
     */
    public LifeSupportSystemTile(SideType[] sides, CrewType providedLifeSupport) {
        super(sides);
        this.providedLifeSupport = providedLifeSupport;
    }

    /**
     * Accepts a visitor for processing this tile.
     *
     * @param visitor The visitor handling this tile.
     */
    @Override
    public void accept(TileVisitor visitor) {
        visitor.visitLifeSupportSystem(this);
    }

    /**
     * Retrieves the type of crew that receives life support from this tile.
     *
     * @return The crew type benefiting from the life support system.
     */
    public CrewType getProvidedLifeSupport() {
        return providedLifeSupport;
    }
}

