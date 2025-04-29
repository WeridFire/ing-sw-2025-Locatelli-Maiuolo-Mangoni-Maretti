package it.polimi.ingsw.shipboard.integrity;

import it.polimi.ingsw.shipboard.TileCluster;
import it.polimi.ingsw.shipboard.tiles.TileSkeleton;
import it.polimi.ingsw.shipboard.tiles.exceptions.NotFixedTileException;
import it.polimi.ingsw.util.Coordinates;

import java.util.*;

/**
 * Represents an integrity issue with a shipboard configuration.
 * <p>
 * An integrity problem arises when the ship structure contains clusters of tiles
 * that must be removed or when conflicting tile connections exist.
 * </p>
 * <p>
 * There are three main types of integrity problems:
 * <ul>
 *     <li>Intrinsically wrong tiles that must be removed (engines not pointing down).</li>
 *     <li>Illegally welded tiles that create conflicting connections.</li>
 *     <li>Separate ship parts that are no longer connected as a whole.</li>
 * </ul>
 * </p>
 */
public class IntegrityProblem {
    /** Clusters of tiles that must be completely removed. */
    private final Set<TileCluster> clustersToRemove;

    /** Clusters of tiles where exactly one needs to be kept. */
    private final Set<TileCluster> clustersToKeep;

    /*
     * POSSIBLE CASES:
     * - If clustersToKeep.isEmpty():
     *     -> The game stops for this player.
     *
     * - If clustersToKeep.size() >= 1:
     *     -> If the intersection of the set of tiles to keep with any set of tiles to remove is not null,
     *       all the tiles in the intersection will be removed.
     *
     * - If clustersToKeep.size() >= 2:
     *     -> If the intersection of one set of tiles to keep with another set of tiles to keep is not null,
     *       all the tiles in the intersection will be kept.
     */

    /**
     * Constructs an integrity problem with the given tile clusters to remove and to keep.
     *
     * @param clustersToRemove a list of tile clusters that must be removed
     * @param clustersToKeep a list of tile clusters where exactly one needs to be kept
     */
    public IntegrityProblem(Set<TileCluster> clustersToRemove, Set<TileCluster> clustersToKeep) {
        this.clustersToRemove = clustersToRemove;
        this.clustersToKeep = clustersToKeep;
    }


    public IntegrityProblem(Map<Coordinates, TileSkeleton> visitedTiles,
                            List<TileCluster> clusters,
                            Set<TileSkeleton> intrinsicallyWrongTiles,
                            List<Map.Entry<TileSkeleton, TileSkeleton>> illegallyWeldedTiles,
                            Set<TileSkeleton> tilesWithHumans) {

        this.clustersToRemove = new HashSet<>();
        this.clustersToKeep = new HashSet<>();

        // add intrinsically wrong tiles as 1-tile clusters to remove
        for (TileSkeleton intrinsicallyWrongTile : intrinsicallyWrongTiles) {
            clustersToRemove.add(new TileCluster(intrinsicallyWrongTile));
        }

        // add multiple clusters as clusters to keep -> only one will be kept: ok
        clustersToKeep.addAll(clusters);

        /* BFS tree search all the clusters for badly connected tiles (one way and the other)
            add all the clusters as to keep -> only one will be kept: ok
            note: with implementation that keeps intersection of clusters to keep, is ok to mask with
            previously set clusters.
         */
        for (Map.Entry<TileSkeleton, TileSkeleton> illegallyWeldedTile : illegallyWeldedTiles) {
            clustersToKeep.add(exploreCluster(visitedTiles,
                    illegallyWeldedTile.getKey(), illegallyWeldedTile.getValue()));
            clustersToKeep.add(exploreCluster(visitedTiles,
                    illegallyWeldedTile.getValue(), illegallyWeldedTile.getKey()));
        }

        // now, some of the cluster to keep may have no humans -> in that case, those clusters need to be removed
        Iterator<TileCluster> iterator = clustersToKeep.iterator();
        while (iterator.hasNext()) {
            TileCluster cluster = iterator.next();
            boolean hasHuman = false;
            for (TileSkeleton tile : cluster.getTiles()) {
                if (tilesWithHumans.contains(tile)) {
                    hasHuman = true;
                    break;
                }
            }
            if (!hasHuman) {
                iterator.remove();  // remove from clustersToKeep set
                clustersToRemove.add(cluster);  // add to clustersToRemove
            }
        }
    }


    /**
     * Explores a connected cluster of tiles using Breadth-First Search (BFS).
     *
     * @param visitedTiles A map of coordinates to their corresponding tiles.
     * @param startingTile The tile where exploration begins.
     * @param tileToIgnore A tile to be ignored during exploration (can be null).
     * @return The updated TileCluster containing all connected tiles.
     * @throws RuntimeException If the tile's neighbors cannot be determined.
     */
    private TileCluster exploreCluster(Map<Coordinates, TileSkeleton> visitedTiles,
                                       TileSkeleton startingTile, TileSkeleton tileToIgnore) {

        TileCluster cluster = new TileCluster(startingTile);

        // Use a queue to explore tiles iteratively (BFS)
        Queue<TileSkeleton> queue = new LinkedList<>();
        queue.add(startingTile);

        while (!queue.isEmpty()) {
            TileSkeleton tile = queue.poll();

            // Skip if already processed
            if (cluster.getTiles().contains(tile)) {
                continue;
            }

            // Add the tile to the cluster
            cluster.addTile(tile);

            Set<Coordinates> neighborsLocations;
            try {
                // Retrieve the coordinates of neighboring tiles
                neighborsLocations = tile.getCoordinates().getNeighbors();
            } catch (NotFixedTileException e) {
                throw new RuntimeException(e);  // should never happen -> runtime error
            }

            // Collect valid neighbors (tiles that exist and are not ignored)
            for (Coordinates coord : neighborsLocations) {
                TileSkeleton neighbor = visitedTiles.get(coord);
                if (neighbor != null && neighbor != tileToIgnore && !cluster.getTiles().contains(neighbor)) {
                    queue.add(neighbor);
                }
            }
        }

        return cluster;
    }

    /**
     * @return {@code true} if this integrity problem is an actual problem,
     * otherwise {@code false} if it represents a normal state of the ship.
     */
    public boolean isProblem() {
        return !clustersToRemove.isEmpty() || (clustersToKeep.size() != 1);
    }

    /**
     * @return a copy of the set of clusters to remove
     */
    public Set<TileCluster> getClustersToRemove() {
        return new HashSet<>(clustersToRemove);
    }

    /**
     * @return a copy of the set of clusters to keep as a list to enumerate its elements
     */
    public List<TileCluster> getClustersToKeep() {
        return new ArrayList<>(clustersToKeep);
    }

}

