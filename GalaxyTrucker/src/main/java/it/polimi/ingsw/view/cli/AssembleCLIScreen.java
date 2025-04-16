package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.enums.AnchorPoint;
import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.enums.Rotation;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.shipboard.tiles.TileSkeleton;
import it.polimi.ingsw.shipboard.tiles.exceptions.FixedTileException;
import it.polimi.ingsw.util.Coordinates;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class AssembleCLIScreen extends CLIScreen{

    private TileSkeleton tileInHand = null;

    public AssembleCLIScreen() {
        super("assemble", true, 0);
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
                    break;
                }

                if(getLastUpdate().getClientPlayer().getShipBoard().isEndedAssembly()){
                    setScreenMessage("You already ended the assembly phase!");
                    break;
                }
                getServer().finishAssembling(getClient());
                break;

            case "showcardgroup":
                if (args.length == 1) {
                    int id = Integer.parseInt(args[0]);
                    getServer().showCardGroup(getClient(), id);
                }
                break;

            case "hidecardgroup":
                getServer().hideCardGroup(getClient());
                break;

            default:
                setScreenMessage("Invalid command. Use help to view available commands.");
                break;
        }
    }

    @Override
    protected List<String> getScreenSpecificCommands() {
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

        return availableCommands;
    }

    private CLIFrame getTileRepresentationWithID(TileSkeleton tile) {
        CLIFrame idLabel = new CLIFrame(ANSI.BACKGROUND_YELLOW + ANSI.BLACK + " ID: " +
                tile.getTileId() + " " + ANSI.RESET);
        return tile.getCLIRepresentation().merge(idLabel, Direction.SOUTH)
                .paintBackground(ANSI.BACKGROUND_BLACK);
    }

    @Override
    public CLIFrame getCLIRepresentation() {
        final int maxWidth = 100;
        Player thisPlayer = getLastUpdate().getClientPlayer();

        // update tile in hand if different from previous
        if ((tileInHand == null) || (!tileInHand.equals(thisPlayer.getTileInHand()))) {
            // note: in here also if both are null, but in that case nothing happens -> no problem
            tileInHand = thisPlayer.getTileInHand();
        }

        // frame for tile in hand
        CLIFrame frameTileInHand = new CLIFrame(ANSI.BACKGROUND_BLUE + ANSI.WHITE + " Tile in Hand ")
                .merge((tileInHand != null)
                                ? tileInHand.getCLIRepresentation()
                                : new CLIFrame(TileSkeleton.getForbiddenTileCLIRepresentation(0, 0)),
                        Direction.SOUTH, 1)
                .merge(new CLIFrame(""), Direction.SOUTH)
                .paintBackground(ANSI.BACKGROUND_BLACK);

        // frame for reserved tiles
        List<TileSkeleton> reservedTiles = thisPlayer.getReservedTiles();
        CLIFrame frameReservedTiles = (reservedTiles.isEmpty()
                    ? new CLIFrame(ANSI.RED + "No reserved tiles" + ANSI.RESET)
                    : reservedTiles.stream()
                    .map(this::getTileRepresentationWithID)
                    .reduce(new CLIFrame(), (b, t) -> b.isEmpty() ? t : b.merge(t, Direction.EAST, 3))
                )
                .merge(new CLIFrame(ANSI.BACKGROUND_BLUE + ANSI.WHITE + " Reserved Tiles "),
                        Direction.NORTH, 1);

        // frame for shipboard
        CLIFrame frameShipboard = thisPlayer.getShipBoard().getCLIRepresentation()
                .paintForeground(ANSI.BLACK)
                .merge(new CLIFrame(ANSI.BACKGROUND_BLUE + ANSI.WHITE + " YOUR SHIPBOARD " + ANSI.RESET),
                        Direction.NORTH, 1);

        // drawn and discarded tiles
        List<TileSkeleton> uncoveredTiles = getLastUpdate().getCurrentGame().getUncoveredTiles();

        // frame for drawn and discarded tiles as a grid
        List<List<CLIFrame>> frameUncoveredTilesGrid = null;
        final int uncoveredTileHorizontalSpacing = 3;
        if (!uncoveredTiles.isEmpty()) {
            // individual frames for each tile with its ID
            List<CLIFrame> tileFrames = new ArrayList<>();

            for (TileSkeleton tile : uncoveredTiles) {
                tileFrames.add(getTileRepresentationWithID(tile));
            }

            // merge all tile frames in a grid
            for (CLIFrame tileFrame : tileFrames) {
                frameUncoveredTilesGrid = CLIFrame.addInFramesGrid(frameUncoveredTilesGrid,
                        tileFrame, uncoveredTileHorizontalSpacing, maxWidth);
            }
        } else {
            frameUncoveredTilesGrid = CLIFrame.addInFramesGrid(frameUncoveredTilesGrid,
                    new CLIFrame(ANSI.RED + "No tiles drawn and discarded" + ANSI.RESET),
                    0, maxWidth);
        }

        // frame for drawn and discarded tiles
        CLIFrame frameUncoveredTiles = CLIFrame
                .fromFramesGrid(frameUncoveredTilesGrid, uncoveredTileHorizontalSpacing, 1)
                // add title
                .merge(new CLIFrame(ANSI.BACKGROUND_BLUE + ANSI.WHITE + " DRAWN AND DISCARDED TILES " + ANSI.RESET),
                        Direction.NORTH, 1);

        // create content
        CLIFrame contentFrame = frameShipboard
                .merge(frameReservedTiles
                        .merge(frameTileInHand, Direction.SOUTH, 2),
                        Direction.EAST, 6)
                .merge(frameUncoveredTiles, Direction.NORTH, 1);

        // white bg frame container
        int containerRows = contentFrame.getRows() + 2;
        if (containerRows < 24) containerRows = 24;
        CLIFrame frameContainer = getScreenFrame(containerRows, maxWidth, ANSI.BACKGROUND_WHITE);

		return frameContainer.merge(contentFrame, AnchorPoint.CENTER, AnchorPoint.CENTER);
    }
}
