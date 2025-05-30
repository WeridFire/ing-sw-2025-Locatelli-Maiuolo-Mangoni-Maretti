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

    // client
    public static boolean USE_RMI = false;
    public static boolean USE_GUI = false;

    //GUI
    public static final String PATH_BACK_TILE = "GT-new_tiles_16_for web157.jpg";
    public static final String PATH_BACK_CARD_I = "GT-cards_I_IT_0121.jpg";
    public static final String PATH_BACK_CARD_II = "GT-cards_II_IT_0121.jpg";

    public static final double BOARD_ELLIPSE_RX = 305;
    public static final double BOARD_ELLIPSE_RY = 160;
    public static final int ELLIPSE_TESTFLIGHT_STEPS = 18;
    public static final int ELLIPSE_ONE_STEPS = 24;

    //GENRAL
    public static final int TOTAL_TILES_NUMBER = TilesFactory.createPileTiles().size();
    public static final int HOURGLASS_SECONDS = 10;  // TODO: reset HOURGLASS_SECONDS = 90. This is for debug purposes

}
