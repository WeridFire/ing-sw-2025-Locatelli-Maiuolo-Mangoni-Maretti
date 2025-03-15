package src.main.java.it.polimi.ingsw.shipboard.tiles;

import src.main.java.it.polimi.ingsw.enums.ConnectorType;
import src.main.java.it.polimi.ingsw.shipboard.tiles.content.TileContentBattery;
import src.main.java.it.polimi.ingsw.shipboard.tiles.side.TileSide;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Map;


public class TilesFactory {

    /**
     * Internal record to store Tile and its associated graphic reference.
     */
    private record TileData(Tile tile, String graphicReference) {}


    private static final TilesFactory INSTANCE = new TilesFactory();
    private final AtomicInteger idCounter = new AtomicInteger(0);
    private final Map<Integer, TileData> tileRegistry = new HashMap<>();

    private TilesFactory() {
        initialize();
    }

    private void initialize() {
        registerTile(new Tile(
                new TileSide(ConnectorType.SINGLE),
                new TileSide(ConnectorType.UNIVERSAL),
                new TileSide(ConnectorType.SMOOTH),
                new TileSide(ConnectorType.DOUBLE),
                TileContentBattery.createBatteryComponentContent(2)
        ), "GT-tile-1");
    }

    /**
     * Retrieves the singleton instance of the TileFactory.
     *
     * @return the singleton instance of TileFactory.
     */
    public static TilesFactory getInstance() {
        return INSTANCE;
    }

    private void registerTile(Tile tile, String graphicReference) {
        int tileId = idCounter.incrementAndGet();
        tileRegistry.put(tileId, new TileData(tile, graphicReference));
    }

    /**
     * Retrieves a Tile instance by its unique ID.
     *
     * @param tileId The ID of the tile.
     * @return The Tile instance, or {@code null} if not found.
     */
    public Tile getTile(int tileId) {
        TileData data = tileRegistry.get(tileId);
        return (data != null) ? data.tile() : null;
    }

    /**
     * Retrieves the graphic reference associated with a Tile ID.
     *
     * @param tileId The ID of the tile.
     * @return The graphic reference string, or {@code null} if not found.
     */
    public String getTileGraphic(int tileId) {
        TileData data = tileRegistry.get(tileId);
        return (data != null) ? data.graphicReference() : null;
    }
}

