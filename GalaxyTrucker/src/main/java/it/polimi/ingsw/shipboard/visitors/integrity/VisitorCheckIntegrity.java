package it.polimi.ingsw.shipboard.visitors.integrity;


import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.enums.Rotation;
import it.polimi.ingsw.shipboard.SideType;
import it.polimi.ingsw.shipboard.TileCluster;
import it.polimi.ingsw.shipboard.tiles.*;
import it.polimi.ingsw.shipboard.tiles.exceptions.NotFixedTileException;
import it.polimi.ingsw.shipboard.visitors.TileVisitor;
import it.polimi.ingsw.util.Coordinates;

import java.util.*;


/**
 * Visitor to check for integrity problems. The first encountered must be resolved.
 */
public class VisitorCheckIntegrity implements TileVisitor {
    private final Map<Coordinates, TileSkeleton<SideType>> visitedTiles;
    private final List<TileCluster> clusters;
    private final Set<TileSkeleton<SideType>> intrinsicallyWrongTiles;
    private final List<Map.Entry<TileSkeleton<SideType>, TileSkeleton<SideType>>> illegallyWeldedTiles;

    public VisitorCheckIntegrity() {
        visitedTiles = new HashMap<>();
        clusters = new ArrayList<>();
        intrinsicallyWrongTiles = new HashSet<>();
        illegallyWeldedTiles = new ArrayList<>();
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

    @Override
    public void visitCabin(CabinTile tile) {
        addToClusters(tile);
    }

    @Override
    public void visitMainCabin(CabinTile tile) {
        addToClusters(tile);
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

    private static boolean areDirectlyWelded(TileSkeleton<SideType> tile1, TileSkeleton<SideType> tile2) {
        Direction dir = tile1.getNeighborDirection(tile2);
        if (dir == null) return false;
        return SideType.areWeldable(tile1.getSide(dir), tile2.getSide(dir.getRotated(Rotation.OPPOSITE)));
    }

    private void addToClusters(TileSkeleton<SideType> tile) {
        List<TileCluster> weldedClusters = new ArrayList<>();

        // store as visited tile
        try {
            visitedTiles.put(tile.getCoordinates(), tile);
        } catch (NotFixedTileException e) {
            throw new RuntimeException(e);  // should never happen -> runtime error
        }

        // store all the clusters welded to this tile
        for (TileCluster cluster : clusters) {
            for (TileSkeleton<SideType> clusterTile : cluster.getTiles()) {
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

    public IntegrityProblem getProblem() {
        return new IntegrityProblem(visitedTiles, clusters, intrinsicallyWrongTiles, illegallyWeldedTiles);
    }
}
