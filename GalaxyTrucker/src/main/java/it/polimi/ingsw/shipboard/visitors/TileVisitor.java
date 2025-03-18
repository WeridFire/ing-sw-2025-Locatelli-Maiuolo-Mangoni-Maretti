package src.main.java.it.polimi.ingsw.shipboard.visitors;

import src.main.java.it.polimi.ingsw.shipboard.tiles.*;

public interface TileVisitor {

    /**
     * What to do when visiting the provided structural tile.
     * To be implemented in each visitor.
     * @param structural The visited structural tile.
     */
    void visitStructural(StructuralTile structural);

    // TODO: add all the other concrete Tiles
    // example -> void visitEngine(EngineTile engine); ...
}
