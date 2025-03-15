package src.main.java.it.polimi.ingsw.shipboard.tiles.exceptions;

public class IncoherentBatteryUsageException extends Exception {
    public IncoherentBatteryUsageException(String message) {
        super(message);
    }
    public IncoherentBatteryUsageException(boolean batteryUsage, boolean requiredBatteryUsage) {
        super("Battery usage is " + (requiredBatteryUsage ? "required" : "NOT required")
                + " but there was an attempt to " + (batteryUsage ? "use it" : "NOT use it"));
    }
}
