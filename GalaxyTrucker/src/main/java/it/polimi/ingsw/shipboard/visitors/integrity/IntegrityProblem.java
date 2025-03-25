package src.main.java.it.polimi.ingsw.shipboard.visitors.integrity;

import src.main.java.it.polimi.ingsw.shipboard.tiles.Tile;
import src.main.java.it.polimi.ingsw.shipboard.TileCluster;

import java.util.ArrayList;
import java.util.List;

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
    private final List<TileCluster> clustersToRemove;

    /** Clusters of tiles where exactly one needs to be kept. */
    private final List<TileCluster> clustersToKeep;

    /*
     * POSSIBLE CASES:
     * - If clustersToKeep.isEmpty():
     *     → The game stops for this player.
     *
     * - If clustersToKeep.size() >= 1:
     *     → If the intersection of the set of tiles to keep with any set of tiles to remove is not null,
     *       all the tiles in the intersection will be removed.
     *
     * - If clustersToKeep.size() >= 2:
     *     → If the intersection of one set of tiles to keep with another set of tiles to keep is not null,
     *       all the tiles in the intersection will be kept.
     */

    /**
     * Constructs an integrity problem with the given tile clusters to remove and to keep.
     *
     * @param clustersToRemove a list of tile clusters that must be removed
     * @param clustersToKeep a list of tile clusters where exactly one needs to be kept
     */
    public IntegrityProblem(List<TileCluster> clustersToRemove, List<TileCluster> clustersToKeep) {
        this.clustersToRemove = clustersToRemove;
        this.clustersToKeep = clustersToKeep;
    }

    /**
     * Creates an integrity problem for an intrinsically incorrect tile.
     * <p>
     * This problem occurs after welding the shipboard when a tile is inherently misplaced
     * and must be removed while keeping the rest of the ship intact.
     * </p>
     *
     * @param wrongTile the tile that is incorrectly placed
     * @param allTiles the complete list of tiles on the shipboard
     * @return an {@code IntegrityProblem} instance representing the issue
     */
    public static IntegrityProblem createIntrinsicallyWrongProblem(Tile wrongTile, List<Tile> allTiles) {
        List<Tile> otherTiles = new ArrayList<>(allTiles);
        otherTiles.remove(wrongTile);

        return new IntegrityProblem(List.of(new TileCluster(wrongTile)), List.of(new TileCluster(otherTiles)));
    }

    /**
     * Creates an integrity problem due to an illegal welding between two conflicting tiles.
     * <p>
     * This problem arises after welding the shipboard when two tiles create an invalid connection.
     * The solution involves creating two possible ship configurations, each removing one of the conflicting tiles.
     * </p>
     *
     * @param conflictingTile1 the first tile involved in the illegal welding
     * @param conflictingTile2 the second tile involved in the illegal welding
     * @param allTiles the complete list of tiles on the shipboard
     * @return an {@code IntegrityProblem} instance representing the issue
     */
    public static IntegrityProblem createIllegalWeldingProblem(Tile conflictingTile1, Tile conflictingTile2,
                                                               List<Tile> allTiles) {
        /* TODO:
         * Create two clusters to keep:
         * 1. The first containing all the tiles except those that would be disconnected from the main cabin
         *    if conflictingTile2 was removed.
         * 2. The second containing all the tiles except those that would be disconnected from the main cabin
         *    if conflictingTile1 was removed.
         */
        List<TileCluster> clustersToKeep = new ArrayList<>();

        return new IntegrityProblem(new ArrayList<>(0), clustersToKeep);
    }

    /**
     * Creates an integrity problem where the ship is split into separate parts.
     * <p>
     * This problem can occur after welding the shipboard or during the flight phase.
     * If parts of the ship become disconnected, the ones without human crew will be removed,
     * while the others must be kept.
     * </p>
     *
     * @param separatedShipParts a list of tile clusters representing separate parts of the ship
     * @return an {@code IntegrityProblem} instance representing the issue
     */
    public static IntegrityProblem createSeparateShipPartsProblem(List<TileCluster> separatedShipParts) {
        /* TODO:
         * Parse ship parts and determine:
         * - Clusters without human crew should be added to 'clustersToRemove'.
         * - The remaining clusters should be added to 'clustersToKeep'.
         */
        List<TileCluster> clustersToRemove = new ArrayList<>();
        List<TileCluster> clustersToKeep = new ArrayList<>();

        return new IntegrityProblem(clustersToRemove, clustersToKeep);
    }
}

