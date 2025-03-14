package src.main.java.it.polimi.ingsw.shipboard.tiles;

/**
 * Interface that enforces the implementation of methods that will be used
 * to handle any tokens used in the game that can be loaded onto tiles.
 */
public interface ILoadableItem {
    /**
     * Overview: every tile has a space limit.
     * @return The space occupied by this item if loaded onto a tile.
     */
    int getOccupiedSpace();

    /**
     * Overview: some items can be sold at the end of the game.
     * @return The selling price in cosmic credits for this item.
     */
    int getCreditsValue();
}
