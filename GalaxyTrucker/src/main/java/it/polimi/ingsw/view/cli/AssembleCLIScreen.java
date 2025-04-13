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
    protected void processCommand(String command, String[] args) throws RemoteException {
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
        List<String> availableCommands = new ArrayList<>();
        // note: last timerflip only if assemble phase ended for this player
        availableCommands.add("timerflip|Flips the hourglass of the game.");

        if (tileInHand == null) {
            availableCommands.add("draw|Draws a tile from the covered tiles.");
            availableCommands.add("pick <id>|Pick in hand the tile with ID <id>.");
        }
        else {
            availableCommands.add("discard|Discard the tile you have in hand.");
            availableCommands.add("rotate <direction>|Rotate the tile you have in hand.");
            availableCommands.add("place <row> <column>|Place the tile from your hand onto your shipboard.");
        }

        printCommands(screenName, availableCommands.toArray(String[]::new));
    }

    @Override
    public CLIFrame getCLIRepresentation() {
        // update tile in hand if different from previous
        if ((tileInHand == null) || (!tileInHand.equals(getLastUpdate().getClientPlayer().getTileInHand()))) {
            // note: in here also if both are null, but in that case nothing happens -> no problem
            tileInHand = getLastUpdate().getClientPlayer().getTileInHand();
        }

        //white bg
        CLIFrame screenBorder = getScreenFrame(36, 100, ANSI.BACKGROUND_WHITE);

        // tile in hand frame
        CLIFrame tileInHandFrame = new CLIFrame(ANSI.BACKGROUND_BLUE + ANSI.WHITE + " Tile in Hand ")
                .merge((tileInHand != null)
                                ? tileInHand.getCLIRepresentation()
                                : new CLIFrame(TileSkeleton.getForbiddenTileCLIRepresentation(0, 0)),
                        Direction.SOUTH, 1)
                .merge(new CLIFrame(""), Direction.SOUTH)
                .paintBackground(ANSI.BACKGROUND_BLACK);

        //title
        CLIFrame shipboardTitle = new CLIFrame(new String[]{
                ANSI.BACKGROUND_BLUE + ANSI.WHITE + " YOUR SHIPBOARD " + ANSI.RESET
        });

        //shipboard frame
        CLIFrame shipboardFrame = getLastUpdate().getClientPlayer().getShipBoard().getCLIRepresentation()
                .paintForeground(ANSI.BLACK);

        CLIFrame shipboardWithTitle = shipboardTitle.merge(shipboardFrame, Direction.SOUTH, 1);

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

                CLIFrame tileWithId = tileRepresentation.merge(idLabel, Direction.SOUTH, 1);

                tileFrames.add(tileWithId);
            }

            //Merge all tile frames horizontally with some spacing
            tilesFrame = new CLIFrame();
            for (CLIFrame tileFrame : tileFrames) {
                tilesFrame = tilesFrame.merge(tileFrame, Direction.EAST, 5);
            }
        } else {
            tilesFrame = new CLIFrame(new String[]{
                    ANSI.RED + "No tiles drawn" + ANSI.RESET
            });
        }

        CLIFrame tilesWithTitle = tilesTitle.merge(tilesFrame, Direction.SOUTH, 1);

        CLIFrame contentFrame = shipboardWithTitle
                .merge(tileInHandFrame, Direction.EAST, 5)
                .merge(tilesWithTitle, Direction.SOUTH, 2);

		return screenBorder.merge(contentFrame, AnchorPoint.CENTER, AnchorPoint.CENTER);
    }
}
