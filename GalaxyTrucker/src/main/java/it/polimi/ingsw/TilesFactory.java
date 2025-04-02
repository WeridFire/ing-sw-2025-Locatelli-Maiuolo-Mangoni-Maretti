package it.polimi.ingsw;

import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.shipboard.LoadableType;
import it.polimi.ingsw.shipboard.SideType;
import it.polimi.ingsw.shipboard.tiles.*;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

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
    public static MainCabinTile createMainCabinTile() {
        /*
        Blue -> "GT-new_tiles_16_for web33.jpg"
        Green -> "GT-new_tiles_16_for web34.jpg"
        Red -> "GT-new_tiles_16_for web52.jpg"
        Yellow -> "GT-new_tiles_16_for web61.jpg"
         */
        return new MainCabinTile();
    }

    /**
     * A mapping of character representations to corresponding tile side suppliers.
     * <p>
     * This map is used to convert a compact string representation of a tile into
     * its detailed {@code TileSide} configuration.
     */
    private static final
    HashMap<Character, SideType> simpleSidesMap = new HashMap<>(){{
        put('0', SideType.SMOOTH);
        put('1', SideType.SINGLE);
        put('2', SideType.DOUBLE);
        put('3', SideType.UNIVERSAL);
        put('c', SideType.CANNON);
        put('C', SideType.CANNON);
        put('e', SideType.ENGINE);
        put('E', SideType.ENGINE);
        // why 's', 'd', 'f', 'g' for shields with 0, 1, 2, 3 connectors? look at your QWERTY keyboard...
        put('s', SideType.SMOOTH);
        put('d', SideType.SINGLE);
        put('f', SideType.DOUBLE);
        put('g', SideType.UNIVERSAL);
    }};

    /**
     * suppose compactTileSides already has exactly 4 characters
     */
    private static SideType[] calculateSimpleSides(String compactTileSides) {
        assert compactTileSides != null && compactTileSides.length() == 4;
        return Direction.sortedArray(
                simpleSidesMap.get(compactTileSides.charAt(0)),
                simpleSidesMap.get(compactTileSides.charAt(1)),
                simpleSidesMap.get(compactTileSides.charAt(2)),
                simpleSidesMap.get(compactTileSides.charAt(3))
        ).toArray(new SideType[0]);
    }

    /**
     * suppose compactTileSides already has exactly 4 characters
     */
    private static Boolean[] calculateShieldedSides(String compactTileSides) {
        assert compactTileSides != null && compactTileSides.length() == 4;
        Boolean[] protectedSides = new Boolean[4];
        Predicate<Character> isShield = (ch) -> (ch == 's') || (ch == 'd') || (ch == 'f') || (ch == 'g');
        for (int i = 0; i < 4; i++) {
            protectedSides[i] = isShield.test(compactTileSides.charAt(i));
        }
        return protectedSides;
    }

    /**
     * Creates a list of tiles of the same type based on a compact string representation of their sides.
     * <p>
     * Can also process the unique reference number for graphical representation: not used yet.
     *
     * @param compactTilesSides an ordered list of strings, each representing the four sides
     *                          of the related tile using encoded characters
     * @param firstReferenceNumber the first reference number for graphical identification
     * @param lastReferenceNumber the last reference number for graphical identification
     * @param tileConstructor a function that provides an instance of a concrete extension of {@link TileSkeleton}
     *                        for each tile, given the characters representing sides info.
     * @return a list of the generated tiles
     * @throws IllegalArgumentException if the reference numbers are inconsistent with the size of the input list
     */
    private static List<TileSkeleton> createListOfTiles(List<String> compactTilesSides,
                                                int firstReferenceNumber, int lastReferenceNumber,
                                                Function<String, TileSkeleton> tileConstructor) {
        if (lastReferenceNumber < firstReferenceNumber) {
            throw new IllegalArgumentException("lastReferenceNumber must be greater or equal to firstReferenceNumber");
        }
        if (compactTilesSides.size() != (lastReferenceNumber + 1 - firstReferenceNumber)) {
            throw new IllegalArgumentException("compactTilesSides must be coherent to reference numbers");
        }

        int i, referenceNumber;
        String compactTileSides;
        List<TileSkeleton> result = new ArrayList<>(compactTilesSides.size());

        for (i = 0, referenceNumber = firstReferenceNumber;
             referenceNumber <= lastReferenceNumber; referenceNumber++, i++) {
            // "GT-new_tiles_16_for web{referenceNumber}.jpg"
            compactTileSides = compactTilesSides.get(i);
            if (compactTilesSides.size() != 4) {
                throw new IllegalArgumentException
                        ("compactTilesSides must all have 4 and only 4 characters: invalid for " + compactTileSides);
            }
            result.add(tileConstructor.apply(compactTileSides));
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
    public static List<TileSkeleton> createPileTiles() {
        ArrayList<TileSkeleton> pile = new ArrayList<>();

        // Battery Components with 2 battery slots
        pile.addAll(createListOfTiles(
                List.of("1302", "2300", "2301", "1212", "0310", "1311",
                        "2322", "0030", "0300", "3030", "0330"),
                1, 11,
                (compactTileSides) -> new BatteryComponentTile(calculateSimpleSides(compactTileSides), 2)));

        // Battery Components with 3 battery slots
        pile.addAll(createListOfTiles(
                List.of("0102", "1102", "0200", "1200", "2201", "0010"),
                12, 17,
                (compactTileSides) -> new BatteryComponentTile(calculateSimpleSides(compactTileSides), 3)));

        // Cargo Hold with 2 cargo slots
        pile.addAll(createListOfTiles(
                List.of("1203", "1213", "3210", "0030", "0030", "1030", "2030", "1232", "0330"),
                18, 26,
                (compactTileSides) -> new CargoHoldTile(calculateSimpleSides(compactTileSides), 2)));

        // Cargo Hold with 3 cargo slots
        pile.addAll(createListOfTiles(
                List.of("0010", "0101", "0020", "0202", "0112", "0221"),
                27, 32,
                (compactTileSides) -> new CargoHoldTile(calculateSimpleSides(compactTileSides), 3)));

        // Cabins
        pile.addAll(createListOfTiles(
                List.of("0013", "2111", "2112", "0210", "1210", "1212", "1023", "2120", "1222", "0320",
                        "1030", "1031", "2030", "0131", "0132", "0232", "2230"),
                35, 51,
                (compactTileSides) -> new CabinTile(calculateSimpleSides(compactTileSides))));

        // Structural Tiles
        pile.addAll(createListOfTiles(
                List.of("3310", "3130", "3131", "3132", "3230", "1332", "2330", "2332"),
                53, 60,
                (compactTileSides) -> new StructuralTile(calculateSimpleSides(compactTileSides))));

        // Special Cargo Hold with 1 cargo slots
        pile.addAll(createListOfTiles(
                List.of("2031", "3030", "1131", "1230", "2232", "0330"),
                62, 67,
                (compactTileSides) -> new SpecialCargoHoldTile(calculateSimpleSides(compactTileSides), 1)));

        // Special Cargo Hold with 2 cargo slots
        pile.addAll(createListOfTiles(
                List.of("0010", "2010", "0020"),
                68, 70,
                (compactTileSides) -> new SpecialCargoHoldTile(calculateSimpleSides(compactTileSides), 2)));

        // Engines
        pile.addAll(createListOfTiles(
                List.of("300e", "300e", "010e", "010e", "110e",
                        "020e", "020e", "320e", "230e", "301e",
                        "121e", "031e", "231e", "102e", "212e",
                        "022e", "132e", "003e", "003e", "203e", "013e"),
                71, 91,
                (compactTileSides) -> new EngineTile(calculateSimpleSides(compactTileSides), false)));

        // Double Engines
        pile.addAll(createListOfTiles(
                List.of("010E", "310E", "020E", "130E", "111E", "222E", "032E", "303E", "023E"),
                92, 100,
                (compactTileSides) -> new EngineTile(calculateSimpleSides(compactTileSides), true)));

        // Cannons
        pile.addAll(createListOfTiles(
                List.of("0c01", "0c01", "0c02", "0c02", "1c00",
                        "1c02", "1c03", "2c00", "2c01", "2c03",
                        "3c01", "0c10", "0c12", "0c13", "1c11",
                        "2c13", "3c10", "0c20", "0c21", "0c23",
                        "1c20", "1c23", "2c22", "0c32", "2c30"),
                101, 125,
                (compactTileSides) -> new CannonTile(calculateSimpleSides(compactTileSides), false)));

        // Double Cannons
        pile.addAll(createListOfTiles(
                List.of("0C01", "0C02", "1C03", "3C00", "3C02",
                        "1C12", "2C10", "0C23", "2C21", "0C30", "0C31"),
                126, 136,
                (compactTileSides) -> new CannonTile(calculateSimpleSides(compactTileSides), true)));

        // Life Supports for Brown Alien
        pile.addAll(createListOfTiles(
                List.of("1110", "1210", "0030", "0031", "2030", "0130"),
                137, 142,
                (compactTileSides) -> new LifeSupportSystemTile(calculateSimpleSides(compactTileSides),
                        LoadableType.BROWN_ALIEN)));

        // Life Supports for Purple Alien
        pile.addAll(createListOfTiles(
                List.of("2120", "2220", "0030", "0032", "1030", "0230"),
                143, 148,
                (compactTileSides) -> new LifeSupportSystemTile(calculateSimpleSides(compactTileSides),
                        LoadableType.PURPLE_ALIEN)));

        // Shield Generators
        pile.addAll(createListOfTiles(
                List.of("ds13", "sd11", "df12", "ss23", "fs22", "fd21", "ss31", "fs32"),
                149, 156,
                (compactTileSides) -> new ShieldGeneratorTile(calculateSimpleSides(compactTileSides),
                        calculateShieldedSides(compactTileSides))));

        return pile;
    }


}

