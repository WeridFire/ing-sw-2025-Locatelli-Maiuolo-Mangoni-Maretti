package it.polimi.ingsw.model.shipboard.visitors;

import it.polimi.ingsw.model.shipboard.tiles.*;
import it.polimi.ingsw.model.shipboard.tiles.exceptions.NotFixedTileException;

public class VisitorCalculateCargoInfo implements TileVisitor {
    private final CalculatorCargoInfo<CabinTile> infoCrew;
    private final CalculatorCargoInfo<CargoHoldTile> infoGoods;
    private final CalculatorCargoInfo<BatteryComponentTile> infoBatteries;
    private final CalculatorCargoInfo<ContainerTile> infoAllContainers;

    public VisitorCalculateCargoInfo() {
        infoCrew = new CalculatorCargoInfo<>();
        infoGoods = new CalculatorCargoInfo<>();
        infoBatteries = new CalculatorCargoInfo<>();
        infoAllContainers = new CalculatorCargoInfo<>();
    }

    @Override
    public void visitStructural(StructuralTile tile) { }
    @Override
    public void visitLifeSupportSystem(LifeSupportSystemTile tile) { }
    @Override
    public void visitCannon(CannonTile tile) { }
    @Override
    public void visitEngine(EngineTile tile) { }
    @Override
    public void visitShieldGenerator(ShieldGeneratorTile tile) { }

    @Override
    public void visitCargoHold(CargoHoldTile tile) {
        try {
            infoGoods.visit(tile);
            infoAllContainers.visit(tile);
        } catch (NotFixedTileException e) {
            throw new RuntimeException(e);  // should never happen -> runtime error
        }
    }

    @Override
    public void visitCabin(CabinTile tile) {
        try {
            infoCrew.visit(tile);
            infoAllContainers.visit(tile);
        } catch (NotFixedTileException e) {
            throw new RuntimeException(e);  // should never happen -> runtime error
        }
    }

    @Override
    public void visitMainCabin(CabinTile tile) {
        visitCabin(tile);
    }

    @Override
    public void visitBatteryComponent(BatteryComponentTile tile) {
        try {
            infoBatteries.visit(tile);
            infoAllContainers.visit(tile);
        } catch (NotFixedTileException e) {
            throw new RuntimeException(e);  // should never happen -> runtime error
        }
    }

    public CalculatorCargoInfo<CabinTile> getCrewInfo() {
        return infoCrew;
    }

    public CalculatorCargoInfo<CargoHoldTile> getGoodsInfo() {
        return infoGoods;
    }

    public CalculatorCargoInfo<BatteryComponentTile> getBatteriesInfo() {
        return infoBatteries;
    }

    public CalculatorCargoInfo<ContainerTile> getInfoAllContainers() {
        return infoAllContainers;
    }
}
