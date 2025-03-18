package src.main.java.it.polimi.ingsw.shipboard1.tiles.content;

/**
 * A container tile content that holds crew members and is Main Cabin.
 */
public class TileContentMainCrew extends TileContentCrew {

    @Override
    public boolean isMainCabin() {
        return true;
    }
}
