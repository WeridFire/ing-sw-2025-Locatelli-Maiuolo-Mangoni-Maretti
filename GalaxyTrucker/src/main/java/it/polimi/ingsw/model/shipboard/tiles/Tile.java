package it.polimi.ingsw.model.shipboard.tiles;

import it.polimi.ingsw.model.shipboard.visitors.TileVisitor;

import java.io.Serializable;

public interface Tile extends Serializable {
    /**
     * Method to accept a visitor for this tile in the Visitor Pattern.
     *
     * @param visitor The visitor that will visit this tile.
     */
    void accept(TileVisitor visitor);
}
