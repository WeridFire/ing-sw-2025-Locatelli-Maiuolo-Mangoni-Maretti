package src.main.java.it.polimi.ingsw;

// TODO: redo

import src.main.java.it.polimi.ingsw.shipboard1.tiles.Tile;
import src.main.java.it.polimi.ingsw.shipboard1.tiles.content.*;
import src.main.java.it.polimi.ingsw.shipboard1.tiles.side.TileSide;
import src.main.java.it.polimi.ingsw.shipboard1.tiles.side.TileSideCannon;
import src.main.java.it.polimi.ingsw.shipboard1.tiles.side.TileSideEngine;
import src.main.java.it.polimi.ingsw.shipboard1.tiles.side.TileSideShield;

import java.util.*;
import java.util.function.Supplier;

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
        /*
        Blue -> "GT-new_tiles_16_for web33.jpg"
        Green -> "GT-new_tiles_16_for web34.jpg"
        Red -> "GT-new_tiles_16_for web52.jpg"
        Yellow -> "GT-new_tiles_16_for web61.jpg"
         */
        return new Tile(
                new TileSide(ConnectorType.UNIVERSAL),
                new TileSide(ConnectorType.UNIVERSAL),
                new TileSide(ConnectorType.UNIVERSAL),
                new TileSide(ConnectorType.UNIVERSAL),
                new TileContentMainCrew()
        );
    }

    /**
     * A mapping of character representations to corresponding tile side suppliers.
     * <p>
     * This map is used to convert a compact string representation of a tile into
     * its detailed {@code TileSide} configuration.
     */
    private static final
    HashMap<Character, Supplier<TileSide>> simpleSidesMap = new HashMap<>(){{
        put('0', () -> new TileSide(ConnectorType.SMOOTH));
        put('1', () -> new TileSide(ConnectorType.SINGLE));
        put('2', () -> new TileSide(ConnectorType.DOUBLE));
        put('3', () -> new TileSide(ConnectorType.UNIVERSAL));
        put('c', () -> new TileSideCannon(false));
        put('C', () -> new TileSideCannon(true));
        put('e', () -> new TileSideEngine(false));
        put('E', () -> new TileSideEngine(true));
        // why 's', 'd', 'f', 'g' for shields with 0, 1, 2, 3 connectors? look at your QWERTY keyboard...
        put('s', () -> new TileSideShield(ConnectorType.SMOOTH));
        put('d', () -> new TileSideShield(ConnectorType.SINGLE));
        put('f', () -> new TileSideShield(ConnectorType.DOUBLE));
        put('g', () -> new TileSideShield(ConnectorType.UNIVERSAL));
    }};

    /**
     * Creates a list of tiles with the same {@code TileContent}
     * based on a compact string representation of their sides.
     * <p>
     * Can also process the unique reference number for graphical representation: not used yet.
     *
     * @param compactTilesSides an ordered list of strings, each representing the four sides
     *                          of the related tile using encoded characters
     * @param firstReferenceNumber the first reference number for graphical identification
     * @param lastReferenceNumber the last reference number for graphical identification
     * @param tileContentSupplier a supplier that provides the {@code TileContent} for each tile
     * @return a list of the generated tiles
     * @throws IllegalArgumentException if the reference numbers are inconsistent with the size of the input list
     */
    private static List<Tile> createListOfTiles(List<String> compactTilesSides,
                                                int firstReferenceNumber, int lastReferenceNumber,
                                                Supplier<TileContent> tileContentSupplier) {
        if (lastReferenceNumber < firstReferenceNumber) {
            throw new IllegalArgumentException("lastReferenceNumber must be greater or equal to firstReferenceNumber");
        }
        if (compactTilesSides.size() != (lastReferenceNumber + 1 - firstReferenceNumber)) {
            throw new IllegalArgumentException("compactTilesSides must be coherent to reference numbers");
        }

        int i, referenceNumber;
        String compactTileSides;
        List<Tile> result = new ArrayList<>(compactTilesSides.size());

        for (i = 0, referenceNumber = firstReferenceNumber;
             referenceNumber <= lastReferenceNumber; referenceNumber++, i++) {
            // "GT-new_tiles_16_for web{referenceNumber}.jpg"
            compactTileSides = compactTilesSides.get(i);
            if (compactTilesSides.size() != 4) {
                throw new IllegalArgumentException
                        ("compactTilesSides must all have 4 and only 4 characters: invalid for " + compactTileSides);
            }
            result.add(new Tile(
                    simpleSidesMap.get(compactTileSides.charAt(0)).get(),
                    simpleSidesMap.get(compactTileSides.charAt(1)).get(),
                    simpleSidesMap.get(compactTileSides.charAt(2)).get(),
                    simpleSidesMap.get(compactTileSides.charAt(3)).get(),
                    tileContentSupplier.get()
            ));
        }

        return result;
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
        ArrayList<Tile> pile = new ArrayList<>();

        // Battery Components with 2 battery slots
        pile.addAll(createListOfTiles(
                List.of("1302", "2300", "2301", "1212", "0310", "1311",
                        "2322", "0030", "0300", "3030", "0330"),
                1, 11,
                () -> TileContentBattery.createBatteryComponentContent(2)));

        // Battery Components with 3 battery slots
        pile.addAll(createListOfTiles(
                List.of("0102", "1102", "0200", "1200", "2201", "0010"),
                12, 17,
                () -> TileContentBattery.createBatteryComponentContent(3)));

        // Cargo Hold with 2 cargo slots
        pile.addAll(createListOfTiles(
                List.of("1203", "1213", "3210", "0030", "0030", "1030", "2030", "1232", "0330"),
                18, 26,
                () -> TileContentCargo.createCargoHoldContent(2)));

        // Cargo Hold with 3 cargo slots
        pile.addAll(createListOfTiles(
                List.of("0010", "0101", "0020", "0202", "0112", "0221"),
                27, 32,
                () -> TileContentCargo.createCargoHoldContent(3)));

        // Cabins
        pile.addAll(createListOfTiles(
                List.of("0013", "2111", "2112", "0210", "1210", "1212", "1023", "2120", "1222", "0320",
                        "1030", "1031", "2030", "0131", "0132", "0232", "2230"),
                35, 51,
                TileContentCrew::new));

        // Structural Tiles
        pile.addAll(createListOfTiles(
                List.of("3310", "3130", "3131", "3132", "3230", "1332", "2330", "2332"),
                53, 60,
                TileContent::new));

        // Special Cargo Hold with 1 cargo slots
        pile.addAll(createListOfTiles(
                List.of("2031", "3030", "1131", "1230", "2232", "0330"),
                62, 67,
                () -> TileContentCargo.createSpecialCargoHoldContent(1)));

        // Special Cargo Hold with 2 cargo slots
        pile.addAll(createListOfTiles(
                List.of("0010", "2010", "0020"),
                68, 70,
                () -> TileContentCargo.createSpecialCargoHoldContent(2)));

        // Engines
        pile.addAll(createListOfTiles(
                List.of("300e", "300e", "010e", "010e", "110e",
                        "020e", "020e", "320e", "230e", "301e",
                        "121e", "031e", "231e", "102e", "212e",
                        "022e", "132e", "003e", "003e", "203e", "013e"),
                71, 91,
                TileContent::new));

        // Double Engines
        pile.addAll(createListOfTiles(
                List.of("010E", "310E", "020E", "130E", "111E", "222E", "032E", "303E", "023E"),
                92, 100,
                TileContent::new));

        // Cannons
        pile.addAll(createListOfTiles(
                List.of("0c01", "0c01", "0c02", "0c02", "1c00",
                        "1c02", "1c03", "2c00", "2c01", "2c03",
                        "3c01", "0c10", "0c12", "0c13", "1c11",
                        "2c13", "3c10", "0c20", "0c21", "0c23",
                        "1c20", "1c23", "2c22", "0c32", "2c30"),
                101, 125,
                TileContent::new));

        // Double Cannons
        pile.addAll(createListOfTiles(
                List.of("0C01", "0C02", "1C03", "3C00", "3C02",
                        "1C12", "2C10", "0C23", "2C21", "0C30", "0C31"),
                126, 136,
                TileContent::new));

        // Life Supports for Brown Alien
        pile.addAll(createListOfTiles(
                List.of("1110", "1210", "0030", "0031", "2030", "0130"),
                137, 142,
                () -> new TileContentLifeSupport(CrewType.BROWN_ALIEN)));

        // Life Supports for Purple Alien
        pile.addAll(createListOfTiles(
                List.of("2120", "2220", "0030", "0032", "1030", "0230"),
                143, 148,
                () -> new TileContentLifeSupport(CrewType.PURPLE_ALIEN)));

        // Shield Generators
        pile.addAll(createListOfTiles(
                List.of("ds13", "sd11", "df12", "ss23", "fs22", "fd21", "ss31", "fs32"),
                149, 156,
                TileContent::new));

        return pile;
    }


}

