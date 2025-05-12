package it.polimi.ingsw.util;

public class Default {

    // network
    public static final String HOST = "localhost";
    public static final String RMI_SERVER_NAME = "GalaxyTruckerServer";
    public static final int RMI_PORT = 1111;
    public static final int SOCKET_PORT = 1234;
    public static int PORT(boolean useRMI) {
        return useRMI ? RMI_PORT : SOCKET_PORT;
    }

    // client
    public static boolean USE_RMI = false;
    public static boolean USE_GUI = false;

    //GUI
    public static final String BACK_TILE_PATH = "GT-new_tiles_16_for web157.jpg";

}
