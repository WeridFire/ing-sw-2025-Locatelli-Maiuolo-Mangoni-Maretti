package src.main.java.it.polimi.ingsw;

import src.main.java.it.polimi.ingsw.enums.ConnectorType;
import src.main.java.it.polimi.ingsw.shipboard.tiles.Tile;
import src.main.java.it.polimi.ingsw.shipboard.tiles.content.TileContentBattery;
import src.main.java.it.polimi.ingsw.shipboard.tiles.content.TileContentMainCrew;
import src.main.java.it.polimi.ingsw.shipboard.tiles.side.TileSide;

import java.util.*;

/**
 * Factory class for creating predefined tiles used in the game.
 * This class provides static methods to generate specific tile instances,
 * including the main cabin tile and a set of tiles available in the tile pile.
 */
public class TilesFactory {

    /**
     * Creates and returns the main cabin tile.
     * <p>
     * The main cabin is a fundamental tile that has universal connectors on all four sides,
     * allowing for maximum flexibility in ship construction.
     * </p>
     *
     * @return A {@code Tile} instance representing the main cabin.
     */
    public static Tile createMainCabinTile() {
        return new Tile(
                new TileSide(ConnectorType.UNIVERSAL),
                new TileSide(ConnectorType.UNIVERSAL),
                new TileSide(ConnectorType.UNIVERSAL),
                new TileSide(ConnectorType.UNIVERSAL),
                new TileContentMainCrew()
        );
    }

    /**
     * Creates and returns a list of all the predefined tiles that can be used in the game.
     * <p>
     * These tiles represent various functional components.
     * </p>
     *
     * @return A {@code List<Tile>} containing a set of predefined tiles.
     */
    public static List<Tile> createPileTiles() {

        return Arrays.asList(
                new Tile(
                        new TileSide(ConnectorType.SINGLE),
                        new TileSide(ConnectorType.UNIVERSAL),
                        new TileSide(ConnectorType.SMOOTH),
                        new TileSide(ConnectorType.DOUBLE),
                        TileContentBattery.createBatteryComponentContent(2)
                ),

                new Tile(
                        new TileSide(ConnectorType.DOUBLE),
                        new TileSide(ConnectorType.UNIVERSAL),
                        new TileSide(ConnectorType.SMOOTH),
                        new TileSide(ConnectorType.SMOOTH),
                        TileContentBattery.createBatteryComponentContent(2)
                )

                /*,

                new Tile(
                        ...
                ),

                // all the other tiles
                */
        );

    }

}

