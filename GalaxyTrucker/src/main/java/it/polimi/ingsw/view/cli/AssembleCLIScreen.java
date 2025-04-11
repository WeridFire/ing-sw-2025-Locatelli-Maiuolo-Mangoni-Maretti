package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.enums.AnchorPoint;
import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.enums.Rotation;
import it.polimi.ingsw.shipboard.tiles.TileSkeleton;
import it.polimi.ingsw.shipboard.tiles.exceptions.FixedTileException;
import it.polimi.ingsw.util.Coordinates;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class AssembleCLIScreen extends CLIScreen{

    private TileSkeleton tileInHand = null;

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
                //TODO: client side checks
                getServer().flipHourglass(getClient());
                break;

            case "draw":
                //TODO: client side checks
                getServer().drawTile(getClient());
                break;

            case "discard":
                //TODO: client side checks
                getServer().discardTile(getClient());
                break;

            case "reserve":
                //TODO: client side checks
                getServer().reserveTile(getClient());
                break;

            case "rotate":
                // Note: this command overrides the private field, not needing confirmation from the server
                if (args.length == 1) {
                    Rotation rotation = Rotation.fromString(args[0]);
                    if (rotation == Rotation.NONE) {
                        setScreenMessage("Rotation '" + args[0] + "' not recognized.\nAllowed rotations are:"
                                + "<left|l> | <right|r> | <opposite|o>");
                        break;
                    }
                    if (tileInHand == null) {
                        setScreenMessage("You have no tile in your hand.");
                        break;
                    }
                    try {
                        tileInHand.rotateTile(rotation);

                        refresh();  // only because it's client side command
                    } catch (FixedTileException e) {
                        setScreenMessage("The tile in your hand has already been placed.");  // should never happen
                        break;
                    }
                }
                else {
                    setScreenMessage("Usage: rotate <direction>");
                }
                break;

            case "place":
                if (args.length == 2) {
                    Coordinates coordinates;
                    try {
                        int row = Integer.parseInt(args[0]);
                        int column = Integer.parseInt(args[1]);
                        coordinates = new Coordinates(row, column);
                    } catch (NumberFormatException e) {
                        setScreenMessage("Invalid coordinates. Please enter valid coordinates: " +
                                "integer number for both row and column.");
                        break;
                    }
                    //TODO: client side checks, e.g. coordinates represent valid empty place on the shipboard
                    if (tileInHand == null) {
                        setScreenMessage("You have no tile in your hand.");
                        break;
                    }
                    // note: implicit handling of rotation
                    getServer().placeTile(getClient(), coordinates, tileInHand.getAppliedRotation());
                }
                else {
                    setScreenMessage("Usage: place <row> <column>");
                }
                break;

            case "pick":
                //TODO: client side checks
                if (args.length == 1) {
                    int id = Integer.parseInt(args[0]);
                    getServer().pickTile(getClient(), id);
                }
                break;

            case "finish":
                if(getLastUpdate().getCurrentGame().getCurrentGamePhaseType() != GamePhaseType.ASSEMBLE){
                    setScreenMessage("The game is not in assembly phase.");
                    return;
                }

                if(getLastUpdate().getClientPlayer().getShipBoard() == null){
                    setScreenMessage("You don't have a shipboard.");
                    return;
                }

                if(getLastUpdate().getClientPlayer().getShipBoard().isEndedAssembly()){
                    setScreenMessage("You already ended the assembly phase!");
                    return;
                }
                getServer().finishAssembling(getClient());
                return;

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
