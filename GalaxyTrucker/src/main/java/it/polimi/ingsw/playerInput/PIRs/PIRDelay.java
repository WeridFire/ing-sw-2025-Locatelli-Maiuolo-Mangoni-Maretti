package it.polimi.ingsw.playerInput.PIRs;

import it.polimi.ingsw.enums.AnchorPoint;
import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.util.Coordinates;
import it.polimi.ingsw.view.cli.ANSI;
import it.polimi.ingsw.view.cli.CLIFrame;
import it.polimi.ingsw.view.cli.CLIScreen;
import it.polimi.ingsw.view.cli.ICLIPrintable;

import java.util.Set;

public class PIRDelay extends PIR {

    private final String message;
    private final CLIFrame cliToShow;

    /**
     * Object that makes the players aware of something and requires any input to go on.
     * After {@code cooldown} seconds, if no input was given by the player, the game continues anyway.
     * <p>
     * This constructor initialize the PIR ready to work with TUI.
     *
     * @param currentPlayer The player the game waits for
     * @param cooldown The cooldown duration.
     * @param toShow The CLIFrame that represents the object to show.
     *               Can be null: in that case only the message will be shown
     */
    public PIRDelay(Player currentPlayer, int cooldown, String message, CLIFrame toShow) {
        super(currentPlayer, cooldown, it.polimi.ingsw.playerInput.PIRType.DELAY);
        this.message = message;
        cliToShow = (toShow != null) ? toShow : new CLIFrame();
    }

    @Override
    public Set<Coordinates> getHighlightMask() {
        return Set.of();
    }

    @Override
    public void run() throws InterruptedException {
        synchronized (lock){
            lock.wait(getCooldown() * 1000L);
        }
    }

    @Override
    void endTurn() {
        synchronized (lock){
            lock.notifyAll();
        }
    }

    @Override
    public CLIFrame getCLIRepresentation() {
        // containerRows below
        int containerColumns = 100;

        // Header frame
        CLIFrame frame = new CLIFrame(ANSI.BACKGROUND_BLUE + ANSI.WHITE + " PLAYER INPUT REQUEST " + ANSI.RESET)
                .merge(new CLIFrame(""), Direction.SOUTH);

        // Message section
        frame = frame.merge(
                new CLIFrame(message).paintForeground(ANSI.YELLOW)
                        .wrap(containerColumns - 2, 1, AnchorPoint.CENTER),
                Direction.SOUTH, 1
        );

        // Info section
        frame = frame.merge(cliToShow, Direction.SOUTH, 2);

        // Command hint
        frame = frame.merge(
                new CLIFrame(ANSI.WHITE + "Press " + ANSI.GREEN + "[Enter]" + ANSI.WHITE + " to continue"),
                Direction.SOUTH, 2
        );

        // Create a container screen with a fixed size, background, and border
        int containerRows = Math.max(frame.getRows() + 2, 24);
        CLIFrame screenFrame = CLIScreen.getScreenFrame(containerRows, containerColumns, ANSI.BACKGROUND_BLACK, ANSI.BLACK);

        // Merge the content into the screen, centered
        return screenFrame.merge(frame, AnchorPoint.CENTER, AnchorPoint.CENTER);
    }


}