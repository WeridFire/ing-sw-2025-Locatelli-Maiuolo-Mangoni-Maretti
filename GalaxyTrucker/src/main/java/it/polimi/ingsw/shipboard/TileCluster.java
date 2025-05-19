package it.polimi.ingsw.shipboard;

import com.fasterxml.jackson.annotation.JsonIgnore;
import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.enums.Rotation;
import it.polimi.ingsw.shipboard.exceptions.NoTileFoundException;
import it.polimi.ingsw.shipboard.exceptions.TileAlreadyPresentException;
import it.polimi.ingsw.shipboard.tiles.Tile;
import it.polimi.ingsw.shipboard.tiles.TileSkeleton;
import it.polimi.ingsw.shipboard.tiles.exceptions.NotFixedTileException;
import it.polimi.ingsw.util.Coordinates;
import it.polimi.ingsw.view.cli.ANSI;

import java.io.Serializable;
import java.util.*;

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
        if (mainTile == null) {
            mainTile = otherCluster.mainTile;
        }
    }

    public TileCluster excluded(TileSkeleton start, TileSkeleton toExclude) throws NoTileFoundException {
        if (!getTiles().contains(start)) {
            throw new NoTileFoundException(
                    "Attempt to exclude tile 'start' from cluster, but it is already not contained");
        }
        if (!getTiles().contains(toExclude)) {
            throw new NoTileFoundException(
                    "Attempt to exclude tile 'toExclude' from cluster, but it is already not contained");
        }
        if (Objects.equals(start, toExclude)) {
            throw new IllegalArgumentException("start and toExclude can not be the same tile");
        }

        TileCluster newCluster = new TileCluster(start);

        // trivial case: start is not placed -> the new cluster is all the non-placed tiles
        if (!start.isPlaced()) {
            for (TileSkeleton tile : getTiles()) {
                if (!tile.isPlaced() && !tile.equals(toExclude)) {
                    newCluster.addTile(tile);
                }
            }
            return newCluster;
        }
        // else: create the new cluster based on tiles welded to each other from start, except toExclude

        // helpful map of (place -> tile>) occupied places from the tiles in this cluster
        HashMap<Coordinates, TileSkeleton> occupiedPlaces = new HashMap<>();
        for (TileSkeleton tile : getTiles()) {
            try {
                Coordinates place = tile.getCoordinates();
                occupiedPlaces.put(place, tile);
            } catch (NotFixedTileException e) {
                // do not store as placed tile
            }
        }

        Queue<TileSkeleton> tilesToVisit = new LinkedList<>();
        Set<TileSkeleton> alreadyVisited = new HashSet<>();
        tilesToVisit.add(start);

        while (!tilesToVisit.isEmpty()) {
            TileSkeleton visited = tilesToVisit.poll();
            if (alreadyVisited.contains(visited)) {
                continue;
            }

            Coordinates visitedCoords = visited.forceGetCoordinates();
            for (Coordinates neighbor : visitedCoords.getNeighbors()) {
                if (occupiedPlaces.containsKey(neighbor)) {
                    TileSkeleton neighborTile = occupiedPlaces.get(neighbor);
                    Direction neighborDirection = visitedCoords.getNeighborDirection(neighbor);
                    if (
                            !neighborTile.equals(toExclude)
                            && SideType.areWeldable(
                                    visited.getSide(neighborDirection),
                                    neighborTile.getSide(neighborDirection.getRotated(Rotation.OPPOSITE))
                            )
                    ) {
                        // neighborTile is tile to add in the new cluster
                        newCluster.addTile(neighborTile);
                        tilesToVisit.add(neighborTile);
                    }
                }
            }

            alreadyVisited.add(visited);
        }

        return newCluster;
    }

    @JsonIgnore
    public String toString(String fgColor) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (TileSkeleton tile : tiles) {
            builder.append(fgColor)
                    .append(tile.forceGetCoordinates())
                    .append(ANSI.RESET);
            builder.append(", ");
        }
        int len = builder.length();
        builder.delete(len - 2, len);
        builder.append("]");
        return builder.toString();
    }

    @Override @JsonIgnore
    public String toString() {
        return toString(ANSI.YELLOW);
    }
}
