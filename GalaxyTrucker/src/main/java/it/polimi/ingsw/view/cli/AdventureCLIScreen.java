package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.enums.Rotation;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.shipboard.tiles.MainCabinTile;
import it.polimi.ingsw.util.GameLevelStandards;
import it.polimi.ingsw.util.Util;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class AdventureCLIScreen extends CLIScreen{

    /**
     * Utility Record.
     * */
    private record Coord(int row, int col){
        public boolean equals(Coord c) {
            return row == c.row && col == c.col;
        }
    }

    private final ArrayList<Coord> coords = new ArrayList<>();


    public AdventureCLIScreen() {
        // note that forceActivate is false because other screens can be activated during an adventure, like PIRs
        super("adventure", false, 0);
    }

    /**
     * The condition a state has to fullfill for a screen to be activable.
     * This does not include if the screen is currently active on the screen handler, but just based on the game state.
     *
     * @return If the screen can be activable.
     */
    @Override
    protected boolean switchConditions() {
        return getLastUpdate().getCurrentGame() != null &&
                getLastUpdate().getCurrentGame().getCurrentGamePhaseType() == GamePhaseType.ADVENTURE;
    }

    /**
     * This function basically allows the main CLI logic to delegate to the active screen the handling of a command.
     * Passes the command and the args, and the screen tries to execute. If it fails, the screen will display the error
     * and the requirements for the command to execute.
     *
     * @param command The command to execute
     * @param args    The args to pass
     * @requires this.switchConditions() == true
     */
    @Override
    protected void processCommand(String command, String[] args) throws RemoteException {

    }

    /**
     * This function should return the commands available in the specific screen
     * to pass as parameter while calling the method printCommands.
     * Can be immutable.
     */
    @Override
    protected List<String> getScreenSpecificCommands() {
        return List.of();
    }

    /**
     * Generates a CLI representation of the implementing object.
     *
     * @return A {@link CLIFrame} containing the CLI representation.
     */
    @Override
    public CLIFrame getCLIRepresentation() {

        GameData gameData = getLastUpdate().getCurrentGame();

        Player thisPlayer = getLastUpdate().getClientPlayer();
        CLIFrame shipboardFrame = thisPlayer.getShipBoard().getCLIRepresentation();

        CLIFrame boardFrame = getBoardFrame(gameData.getLevel(), getLastUpdate().getCurrentGame().getPlayers());

        shipboardFrame = shipboardFrame.merge(boardFrame, Direction.NORTH, 2);

        return shipboardFrame;
    }

    /**
     * Generates or retrieves the list of coordinates defining the game path for a specific level.
     * The coordinates are generated once per instance and then cached in the `coords` field.
     * They are ordered with the specified rotation from the first board place.
     *
     * @implNote The caching mechanism is instance-based and does not differentiate between levels after the first call.
     * If this method is called subsequently with a different level on the same instance, it will return the
     * previously cached coordinates without recalculating.
     * This implies the instance is expected to be used for a single level's board generation.
     *
     * @param level The {@link GameLevel} for which to get the coordinates.
     * @param turnDirection To order the coordinates in {@link Rotation#CLOCKWISE} or {@link Rotation#COUNTERCLOCKWISE}.
     *                      If {@code turnDirection} is not properly specified, the default (counterclockwise) applies.
     * @return A {@link List} of {@link Coord} objects representing the ordered path for the level.
     *         Returns the cached list if already populated, otherwise returns the newly generated and cached list.
     */
    private List<Coord> getCoords(GameLevel level, Rotation turnDirection) {
        if (!coords.isEmpty()) return coords;

        switch(level){
            case TESTFLIGHT, ONE:
                coords.add(new Coord(0, 10));
                coords.add(new Coord(0, 6));
                coords.add(new Coord(1, 4));
                coords.add(new Coord(2, 2));
                coords.add(new Coord(3, 0));
                coords.add(new Coord(5, 0));
                coords.add(new Coord(6, 2));
                coords.add(new Coord(7, 4));
                coords.add(new Coord(8, 6));
                coords.add(new Coord(0, 8));
                coords.add(new Coord(8, 8));
                coords.add(new Coord(8, 10));
                coords.add(new Coord(1, 12));
                coords.add(new Coord(7, 12));
                coords.add(new Coord(2, 14));
                coords.add(new Coord(6, 14));
                coords.add(new Coord(3, 16));
                coords.add(new Coord(5, 16));
                break;

            case TWO:
                coords.add(new Coord(4, 0));
                coords.add(new Coord(6, 0));
                coords.add(new Coord(2, 1));
                coords.add(new Coord(8, 1));
                coords.add(new Coord(2, 3));
                coords.add(new Coord(8, 3));
                coords.add(new Coord(1, 5));
                coords.add(new Coord(9, 5));
                coords.add(new Coord(1, 7));
                coords.add(new Coord(9, 7));
                coords.add(new Coord(0, 9));
                coords.add(new Coord(10, 9));
                coords.add(new Coord(0, 11));
                coords.add(new Coord(10, 11));
                coords.add(new Coord(1, 13));
                coords.add(new Coord(9, 13));
                coords.add(new Coord(1, 15));
                coords.add(new Coord(9, 15));
                coords.add(new Coord(2, 17));
                coords.add(new Coord(8, 17));
                coords.add(new Coord(2, 19));
                coords.add(new Coord(8, 19));
                coords.add(new Coord(4, 20));
                coords.add(new Coord(6, 20));
                break;

                /*
            case THREE:
                coords.add(new Coord(6, 0));
                coords.add(new Coord(8, 0));
                coords.add(new Coord(10, 0));
                coords.add(new Coord(4, 1));
                coords.add(new Coord(12, 1));
                coords.add(new Coord(2, 2));
                coords.add(new Coord(14, 2));
                coords.add(new Coord(2, 4));
                coords.add(new Coord(14, 4));
                coords.add(new Coord(1, 6));
                coords.add(new Coord(15, 6));
                coords.add(new Coord(1, 8));
                coords.add(new Coord(15, 8));
                coords.add(new Coord(0, 10));
                coords.add(new Coord(16, 10));
                coords.add(new Coord(0, 12));
                coords.add(new Coord(16, 12));
                coords.add(new Coord(0, 14));
                coords.add(new Coord(16, 14));
                coords.add(new Coord(0, 16));
                coords.add(new Coord(16, 16));
                coords.add(new Coord(1, 18));
                coords.add(new Coord(15, 18));
                coords.add(new Coord(1, 20));
                coords.add(new Coord(15, 20));
                coords.add(new Coord(2, 22));
                coords.add(new Coord(14, 22));
                coords.add(new Coord(2, 24));
                coords.add(new Coord(14, 24));
                coords.add(new Coord(4, 25));
                coords.add(new Coord(12, 25));
                coords.add(new Coord(6, 26));
                coords.add(new Coord(8, 26));
                coords.add(new Coord(10, 26));
                break;

                 */
        }

        // calculate correct centroid
        double cx = 0, cy = 0;
        for (Coord p : coords) {
            cx += p.row();
            cy += p.col();
        }
        cx /= coords.size();
        cy /= coords.size();

        // sort by angle
        double finalCx = cx;
        double finalCy = cy;
        boolean clockwise = turnDirection == Rotation.CLOCKWISE;
        coords.sort((a, b) -> {
            // note: y inverted because rows increase "going down"
            double angleA = Math.atan2(-(a.row() - finalCx), a.col() - finalCy);
            double angleB = Math.atan2(-(b.row() - finalCx), b.col() - finalCy);
            return clockwise ? Double.compare(angleB, angleA) : Double.compare(angleA, angleB);
        });

        // magic shift 2 places
        for (int i = 0; i < 2; i++) {
            coords.add(coords.removeFirst());
        }

        return coords;
    }

    /**
     * Utility Record.
     * */
    private record PlayerPosAndColor(Coord pos, MainCabinTile.Color color, boolean isLeader) {}

    /**
     * Generates a {@link CLIFrame} representing the visual state of the game board for a given level.
     * It determines the board layout, dimensions, and background color based on the specified {@code level}.
     * The method retrieves the ordered path coordinates using {@link #getCoords(GameLevel, Rotation)} and fetches
     * the current player positions and colors from the latest game update (via {@code getLastUpdate()}).
     * The resulting frame includes appropriate ANSI color codes for background and player pawns.
     *
     * @param level The {@link GameLevel} to render the board for.
     * @return A {@link CLIFrame} object containing the string representation of the board,
     *         ready for display in a command-line interface, with appropriate background color set.
     */
    public CLIFrame getBoardFrame(GameLevel level, List<Player> players) {
        //color related to level
        String bg = GameLevelStandards.getColorANSI(level, true);
        boolean[][] board = switch (level) {
            case TESTFLIGHT, ONE -> new boolean[9][17];
            case TWO -> new boolean[11][21];
            // case THREE -> new int[17][27];
        };
        ArrayList<Coord> buff_coords = new ArrayList<>(getCoords(level, Rotation.CLOCKWISE));

        List<PlayerPosAndColor> playersPosAndColor = new ArrayList<>();
        for (int i = 0; i < players.size(); i++) {
            playersPosAndColor.add(new PlayerPosAndColor(
                    Util.getModularAt(buff_coords, players.get(i).getPosition()),
                    players.get(i).getColor(),
                    i == 0));
        }

        for (Coord coord : buff_coords) {
            board[coord.row()][coord.col()] = true;
        }

        boolean found;
        String padding = " ".repeat(1);
        ArrayList<String> res = new ArrayList<String>();
        for (int i = 0; i < board.length; i++) {
            StringBuilder sb = new StringBuilder(padding);
            for (int j = 0; j < board[i].length; j++) {
                found = false;
                if (board[i][j]) {
                    Coord new_c = new Coord(i,j);

                    for(PlayerPosAndColor pc : playersPosAndColor) {
                        if (new_c.equals(pc.pos)) {
                            sb.append(pc.isLeader ? ANSI.BACKGROUND_WHITE : ANSI.BACKGROUND_BLACK)  // no racism
                                    .append(pc.color.toANSIColor(false))
                                    .append("▲").append(ANSI.RESET);
                            found = true;
                        }
                    }
                    if (!found) {
                        sb.append(ANSI.WHITE + "△" + ANSI.RESET);
                    }

                } else {
                    sb.append(" ");
                }

                if (j < board[i].length - 1) {
                    sb.append("   ");
                }
            }
            sb.append(padding);
            res.add(sb.toString());
        }

        CLIFrame boardFrame = new CLIFrame(res.toArray(new String[0]));
        boardFrame = boardFrame.paintBackground(bg);

        return boardFrame;
    }
}
