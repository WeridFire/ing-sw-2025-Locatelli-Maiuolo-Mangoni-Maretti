package src.main.java.it.polimi.ingsw.shipboard1;

import src.main.java.it.polimi.ingsw.enums.BatteryType;
import src.main.java.it.polimi.ingsw.enums.PowerType;
import src.main.java.it.polimi.ingsw.util.Coordinates;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The {@code ShipStatistics} class manages and computes various statistics of a {@link ShipBoard}.
 * It tracks free power, battery-dependent power locations, bonus power locations, crew members, batteries,
 * and the value of goods present on the ship.
 *
 * <p> Other functions can be implemented here for the retrieval of other shipboard statistics. </p>
 *
 * <p>The statistics are cached for performance reasons and are automatically invalidated when the shipboard
 * updates (via the {@link ShipBoardListener} interface).</p>
 */
public class ShipStatistics implements ShipBoardListener {
    /**
     * The ship board associated with this statistics tracker.
     * It provides access to the tiles and triggers updates when modifications occur.
     */
    private final ShipBoard shipBoard;

    /**
     * A cached mapping of free power available on the ship, grouped by power type.
     * The values are recalculated only when invalidated.
     */
    private final Map<PowerType, Float> freePower = new HashMap<>();

    /**
     * A cached mapping of power clusters that require battery activation, grouped by power type.
     * Each entry contains a list of coordinates where power sources are located.
     */
    private final Map<PowerType, List<Coordinates>> batteryDependentPowerLocations = new HashMap<>();

    /**
     * A cached mapping of special power bonuses granted by tiles, grouped by power type.
     * Each entry contains the coordinate of the tile providing the bonus.
     */
    private final Map<PowerType, Coordinates> bonusPowerLocations = new HashMap<>();

    /**
     * The total count of crew members on the ship.
     * This value is computed lazily and cached until invalidated.
     */
    private Integer crewMembersCount = null;

    /**
     * A cached mapping of crew member distribution on the ship.
     * Each entry associates a coordinate with the non-zero number of crew members at that location.
     */
    private Map<Coordinates, Integer> crewMembersLocationQuantity = null;

    /**
     * The total number of batteries available on the ship.
     * This value is computed lazily and cached until invalidated.
     */
    private Integer batteriesCount = null;

    /**
     * A cached mapping of battery distribution on the ship.
     * Each entry associates a coordinate with the non-zero number of batteries at that location.
     */
    private Map<Coordinates, Integer> batteriesLocationQuantity = null;

    /**
     * The total monetary value (in cosmic credits) of all goods stored in the ship.
     * This value is computed lazily and cached until invalidated.
     */
    private Integer goodsValue = null;

    /**
     * The total count of exposed connectors.
     * This value is computed lazily and cached until invalidated.
     */
    private Integer exposedConnectors = null;


    /**
     * Constructs a new {@code ShipStatistics} object and registers it as a listener
     * for changes to the associated {@code ShipBoard}.
     *
     * @param shipBoard the ship board whose statistics are being managed
     */
    public ShipStatistics(ShipBoard shipBoard) {
        this.shipBoard = shipBoard;
        shipBoard.addListener(this);  // Register as listener
        invalidateCache();  // Initialize values as to be checked
    }

    /**
     * Callback method invoked when the ship board is updated.
     * It invalidates the cached statistics to ensure recalculations occur when requested.
     */
    @Override
    public void onShipBoardUpdated() {
        invalidateCache();
    }

    /**
     * Clears all cached values, forcing recalculations when statistics are requested.
     */
    private void invalidateCache() {
        freePower.clear();
        batteryDependentPowerLocations.clear();
        bonusPowerLocations.clear();

        crewMembersCount = null;
        crewMembersLocationQuantity = null;

        batteriesCount = null;
        batteriesLocationQuantity = null;

        goodsValue = null;
        exposedConnectors = null;
    }

    /**
     * Returns the amount of free power available for a given power type.
     * If the value is not cached, it is computed and stored.
     *
     * @param powerType the type of power to retrieve
     * @return the amount of free power available
     */
    public float getFreePower(PowerType powerType) {
        return freePower.computeIfAbsent(powerType, this::calculateFreePower);
    }

    /**
     * Returns the locations of all battery-dependent power clusters for a given power type.
     * If the value is not cached, it is computed and stored.
     *
     * @param powerType the type of power to retrieve
     * @return a list of coordinates representing battery-dependent power locations
     */
    public List<Coordinates> getBatteryPowerCluster(PowerType powerType) {
        return batteryDependentPowerLocations.computeIfAbsent(powerType, this::calculateBatteryPowerCluster);
    }

    /**
     * Returns the location of a tile that provides bonus power of the specified type.
     * If the value is not cached, it is computed and stored.
     *
     * @param powerType the type of bonus power to retrieve
     * @return the coordinates of the bonus power source, or {@code null} if none exists
     */
    public Coordinates getBonusPowerLocation(PowerType powerType) {
        return bonusPowerLocations.computeIfAbsent(powerType, this::calculateBonusPowerLocation);
    }

    /**
     * Returns the total count of crew members on the ship.
     * If the value is not cached, it is computed and stored.
     *
     * @return the total number of crew members
     */
    public int getCrewMembersCount() {
        if (crewMembersCount == null) {
            crewMembersLocationQuantity = calculateCrewMembersLocationQuantity();
            crewMembersCount = crewMembersLocationQuantity.values().stream().mapToInt(Integer::intValue).sum();
        }
        return crewMembersCount;
    }

