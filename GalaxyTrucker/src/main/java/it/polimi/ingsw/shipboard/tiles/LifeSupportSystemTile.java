package it.polimi.ingsw.shipboard.tiles;

import it.polimi.ingsw.shipboard.LoadableType;
import it.polimi.ingsw.shipboard.SideType;
import it.polimi.ingsw.shipboard.visitors.TileVisitor;

/**
 * Represents a tile that provides life support in a given system.
 * This tile has specific sides and offers life support to a certain crew type.
 */
public class LifeSupportSystemTile extends TileSkeleton {

    private final LoadableType providedLifeSupport;

    /**
     * Constructs a LifeSupportSystemTile with the specified sides and provided life support type.
     *
     * @param sides               An array of sides defining the structure of the tile.
     * @param providedLifeSupport The type of crew that benefits from this life support system.
     */
    public LifeSupportSystemTile(SideType[] sides, LoadableType providedLifeSupport) {
        super(sides);
        if(providedLifeSupport != LoadableType.BROWN_ALIEN &&
        providedLifeSupport != LoadableType.PURPLE_ALIEN){
            throw new IllegalArgumentException("The life support should be for either a brown or a purple alien.");
        }
        this.providedLifeSupport = providedLifeSupport;
        setCLISymbol(providedLifeSupport == LoadableType.PURPLE_ALIEN ? "HP" : "HB");
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
    public LoadableType getProvidedLifeSupport() {
        return providedLifeSupport;
    }

    @Override
    public String getName() {
        return "Life Support " + getProvidedLifeSupport();
    }
}

