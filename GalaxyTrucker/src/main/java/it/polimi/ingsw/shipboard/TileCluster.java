package it.polimi.ingsw.shipboard;

import it.polimi.ingsw.shipboard.exceptions.TileAlreadyPresentException;
import it.polimi.ingsw.shipboard.tiles.Tile;
import it.polimi.ingsw.shipboard.tiles.TileSkeleton;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a cluster of connected tiles, with an optional main tile.
 * A tile cluster groups tiles that are considered part of the same unit (e.g. all and only those welded together).
 */
public class TileCluster implements Serializable {
    private final Set<TileSkeleton> tiles;
    private TileSkeleton mainTile;

    /**
     * Creates an empty tile cluster with no main tile.
     */
    public TileCluster() {
        tiles = new HashSet<>();
        mainTile = null;
    }

    /**
     * Creates a tile cluster with a single tile set as the main tile.
     *
     * @param mainTile the tile to be set as the main tile
     */
    public TileCluster(TileSkeleton mainTile) {
        tiles = new HashSet<>();
        tiles.add(mainTile);
        this.mainTile = mainTile;
    }

    /**
     * Creates a tile cluster containing a predefined list of tiles.
     * The main tile is not set by default.
     *
     * @param tiles the list of tiles to be added to the cluster
     */
    public TileCluster(Set<TileSkeleton> tiles) {
        this.tiles = new HashSet<>(tiles);
        mainTile = null;
    }

    /**
     * Adds a tile to the cluster.
     * If the tile is already part of the cluster, does nothing.
     *
     * @param tile the tile to be added
     */
    public void addTile(TileSkeleton tile) {
        tiles.add(tile);
    }

    /**
     * Adds a tile to the cluster and sets it as the main tile.
     *
     * @param tile the tile to be set as the main tile
     * @throws TileAlreadyPresentException if there is already a main tile in the cluster
     */
    public void addAsMainTile(TileSkeleton tile) throws TileAlreadyPresentException {
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
     * @return a set of tiles in the cluster
     */
    public Set<TileSkeleton> getTiles() {
        return tiles;
    }

    /**
     * Merge all the tiles from {@code otherCluster} in this cluster.
     * If this cluster does not have a main tile: the main tile becomes the {@code otherCluster}'s,
     * otherwise it does not change.
     */
    public void merge(TileCluster otherCluster) {
        tiles.addAll(otherCluster.getTiles());
    }
}
