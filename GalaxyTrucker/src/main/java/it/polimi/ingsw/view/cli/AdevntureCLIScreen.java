package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.enums.GamePhaseType;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class AdevntureCLIScreen extends CLIScreen{
    /**
     * @param screenName The identifier of the screen.
     */
    public AdevntureCLIScreen(String screenName) {
        super(screenName);
    }

    /**
     * Abstract class for a CLI screen. Contains the standardized methods and fields to design a new screen.
     * A screen is an object that displays information on the CLI based on the current game state.
     * The user, based on the screen they are on, can perform different commands.
     * A user can swap between screens using the screen global command. Screens differ in availability, and
     * based on the current state of the game there might be different available screens.
     *
     * @param screenName    The identifier of the screen.
     * @param forceActivate if to forcefully activate this screen whenever an update satisfying it will be received.
     * @param priority      if there are multiple screens not force-activable, priority will indicate which one to prioritize.
     */
    public AdevntureCLIScreen(String screenName, boolean forceActivate, int priority) {
        super(screenName, forceActivate, priority);
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
        return null;
    }

    private record Coord(int row, int col){}
    private List<Coord> getCoords(GameLevel level){
        int max_row = 0;
        int max_col = 0;
        int magicShift = 0;
        List<Coord> coords = new ArrayList<>();
        switch(level){
            case TESTFLIGHT:
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
                max_row = 9;
                max_col = 17;
                magicShift = 9;
                break;

            case ONE:
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
                max_row = 11;
                max_col = 21;
                magicShift = 5;
                break;

            case TWO:
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
                coords.add(new Coord(6, 27));
                coords.add(new Coord(8, 27));
                coords.add(new Coord(10, 27));
                max_row = 17;
                max_col = 28;
                magicShift = 15;
                break;
        }

        double cx = 0, cy = 0;
        for (Coord p : coords) {
            cx += p.row();
            cy += p.col();
        }
        cx /= max_row;
        cy /= max_col;

        double finalCx = cx;
        double finalCy = cy;
        coords.sort((a, b) -> {
            double angleA = Math.atan2(a.row() - finalCx, a.col() - finalCy);
            double angleB = Math.atan2(b.row()- finalCx, b.col() - finalCy);
            return Double.compare(angleB, angleA);
        });

        System.out.println("Punti ordinati in senso orario:");
        for (Coord p : coords) {
            System.out.println("(" + p.row() + ", " + p.col() + ")");
        }


        return coords;
    }

    public CLIFrame getBoardFrame(GameLevel level) {

        //color related to level
        String bg = null;
        int[][] board = switch (level) {
            case TESTFLIGHT -> {
                bg = ANSI.BACKGROUND_BLUE;
                yield new int[9][17];
            }
            case ONE -> {
                bg = ANSI.BACKGROUND_PURPLE;
                yield new int[11][21];
            }
            case TWO -> {
                bg = ANSI.BACKGROUND_RED;
                yield new int[17][28];
            }
        };

        for (Coord coord : getCoords(level)) {
            board[coord.row()][coord.col()] = 1;
        }

        for (int[] ints : board) {
            for (int j = 0; j < ints.length; j++) {
                System.out.print(ints[j] + " ");
            }
            System.out.println();
        }

        CLIFrame boardFrame = new CLIFrame(bg + ANSI.WHITE + " BOARD ");




        return boardFrame;
    }
}
