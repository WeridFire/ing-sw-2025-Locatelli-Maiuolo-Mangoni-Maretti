package src.main.java.it.polimi.ingsw.shipboard.tiles;

import src.main.java.it.polimi.ingsw.enums.Direction;

public class Tile extends TileSkeleton<String, Integer> {

    /**
     * Create a new tile with four specified sides (East, North, West, South)
     * and a specified content (rotation does not apply to it).
     * @param eastSide Tile's East side
     * @param northSide Tile's North side
     * @param westSide Tile's West side
     * @param southSide Tile's South side
     * @param content Tile's content
     */
    public Tile(String eastSide, String northSide, String westSide, String southSide, int content) {
        super(Direction.sortedArray(eastSide, northSide, westSide, southSide).toArray(new String[0]), content);
    }


}
