package it.polimi.ingsw.model.shipboard.integrity;

import it.polimi.ingsw.model.shipboard.TileCluster;
import it.polimi.ingsw.model.shipboard.exceptions.NoTileFoundException;
import it.polimi.ingsw.model.shipboard.tiles.TileSkeleton;
import it.polimi.ingsw.util.Pair;

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
    public static final String TAG = "IntegrityProblem";

    /** Clusters of tiles that must be completely removed. */
    private final Set<TileCluster> clustersToRemove;

    /** Clusters of tiles where exactly one needs to be kept. */
    private final Set<TileCluster> clustersToKeep;

    /** Info about absence of humans on board, iff problem to continue */
    private final boolean noMoreHumans;

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

    public IntegrityProblem(List<TileCluster> clusters,
                            Set<TileSkeleton> intrinsicallyWrongTiles,
                            List<Pair<TileSkeleton>> illegallyWeldedTiles,
                            Set<TileSkeleton> tilesWithHumans) {

        this.clustersToRemove = new HashSet<>();
        this.clustersToKeep = new HashSet<>();

        if (tilesWithHumans.isEmpty()) {
            noMoreHumans = true;
            return;
        } else {
            noMoreHumans = false;
        }

        // add intrinsically wrong tiles as 1-tile clusters to remove
        for (TileSkeleton intrinsicallyWrongTile : intrinsicallyWrongTiles) {
            clustersToRemove.add(new TileCluster(intrinsicallyWrongTile));
        }

        // add multiple clusters as clusters to keep -> only one will be kept: ok
        clustersToKeep.addAll(clusters);

        // now, some of the cluster to keep may have no humans -> in that case, those clusters need to be removed
        Iterator<TileCluster> iterator = clustersToKeep.iterator();
        while (iterator.hasNext()) {
            TileCluster cluster = iterator.next();
            boolean hasHuman = cluster.containsAny(tilesWithHumans);
            if (!hasHuman) {
                iterator.remove();  // remove from clustersToKeep set
                clustersToRemove.add(cluster);  // add to clustersToRemove
            }
        }

        /* BFS tree search all the remaining clusters for badly connected tiles (one way and the other) among those
            in the same cluster and "split" it in the clusters to keep -> only one will be kept: ok
            note: with implementation that keeps intersection of clusters to keep, is ok to mask with
            previously set clusters.
         */
        Queue<TileCluster> oldClustersToKeep = new LinkedList<>(clustersToKeep);
        clustersToKeep.clear();
        boolean modified;
        while (!oldClustersToKeep.isEmpty()) {
            TileCluster processing = oldClustersToKeep.poll();
            modified = false;

            for (Pair<TileSkeleton> illegallyWeldedTilePair : illegallyWeldedTiles) {
                if (illegallyWeldedTilePair.isIn(processing.getTiles())) {

                    TileCluster c1, c2;
                    try {
                        c1 = processing.excluded(illegallyWeldedTilePair.getFirst(),
                                illegallyWeldedTilePair.getSecond());
                        c2 = processing.excluded(illegallyWeldedTilePair.getSecond(),
                                illegallyWeldedTilePair.getFirst());
                    } catch (NoTileFoundException e) {
                        throw new RuntimeException(e);  // should never happen -> runtime exception
                    }

                    oldClustersToKeep.add(c1);
                    oldClustersToKeep.add(c2);

                    modified = true;
                    break;
                }
            }

            if (!modified) {
                clustersToKeep.add(processing);
            }
        }

        /* to avoid the possibility of keeping a cluster without humans, remove all of those
        * ---
        * This is an edge case, keep this code snippet even if it seems redundant from a superficial overview.
        * In particular: if a cluster
        *  1. is in clustersToKeep because it contains humans in exactly one tile,
        * AND
        *  2. the humans tile is illegally welded to another
        * THEN
        * that cluster would have been split into two sub-clusters, one of which contains no humans.
        * With this code snippet, we consider as a cluster to be kept only the valid sub-cluster (with humans) and
        * the tiles in the invalid one that are not also in the valid sub-cluster are considered as a cluster
        * to be removed.
        */
        iterator = clustersToKeep.iterator();
        while (iterator.hasNext()) {
            TileCluster cluster = iterator.next();
            boolean hasHuman = cluster.containsAny(tilesWithHumans);
            if (!hasHuman) {
                iterator.remove();  // remove from clustersToKeep set
                // add the tiles exclusively in this cluster into clustersToRemove
                clustersToRemove.add(cluster.exclusive(clustersToKeep));
            }
        }

    }

    /**
     * @return {@code true} if this integrity problem is an actual problem,
     * otherwise {@code false} if it represents a normal state of the ship.
     */
    public boolean isProblem() {
        return !clustersToRemove.isEmpty() || (clustersToKeep.size() != 1);
    }

    /**
     * @return {@code true} if this integrity problem is caused by having zero humans on board of the ship,
     * regardless of ship structure; {@code false} otherwise (if at least one human is present).
     */
    public boolean isNoMoreHumansProblem() {
        return noMoreHumans;
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

