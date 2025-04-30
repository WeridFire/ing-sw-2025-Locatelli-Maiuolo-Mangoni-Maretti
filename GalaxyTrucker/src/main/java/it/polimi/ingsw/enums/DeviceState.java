package it.polimi.ingsw.enums;

/**
 * This enum represents how the Main App is run for the device that makes it run.
 * <ul>
 *  <li>If only server -> {@link DeviceState#SERVER}</li>
 *  <li>If only client -> {@link DeviceState#CLIENT}</li>
 *  <li>If both server and client -> {@link DeviceState#HOST}</li>
 * </ui>
 */
public enum DeviceState {
    SERVER, CLIENT, HOST;

    public static DeviceState add(DeviceState oldState, DeviceState addState) {
        if (oldState == null) {
            return addState;
        }
        return switch (addState) {
            case SERVER -> oldState == CLIENT ? HOST : oldState;
            case CLIENT -> oldState == SERVER ? HOST : oldState;
            case HOST -> HOST;
        };
    }

    public static DeviceState remove(DeviceState oldState, DeviceState removeState) {
        if ((oldState == null) || (oldState == removeState) || (removeState == HOST)) return null;
        // if here:
        // 1. oldState is HOST and removeState is not HOST, or
        // 2. oldState is SERVER and removeState is CLIENT or null, or
        // 3. oldState is CLIENT and removeState is SERVER or null
        // hence:
        if (oldState == HOST) {
            return switch (removeState) {
                case SERVER -> CLIENT;
                case CLIENT -> SERVER;
                default -> HOST;  // default is null
            };
        }
        else {
            return oldState;
        }
    }
}
