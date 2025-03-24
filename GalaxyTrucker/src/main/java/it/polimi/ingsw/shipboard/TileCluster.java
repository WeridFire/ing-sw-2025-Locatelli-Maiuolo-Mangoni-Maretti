package src.main.java.it.polimi.ingsw.shipboard;

import src.main.java.it.polimi.ingsw.shipboard.exceptions.TileAlreadyPresentException;
import src.main.java.it.polimi.ingsw.shipboard.tiles.Tile;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a cluster of connected tiles, with an optional main tile.
 * A tile cluster groups tiles that are considered part of the same unit (e.g. all and only those welded together).
 */
public class TileCluster {
    private final List<Tile> tiles;
    private Tile mainTile;

    /**
     * Creates an empty tile cluster with no main tile.
     */
    public TileCluster() {
        tiles = new ArrayList<>();
        mainTile = null;
    }

    /**
     * Creates a tile cluster with a single tile set as the main tile.
     *
     * @param mainTile the tile to be set as the main tile
     */
    public TileCluster(Tile mainTile) {
        tiles = new ArrayList<>();
        tiles.add(mainTile);
        this.mainTile = mainTile;
    }

    /**
     * Creates a tile cluster containing a predefined list of tiles.
     * The main tile is not set by default.
     *
     * @param tiles the list of tiles to be added to the cluster
     */
    public TileCluster(List<Tile> tiles) {
        this.tiles = new ArrayList<>(tiles);
        mainTile = null;
    }

    /**
     * Adds a tile to the cluster.
     *
     * @param tile the tile to be added
     * @throws TileAlreadyPresentException if the tile is already part of the cluster
     */
    public void addTile(Tile tile) throws TileAlreadyPresentException {
        if (tiles.contains(tile)) {
            throw new TileAlreadyPresentException("Attempt to add a tile already present in the cluster");
        }

        tiles.add(tile);
    }

    /**
     * Adds a tile to the cluster and sets it as the main tile.
     *
     * @param tile the tile to be set as the main tile
     * @throws TileAlreadyPresentException if there is already a main tile in the cluster
     */
    public void addAsMainTile(Tile tile) throws TileAlreadyPresentException {
        if (mainTile != null) {
            throw new TileAlreadyPresentException(
                    "Attempt to add a tile as main tile in a cluster with an already set main tile");
        }

        addTile(tile);
        mainTile = tile;
    }

    /**
     * Checks if the cluster has a main tile.
     *
     * @return {@code true} if a main tile is set, {@code false} otherwise
     */
    public boolean hasMainTile() {
        return mainTile != null;
    }

    /**
     * Retrieves the main tile of the cluster.
     *
     * @return the main tile, or {@code null} if no main tile is set
     */
    public Tile getMainTile() {
        return mainTile;
    }

    /**
     * Retrieves the list of tiles in the cluster.
     *
     * @return a list of tiles in the cluster
     */
    public List<Tile> getTiles() {
        return tiles;
    }
}
