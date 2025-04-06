package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.enums.AnchorPoint;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.shipboard.tiles.TileSkeleton;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class AssembleCLIScreen extends CLIScreen{

    public AssembleCLIScreen() {
        super("assemble", true);
    }

    @Override
    protected boolean switchConditions() {
        return getLastUpdate().getCurrentGame() != null &&
                getLastUpdate().getCurrentGame().getCurrentGamePhaseType() == GamePhaseType.ASSEMBLE;
    }

    @Override
    protected void processCommand(String command, String[] args) throws RemoteException, IllegalArgumentException {
        command = command.toLowerCase();
        switch(command){
            case "timerflip":
                getServer().flipHourglass(getClient());
                setScreenMessage("Done");
                break;

            case "draw":
                getServer().drawTile(getClient());
                setScreenMessage("Done");
                break;

            case "discard":
                getServer().discardTile(getClient());
                setScreenMessage("Done");
                break;

            case "reserve":
                getServer().reserveTile(getClient());
                setScreenMessage("Done");
                break;

            case "pick":
                if (args.length == 1) {
                    int id = Integer.parseInt(args[0]);
                    getServer().pickTile(getClient(), id);
                }
                setScreenMessage("Done");
                break;

            default:
                setScreenMessage("Invalid command. Use help to view available commands.");
                break;
        }
    }

    @Override
    void printScreenSpecificCommands() {
        printCommands(screenName,
                "timerflip | Flips the hourglass of the game.",
                "draw | Draws a tile from the covered tiles",
                "discard | Discard the tile you have in hand",
                "pick <tileId> | Pick in hand the tile with Id <tileId>"
        );
    }

    @Override
    public CLIFrame getCLIRepresentation() {
        //white bg
        CLIFrame screenBorder = getScreenFrame(40, 120, ANSI.BACKGROUND_WHITE);

        //title
        CLIFrame shipboardTitle = new CLIFrame(new String[]{
                ANSI.BACKGROUND_BLUE + ANSI.WHITE + " YOUR SHIPBOARD " + ANSI.RESET
        });

        //shipboard frame
        CLIFrame shipboardFrame = getLastUpdate().getClientPlayer().getShipBoard().getCLIRepresentation();

        CLIFrame shipboardWithTitle = shipboardTitle.merge(
                shipboardFrame,
                AnchorPoint.BOTTOM,
                AnchorPoint.TOP,
                1,
                0
        );

        //drawn tiles
        List<TileSkeleton> drawnTiles = getLastUpdate().getCurrentGame().getDrawnTiles();

        CLIFrame tilesTitle = new CLIFrame(new String[]{
                ANSI.BACKGROUND_BLUE + ANSI.WHITE + " DRAWN TILES " + ANSI.RESET
        });

        //frame for drawn tiles
        CLIFrame tilesFrame;
        if (!drawnTiles.isEmpty()) {
            //individual frames for each tile with its Id
            List<CLIFrame> tileFrames = new ArrayList<>();

            for (TileSkeleton tile : drawnTiles) {
                CLIFrame tileRepresentation = tile.getCLIRepresentation();

                CLIFrame idLabel = new CLIFrame(new String[]{
                        ANSI.BACKGROUND_YELLOW + ANSI.BLACK + " ID: " +
                                tile.getTileId() + " " + ANSI.RESET
                });

                CLIFrame tileWithId = tileRepresentation.merge(
                        idLabel,
                        AnchorPoint.BOTTOM,
                        AnchorPoint.TOP,
                        1,
                        0
                );

                tileFrames.add(tileWithId);
            }

            //Merge all tile frames horizontally with some spacing
            tilesFrame = new CLIFrame();
            int horizontalOffset = 0;
            for (CLIFrame tileFrame : tileFrames) {
                tilesFrame = tilesFrame.merge(
                        tileFrame,
                        AnchorPoint.TOP_LEFT,
                        AnchorPoint.TOP_LEFT,
                        0,
                        horizontalOffset
                );
                horizontalOffset += 10;
            }
        } else {
            tilesFrame = new CLIFrame(new String[]{
                    ANSI.RED + "No tiles drawn" + ANSI.RESET
            });
        }

        CLIFrame tilesWithTitle = tilesTitle.merge(
                tilesFrame,
                AnchorPoint.BOTTOM,
                AnchorPoint.TOP,
                1,
                0
        );

        CLIFrame contentFrame = shipboardWithTitle.merge(
                tilesWithTitle,
                AnchorPoint.BOTTOM,
                AnchorPoint.TOP,
                2,
                0
        );

		return screenBorder.merge(
                contentFrame,
                AnchorPoint.CENTER,
                AnchorPoint.CENTER,
                0,
                0
        );
    }
}
