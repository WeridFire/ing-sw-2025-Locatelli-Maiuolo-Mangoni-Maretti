package it.polimi.ingsw.shipboard.integrity;


import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.enums.Rotation;
import it.polimi.ingsw.shipboard.LoadableType;
import it.polimi.ingsw.shipboard.SideType;
import it.polimi.ingsw.shipboard.TileCluster;
import it.polimi.ingsw.shipboard.tiles.*;
import it.polimi.ingsw.shipboard.tiles.exceptions.NotFixedTileException;
import it.polimi.ingsw.shipboard.visitors.TileVisitor;
import it.polimi.ingsw.util.Coordinates;
import it.polimi.ingsw.util.Pair;

import java.util.*;


/**
 * Visitor to check for integrity problems. The first encountered must be resolved.
 */
public class VisitorCheckIntegrity implements TileVisitor {
    private final Map<Coordinates, TileSkeleton> visitedTiles;
    private final List<TileCluster> clusters;
    private final Set<TileSkeleton> intrinsicallyWrongTiles;
    private final List<Pair<TileSkeleton>> illegallyWeldedTiles;
    private final Set<TileSkeleton> tilesWithHumans;
    private TileSkeleton mainCabin;

    public VisitorCheckIntegrity() {
        visitedTiles = new HashMap<>();
        clusters = new ArrayList<>();
        intrinsicallyWrongTiles = new HashSet<>();
        illegallyWeldedTiles = new ArrayList<>();
        tilesWithHumans = new HashSet<>();
    }

    @Override
    public void visitStructural(StructuralTile tile) {
        addToClusters(tile);
    }

    @Override
    public void visitLifeSupportSystem(LifeSupportSystemTile tile) {
        addToClusters(tile);
    }

    @Override
    public void visitCargoHold(CargoHoldTile tile) {
        addToClusters(tile);
    }

    // TODO: manage clusters with crew and those without -> if without crew can not keep

    @Override
    public void visitCabin(CabinTile tile) {
        addToClusters(tile);
        if (tile.getLoadedItems().stream().anyMatch(i -> i == LoadableType.HUMAN)) {
            tilesWithHumans.add(tile);
        }
    }

    @Override
    public void visitMainCabin(CabinTile tile) {
        mainCabin = tile;
        visitCabin(tile);
    }

    @Override
    public void visitBatteryComponent(BatteryComponentTile tile) {
        addToClusters(tile);
    }

    @Override
    public void visitCannon(CannonTile tile) {
        addToClusters(tile);
    }

    @Override
    public void visitEngine(EngineTile tile) {
        addToClusters(tile);
        if (tile.hasPower(Direction.EAST) || tile.hasPower(Direction.WEST) || tile.hasPower(Direction.NORTH)) {
            intrinsicallyWrongTiles.add(tile);
        }
    }

    @Override
    public void visitShieldGenerator(ShieldGeneratorTile tile) {
        addToClusters(tile);
    }

    private static boolean areDirectlyWelded(TileSkeleton tile1, TileSkeleton tile2) {
        Direction dir = tile1.getNeighborDirection(tile2);
        if (dir == null) return false;
        return SideType.areWeldable(tile1.getSide(dir), tile2.getSide(dir.getRotated(Rotation.OPPOSITE)));
    }

    private void addToClusters(TileSkeleton tile) {
        Coordinates tileCoordinates = tile.forceGetCoordinates();

        /* first of all signal if it would be illegally welded */
        for (Coordinates neighborCoordinates : tileCoordinates.getNeighbors()) {
            TileSkeleton neighborTile = visitedTiles.get(neighborCoordinates);
            if (neighborTile != null) {  // there is threat to be illegally welded
                Direction neighborDirection = tileCoordinates.getNeighborDirection(neighborCoordinates);
                if (!SideType.areCompatible(tile.getSide(neighborDirection),
                        neighborTile.getSide(neighborDirection.getRotated(Rotation.OPPOSITE)))) {
                    illegallyWeldedTiles.add(new Pair<>(tile, neighborTile));
                }
            }
        }
        /**/

        List<TileCluster> weldedClusters = new ArrayList<>();

        // store as visited tile
        visitedTiles.put(tileCoordinates, tile);

        // store all the clusters welded to this tile
        for (TileCluster cluster : clusters) {
            for (TileSkeleton clusterTile : cluster.getTiles()) {
                if (areDirectlyWelded(tile, clusterTile)) {
                    weldedClusters.add(cluster);
                    break;
                }
            }
        }

        // if no cluster is welded to this tile -> this tile creates a new cluster
        if (weldedClusters.isEmpty()) {
            clusters.add(new TileCluster(Set.of(tile)));
        }
        else {
            TileCluster weldedCluster = weldedClusters.getFirst();
            // merge welded clusters that would have been disconnected if this tile wasn't there.
            for (int i = 1; i < weldedClusters.size(); i++) {
                weldedCluster.merge(weldedClusters.get(i));
            }
            // remove welded clusters in excess (all but the first)
            weldedClusters.remove(weldedCluster);
            clusters.removeAll(weldedClusters);
            // actually add this tile to the cluster
            weldedCluster.addTile(tile);
        }
    }

    public IntegrityProblem getProblem(boolean isAssemblePhase) {
        return new IntegrityProblem(clusters, intrinsicallyWrongTiles, illegallyWeldedTiles,
                // if not assigned -> humans are not necessary to keep cluster, but main cabin is.
                isAssemblePhase ? Set.of(mainCabin) : tilesWithHumans);
    }
}
