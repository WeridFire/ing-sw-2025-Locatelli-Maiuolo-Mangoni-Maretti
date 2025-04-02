package it.polimi.ingsw.player;

import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.shipboard.ShipBoard;
import it.polimi.ingsw.shipboard.SideType;
import it.polimi.ingsw.shipboard.exceptions.OutOfBuildingAreaException;
import it.polimi.ingsw.shipboard.exceptions.TileAlreadyPresentException;
import it.polimi.ingsw.shipboard.tiles.CabinTile;
import it.polimi.ingsw.shipboard.tiles.exceptions.FixedTileException;
import it.polimi.ingsw.util.Coordinates;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class PlayerTest {

    Player player1 = new Player("SpaceTruckKing", UUID.randomUUID());
    ShipBoard shipBoard1 = new ShipBoard(GameLevel.TWO);
    SideType[] sideCannon1 = new SideType[4];
    Coordinates cabinCord1 = new Coordinates(5, 6);
    CabinTile cabin1 = new CabinTile(Direction.sortedArray(
            SideType.UNIVERSAL, SideType.DOUBLE, SideType.UNIVERSAL, SideType.SMOOTH).toArray(SideType[]::new));
    Coordinates cabinCord2 = new Coordinates(6, 6);
    CabinTile cabin2 = new CabinTile(Direction.sortedArray(
            SideType.SINGLE, SideType.UNIVERSAL, SideType.UNIVERSAL, SideType.SMOOTH).toArray(SideType[]::new));


    @Test
    void testCliPrint() throws OutOfBuildingAreaException,FixedTileException, TileAlreadyPresentException {
        System.out.println("Test1");
        player1.setShipBoard(shipBoard1);
        shipBoard1.setTile(cabin1, cabinCord1);
        shipBoard1.setTile(cabin2, cabinCord2);
        player1.printCliShipboard();
        for(String s : player1.getShipBoard().getCLIRepresentation()){
            System.out.println(s);
        }
        System.out.println("Test2");
    }
}