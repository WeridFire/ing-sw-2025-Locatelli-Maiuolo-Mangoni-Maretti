package it.polimi.ingsw.util;

import it.polimi.ingsw.TilesFactory;

public class Default {

    // network
    public static final String HOST = "localhost";
    public static final String RMI_SERVER_NAME = "GalaxyTruckerServer";
    public static final int RMI_PORT = 1111;
    public static final int SOCKET_PORT = 1234;
    public static int PORT(boolean useRMI) {
        return useRMI ? RMI_PORT : SOCKET_PORT;
    }
    public static final String SOCKET_HANDSHAKE_MESSAGE = "GalaxyTruckerSocketServer";
    public static final int SOCKET_HANDSHAKE_TIMEOUT_MS = 5000;

    // client
    public static boolean USE_RMI = false;
    public static boolean USE_GUI = false;

    //GUI
    public static final String PATH_BACK_TILE = "GT-new_tiles_16_for web157.jpg";
    public static final String PATH_BACK_CARD_I = "GT-cards_I_IT_0121.jpg";
    public static final String PATH_BACK_CARD_II = "GT-cards_II_IT_0121.jpg";
    public static final String OVERLAY_DRAG_ID = "dragId";
    public static final String ADD_CARGO_STRING_COLOR = "rgba(0, 128, 0, 0.5)";
    public static final String REMOVE_CARGO_STRING_COLOR = "rgba(255, 255, 0, 0.5)";

    //GENRAL
    public static final int TOTAL_TILES_NUMBER = TilesFactory.createPileTiles().size();
    public static final int HOURGLASS_SECONDS = 90;

    // NON-FINAL: "settings" to change during game (used mainly for tests)
    public static int PIR_SECONDS = 60;
    public static int PIR_SHORT_SECONDS = 10;

    public static String STARTING_CARD = null;

}
