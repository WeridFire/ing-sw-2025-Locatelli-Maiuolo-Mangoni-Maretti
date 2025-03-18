package src.main.java.it.polimi.ingsw.shipboard1;

/**
 * Monitors and verifies the structural integrity of a ship's board.
 * This class listens for updates to the {@link ShipBoard} and recalculates potential integrity issues.
 * Integrity checks follow a predefined order of resolution:
 * <ol>
 *   <li>Tiles that are intrinsically misconnected (e.g., invalid orientation).</li>
 *   <li>Detection of multiple disconnected clusters of tiles.</li>
 *   <li>Tiles that are improperly connected.</li>
 * </ol>
 */
public class ShipIntegrity implements ShipBoardListener {

    /**
     * The ship board being monitored for integrity checks.
     */
    private final ShipBoard shipBoard;

    /**
     * Constructs a {@code ShipIntegrity} instance and registers it as a listener to the given {@code ShipBoard}.
     * This ensures that integrity checks are updated whenever the board changes.
     *
     * @param shipBoard The ship board to monitor.
     */
    public ShipIntegrity(ShipBoard shipBoard) {
        this.shipBoard = shipBoard;
        shipBoard.addListener(this);  // Register as listener
        invalidateIntegrity();  // Initialize values as to be checked
    }

    /**
     * Callback method triggered when the {@link ShipBoard} is updated.
     * This causes a recalculation of integrity-related issues.
     */
    @Override
    public void onShipBoardUpdated() {
        invalidateIntegrity();
    }

    /**
     * Invalidates and recalculates all integrity-related checks in a specific order:
     * <ul>
     *   <li>Detects tiles that are intrinsically misconnected (e.g., incorrect orientation).</li>
     *   <li>Checks for multiple disconnected tile clusters.</li>
     *   <li>Identifies improperly connected tiles.</li>
     * </ul>
     * The exact logic for these checks should be implemented within this method.
     */
    private void invalidateIntegrity() {
        // TODO: Implement integrity verification logic
    }
}

