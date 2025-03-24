package src.main.java.it.polimi.ingsw.shipboard.tiles;

import src.main.java.it.polimi.ingsw.enums.Direction;
import src.main.java.it.polimi.ingsw.shipboard.LoadableType;
import src.main.java.it.polimi.ingsw.shipboard.SideType;
import src.main.java.it.polimi.ingsw.shipboard.visitors.TileVisitor;

import java.util.Set;

public class MainCabinTile extends CabinTile {
    /**
     * Constructs a Main CabinTile.
     * Since it's the main cabin, it's not allowed to load aliens here.
     */
    public MainCabinTile() {
        super(Direction.sortedArray(SideType.UNIVERSAL, SideType.UNIVERSAL,
                        SideType.UNIVERSAL, SideType.UNIVERSAL).toArray(new SideType[0]),
                Set.of(LoadableType.HUMAN));
    }

    @Override
    public void accept(TileVisitor visitor) {
        visitor.visitMainCabin(this);
    }
}
