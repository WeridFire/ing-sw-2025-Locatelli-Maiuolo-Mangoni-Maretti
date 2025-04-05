package it.polimi.ingsw.player;

import it.polimi.ingsw.TilesFactory;
import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.shipboard.ShipBoard;
import it.polimi.ingsw.shipboard.SideType;
import it.polimi.ingsw.shipboard.exceptions.OutOfBuildingAreaException;
import it.polimi.ingsw.shipboard.exceptions.TileAlreadyPresentException;
import it.polimi.ingsw.shipboard.tiles.CabinTile;
import it.polimi.ingsw.shipboard.tiles.TileSkeleton;
import it.polimi.ingsw.shipboard.tiles.exceptions.FixedTileException;
import it.polimi.ingsw.util.Coordinates;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;
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
        player1.setShipBoard(shipBoard1);
        List<TileSkeleton> tilesPool = TilesFactory.createPileTiles();
        for(int i=0; i<10; i++){
            for(int j=0; j<10; j++){
                Coordinates c = new Coordinates(i, j);
                Collections.shuffle(tilesPool);
                TileSkeleton t = tilesPool.removeFirst();
                try{
                    shipBoard1.setTile(t, c);
                }catch(Exception e){}
            }
        }

        /* DEPRECATED
        System.out.println("Test1");
        player1.printCliShipboard();
         */

        System.out.println("\nTest BEFORE assembly's ended\n");
        System.out.println(player1.getShipBoard().getCLIRepresentation());

        assertDoesNotThrow(() -> player1.getShipBoard().endAssembly());
        System.out.println("\nTest AFTER assembly's ended\n");
        System.out.println(player1.getShipBoard().getCLIRepresentation());
    }
}