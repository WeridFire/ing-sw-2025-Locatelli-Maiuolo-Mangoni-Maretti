package src.main.java.it.polimi.ingsw.shipboard.tiles;

import src.main.java.it.polimi.ingsw.shipboard.visitors.TileVisitor;

public interface Tile {
    /**
     * Method to accept a visitor for this tile in the Visitor Pattern.
     *
     * @param visitor The visitor that will visit this tile.
     */
    void accept(TileVisitor visitor);
}
