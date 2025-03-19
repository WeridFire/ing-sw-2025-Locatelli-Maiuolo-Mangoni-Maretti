package src.main.java.it.polimi.ingsw.shipboard.visitors;

import src.main.java.it.polimi.ingsw.shipboard.tiles.*;

public interface TileVisitor {

    /**
     * What to do when visiting the provided structural tile.
     * To be implemented in each visitor.
     * @param tile The visited structural tile.
     */
    void visitStructural(StructuralTile tile);

    /**
     * What to do when visiting the provided life support system tile.
     * To be implemented in each visitor.
     * @param tile The visited life support system tile.
     */
    void visitLifeSupportSystem(LifeSupportSystemTile tile);

    /**
     * What to do when visiting the provided cargo hold tile.
     * To be implemented in each visitor.
     * @param tile The visited cargo hold tile.
     */
    void visitCargoHold(CargoHoldTile tile);

    // TODO: add all the other concrete Tiles
    // example -> void visitEngine(EngineTile engine); ...
}