    /**
     * Returns a mapping of crew member locations and their respective counts.
     * If the value is not cached, it is computed and stored.
     *
     * @return an unmodifiable map of crew member locations and their respective counts
     */
    public Map<Coordinates, Integer> getCrewMembersLocationQuantity() {
        if (crewMembersLocationQuantity == null) {
            getCrewMembersCount(); // Ensures both values are computed together
        }
        return Collections.unmodifiableMap(crewMembersLocationQuantity);
    }

    /**
     * Returns the total number of batteries present on the ship.
     * If the value is not cached, it is computed and stored.
     *
     * @return the total number of batteries
     */
    public int getBatteriesCount() {
        if (batteriesCount == null) {
            batteriesLocationQuantity = calculateBatteriesLocationQuantity();
            batteriesCount = batteriesLocationQuantity.values().stream().mapToInt(Integer::intValue).sum();
        }
        return batteriesCount;
    }

    /**
     * Returns a mapping of battery locations and their respective counts.
     * If the value is not cached, it is computed and stored.
     *
     * @return an unmodifiable map of battery locations and their respective counts
     */
    public Map<Coordinates, Integer> getBatteriesLocationQuantity() {
        if (batteriesLocationQuantity == null) {
            getBatteriesCount(); // Ensures both values are computed together
        }
        return Collections.unmodifiableMap(batteriesLocationQuantity);
    }

    /**
     * Returns the total value of goods on the ship.
     * If the value is not cached, it is computed and stored.
     *
     * @return the total goods value in credits
     */
    public int getGoodsValue() {
        if (goodsValue == null) {
            goodsValue = calculateGoodsValue();
        }
        return goodsValue;
    }

    /**
     * Returns the number of exposed connectors on the ship.
     * If the value is not cached, it is computed and stored.
     *
     * @return the number of exposed connectors
     */
    public int getExposedConnectors() {
        if (exposedConnectors == null) {
            exposedConnectors = calculateExposedConnectors();
        }
        return exposedConnectors;
    }

    // Private methods to calculate statistics

    /**
     * Computes the total amount of free power available on the ship for a specific power type.
     * This method iterates over all tiles and sums their contributions to the requested power type.
     *
     * @param powerType The type of power to compute.
     * @return The total amount of free power available.
     */
    private float calculateFreePower(PowerType powerType) {
        return shipBoard.getTilesOnBoard().stream()
                .map(entry -> entry.getValue().calculateFreePower(powerType))
                .reduce(0f, Float::sum);
    }

    /**
     * Identifies all tile locations that contribute to battery-dependent power for a specific power type.
     * This method scans the board and collects the coordinates of relevant power sources.
     *
     * @param powerType The type of power to compute.
     * @return A list of coordinates where battery-dependent power sources are located.
     */
    private List<Coordinates> calculateBatteryPowerCluster(PowerType powerType) {
        return shipBoard.getTilesOnBoard().stream()
                .filter(entry -> entry.getValue().calculateBatteryPower(powerType) > 0)
                .map(Map.Entry::getKey)
                .toList();
    }

    /**
     * Determines the location of the tile that grants a bonus power for a specific power type.
     * If multiple tiles provide the bonus, the first encountered is returned.
     *
     * @param powerType The type of power bonus to search for.
     * @return The coordinates of the bonus power source, or {@code null} if none is found.
     */
    private Coordinates calculateBonusPowerLocation(PowerType powerType) {
        return shipBoard.getTilesOnBoard().stream()
                .filter(entry -> entry.getValue().getContent().getBonusPower(powerType) > 0)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    /**
     * Computes the distribution of crew members across the ship.
     * This method creates a mapping between tile coordinates and the number of crew members present.
     *
     * @return A map of coordinates to crew member counts.
     */
    private Map<Coordinates, Integer> calculateCrewMembersLocationQuantity() {
        return shipBoard.getTilesOnBoard().stream()
                .map(entry -> Map.entry(entry.getKey(),
                        entry.getValue().getContent().countCrew()))
                .filter(entry -> entry.getValue() > 0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Computes the distribution of batteries across the ship.
     * This method creates a mapping between tile coordinates and the number of batteries present.
     *
     * @return A map of coordinates to battery counts.
     */
    private Map<Coordinates, Integer> calculateBatteriesLocationQuantity() {
        return shipBoard.getTilesOnBoard().stream()
                .map(entry -> Map.entry(entry.getKey(),
                        entry.getValue().getContent().countBatteries(BatteryType.BATTERY)))
                .filter(entry -> entry.getValue() > 0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Computes the total monetary value of all goods stored in the ship.
     * This method sums up the value of items contained in each tile.
     *
     * @return The total value of goods in credits.
     */
    private int calculateGoodsValue() {
        return shipBoard.getTilesOnBoard().stream()
                .map(entry -> entry.getValue().getContent().calculateItemsSellingPrice())
                .reduce(0, Integer::sum);
    }

    /**
     * Count the total number of exposed connectors.
     *
     * @return The total number of exposed connectors.
     */
    private int calculateExposedConnectors() {
        // TODO: calculate exposed connectors
        return 0;
    }
}

