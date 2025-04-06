package it.polimi.ingsw.shipboard.visitors;

import it.polimi.ingsw.shipboard.tiles.*;

import java.io.Serializable;

public interface TileVisitor extends Serializable {

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
     * What to do when visiting the provided cargo hold tile (or special cargo hold tile).
     * To be implemented in each visitor.
     * @param tile The visited (eventually special) cargo hold tile.
     */
    void visitCargoHold(CargoHoldTile tile);

    /**
     * What to do when visiting the provided cabin tile (can NOT be the main cabin).
     * To be implemented in each visitor.
     * @param tile The visited (NOT-main) cabin tile.
     */
    void visitCabin(CabinTile tile);

    /**
     * What to do when visiting the provided main cabin tile.
     * To be implemented in each visitor.
     * @param tile The visited main cabin tile.
     */
    void visitMainCabin(CabinTile tile);

    /**
     * What to do when visiting the provided battery component tile.
     * To be implemented in each visitor.
     * @param tile The visited battery component tile.
     */
    void visitBatteryComponent(BatteryComponentTile tile);

    /**
     * What to do when visiting the provided cannon tile (can be double cannon).
     * To be implemented in each visitor.
     * @param tile The visited (eventually double) cannon tile.
     */
    void visitCannon(CannonTile tile);

    /**
     * What to do when visiting the provided engine tile (can be double engine).
     * To be implemented in each visitor.
     * @param tile The visited (eventually double) engine tile.
     */
    void visitEngine(EngineTile tile);

    /**
     * What to do when visiting the provided shield generator tile.
     * To be implemented in each visitor.
     * @param tile The visited shield generator tile.
     */
    void visitShieldGenerator(ShieldGeneratorTile tile);

}
