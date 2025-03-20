package src.main.java.it.polimi.ingsw.shipboard.tiles;

import src.main.java.it.polimi.ingsw.shipboard.LoadableType;
import src.main.java.it.polimi.ingsw.shipboard.SideType;
import src.main.java.it.polimi.ingsw.shipboard.visitors.TileVisitor;

import java.util.Set;

public class CabinTile extends ContainerTile {

    /**
     * Constructs a CabinTile with specified sides and maximum allowed crew.
     *
     * @param sides An array of sides defining the structure of the tile.
     */
    protected CabinTile(SideType[] sides, Set<LoadableType> maxAllowedCrew) {
        super(sides, maxAllowedCrew, Set.of(LoadableType.HUMAN), 2);
    }

    /**
     * Constructs a standard CabinTile with specified sides.
     * It's initialized as to load only humans, but it's possible to call
     * {@link #setAllowedItems(Set)} to allow the loading of aliens.
     *
     * @param sides An array of sides defining the structure of the tile.
     */
    public CabinTile(SideType[] sides) {
        this(sides, Set.of(LoadableType.HUMAN, LoadableType.PURPLE_ALIEN, LoadableType.BROWN_ALIEN));
    }

    @Override
    public void accept(TileVisitor visitor) {
        visitor.visitCabin(this);
    }
}
