package it.polimi.ingsw.shipboard.integrity;

import it.polimi.ingsw.shipboard.TileCluster;
import it.polimi.ingsw.shipboard.exceptions.TileAlreadyPresentException;
import it.polimi.ingsw.shipboard.tiles.TileSkeleton;
import it.polimi.ingsw.util.Coordinates;
import it.polimi.ingsw.util.Pair;
import it.polimi.ingsw.view.cli.ANSI;

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
                            List<Pair<TileSkeleton>> illegallyWeldedTiles,
                            Set<TileSkeleton> tilesWithHumans) {

        this.clustersToRemove = new HashSet<>();
        this.clustersToKeep = new HashSet<>();

        // add intrinsically wrong tiles as 1-tile clusters to remove
        for (TileSkeleton intrinsicallyWrongTile : intrinsicallyWrongTiles) {
            clustersToRemove.add(new TileCluster(intrinsicallyWrongTile));
        }

        // add multiple clusters as clusters to keep -> only one will be kept: ok
        clustersToKeep.addAll(clusters);

        /* check debug */
        System.out.println("clusters to remove 1");
        for (TileCluster cluster : clustersToRemove) {
            System.out.println(cluster.toString(ANSI.RED));
        }
        System.out.println("clusters to keep 1");
        for (TileCluster cluster : clustersToKeep) {
            System.out.println(cluster.toString(ANSI.YELLOW));
        } /**/

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

        /* check debug */
        System.out.println("clusters to remove 2");
        for (TileCluster cluster : clustersToRemove) {
            System.out.println(cluster.toString(ANSI.RED));
        }
        System.out.println("clusters to keep 2");
        for (TileCluster cluster : clustersToKeep) {
            System.out.println(cluster.toString(ANSI.YELLOW));
        } /**/

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

                    TileCluster c1 = new TileCluster(processing.getTiles());
                    TileCluster c2 = new TileCluster(processing.getTiles());

                    c1.getTiles().remove(illegallyWeldedTilePair.getSecond());
                    c2.getTiles().remove(illegallyWeldedTilePair.getFirst());

                    System.out.println("c1: " + c1.toString(ANSI.WHITE));
                    System.out.println("c2: " + c2.toString(ANSI.WHITE));
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
        /**/

        /* check debug */
        System.out.println("clusters to remove 3");
        for (TileCluster cluster : clustersToRemove) {
            System.out.println(cluster.toString(ANSI.RED));
        }
        System.out.println("clusters to keep 3");
        for (TileCluster cluster : clustersToKeep) {
            System.out.println(cluster.toString(ANSI.YELLOW));
        } /**/

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

