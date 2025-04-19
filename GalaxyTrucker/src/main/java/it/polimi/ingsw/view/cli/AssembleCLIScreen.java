package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.cards.Card;
import it.polimi.ingsw.cards.CardsGroup;
import it.polimi.ingsw.cards.exceptions.CardsGroupException;
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
import java.util.Optional;

public class AssembleCLIScreen extends CLIScreen{

    private TileSkeleton tileInHand = null;

    public AssembleCLIScreen() {
        super("assemble", true, 0);
    }

    private Player getPlayer() {
        return getLastUpdate().getClientPlayer();
    }
    private boolean isEndedAssembly() {
        return getPlayer().getShipBoard().isEndedAssembly();
    }
    private boolean isPlacedOneTile() {
        return getPlayer().getShipBoard().getTiles().size() > 1;  // 1 is the main cabin so needs to be > not >=
    }
    private boolean isFilled() {
        return getPlayer().getShipBoard().isFilled();
    }
    private Optional<CardsGroup> getCardGroupInHand() {
        Integer cgIndex = getPlayer().getCardGroupInHand();
        if (cgIndex == null) return Optional.empty();
        else {
            try {
                return Optional.ofNullable(getLastUpdate().getCurrentGame().getDeck().getGroup(cgIndex));
            } catch (CardsGroupException e) {
                throw new RuntimeException(e);  // should never happen -> runtime exception
            }
        }
    }
    private List<Boolean> listOfAvailableCardGroups() {
        return getLastUpdate().getCurrentGame().getDeck().getGroupsAvailability();
    }
    private String getOccupiedHandMessage() {
        if (tileInHand != null) {
            return "tile";
        }
        if (getCardGroupInHand().isPresent()) {
            return "group of cards";
        }
        return null;
    }

    @Override
    protected boolean switchConditions() {
        return getLastUpdate().getCurrentGame() != null &&
                getLastUpdate().getCurrentGame().getCurrentGamePhaseType() == GamePhaseType.ASSEMBLE;
    }

    @Override
    protected void processCommand(String command, String[] args) throws RemoteException {

        if (isEndedAssembly()) {
            setScreenMessage("You've already finished assembling your majestic ship!\n" +
                    "Wait for the other players to complete their surely more mediocre work.");
            return;
        }

        command = command.toLowerCase();
        String occupiedHand = getOccupiedHandMessage();

        switch (command) {
            case "timerflip":
                //TODO: client side checks
                getServer().flipHourglass(getClient());
                break;

            case "draw":
                if (occupiedHand != null) {
                    setScreenMessage("You can't draw a tile with a " + occupiedHand + " in hand.");
                    break;
                }
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
                if (occupiedHand != null) {
                    setScreenMessage("You can't pick a tile with a " + occupiedHand + " in hand.");
                    break;
                }
                //TODO: client side checks
                if (args.length == 1) {
                    int id = Integer.parseInt(args[0]);
                    getServer().pickTile(getClient(), id);
                }
                else {
                    setScreenMessage("Usage: pick <id>");
                }
                break;

            case "finish":
                if (getLastUpdate().getClientPlayer().getShipBoard() == null) {
                    setScreenMessage("You don't have a shipboard.");
                    break;
                }
                // this finish is done by the player, not forced by the end of time
                // -> check for no tiles / cards groups in hand
                if (occupiedHand != null) {
                    setScreenMessage("You can't finish with a " + occupiedHand + " in hand.");
                    break;
                }

                getServer().finishAssembling(getClient());
                break;

            case "showcg":
                if (!isPlacedOneTile()) {
                    setScreenMessage("You have to place a tile before taking a group of cards.");
                    break;
                }
                if (occupiedHand != null) {
                    setScreenMessage("You can't take a group of cards with a " + occupiedHand + " in hand.");
                    break;
                }
                if (args.length == 1) {
                    int id = Integer.parseInt(args[0]);
                    getServer().showCardGroup(getClient(), id);
                }
                else {
                    setScreenMessage("Usage: showcg <id>");
                }
                break;

            case "hidecg":
                if (getCardGroupInHand().isEmpty()) {
                    setScreenMessage("You have no group of cards in your hand.");
                    break;
                }
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

        if (!isEndedAssembly()) {

            // note: last timerflip only if assemble phase ended for this player
            availableCommands.add("timerflip|Flips the hourglass of the game.");

            boolean hasCardGroupInHand = getCardGroupInHand().isPresent();
            boolean areThereCardGroups = !listOfAvailableCardGroups().isEmpty();

            if (tileInHand == null && !hasCardGroupInHand) {
                // can take tile
                availableCommands.add("draw|Draws a tile from the covered tiles.");
                availableCommands.add("pick <id>|Pick in hand the tile with ID <id>.");
                // or group of cards
                if (areThereCardGroups && isPlacedOneTile()) {
                    availableCommands.add("showcg <id>|Pick and show the card group with ID <id>.");
                }
                // or finish assembly
                // TODO: only after all hourglass flips - 1
                availableCommands.add("finish|Force end of assembly.");
            }
            else if (tileInHand != null) {
                // can only act with the tile in hand
                availableCommands.add("discard|Discard the tile you have in hand.");
                availableCommands.add("rotate <direction>|Rotate the tile you have in hand.");
                availableCommands.add("place <row> <column>|Place the tile from your hand onto your shipboard.");
            }
            else /* hasCardGroupInHand */ {
                // can only act with the card group in hand
                availableCommands.add("hidecg|Set the card group from your hand back to the shared board.");
            }

        }

        return availableCommands;
    }

    private CLIFrame getCLIRTileWithID(TileSkeleton tile) {
        CLIFrame idLabel = new CLIFrame(ANSI.BACKGROUND_YELLOW + ANSI.BLACK + " ID: " +
                tile.getTileId() + " " + ANSI.RESET);
        return tile.getCLIRepresentation().merge(idLabel, Direction.SOUTH)
                .paintBackground(ANSI.BACKGROUND_BLACK);
    }

    private CLIFrame getCLIRUncoveredTiles(int maxWidth) {
        // drawn and discarded tiles
        List<TileSkeleton> uncoveredTiles = getLastUpdate().getCurrentGame().getUncoveredTiles();

        // frame for drawn and discarded tiles as a grid
        List<List<CLIFrame>> frameUncoveredTilesGrid = null;
        final int uncoveredTileHorizontalSpacing = 3;
        if (!uncoveredTiles.isEmpty()) {
            // individual frames for each tile with its ID
            List<CLIFrame> tileFrames = new ArrayList<>();

            for (TileSkeleton tile : uncoveredTiles) {
                tileFrames.add(getCLIRTileWithID(tile));
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
        return CLIFrame
                .fromFramesGrid(frameUncoveredTilesGrid, uncoveredTileHorizontalSpacing, 1)
                // add title
                .merge(new CLIFrame(ANSI.BACKGROUND_BLUE + ANSI.WHITE + " DRAWN AND DISCARDED TILES " + ANSI.RESET),
                        Direction.NORTH, 1);
    }

    private CLIFrame getCLIRTileInHand() {
        // frame for tile in hand
        return new CLIFrame(ANSI.BACKGROUND_BLUE + ANSI.WHITE + " Tile in Hand ")
                .merge((tileInHand != null)
                                ? tileInHand.getCLIRepresentation()
                                : new CLIFrame(TileSkeleton.getForbiddenTileCLIRepresentation(0, 0)),
                        Direction.SOUTH, 1)
                .merge(new CLIFrame(""), Direction.SOUTH)
                .paintBackground(ANSI.BACKGROUND_BLACK);
    }

    private CLIFrame getCLIRReservedTiles() {
        // frame for reserved tiles
        List<TileSkeleton> reservedTiles = getPlayer().getReservedTiles();
        return (reservedTiles.isEmpty()
                ? new CLIFrame(ANSI.RED + "No reserved tiles" + ANSI.RESET)
                : reservedTiles.stream()
                .map(this::getCLIRTileWithID)
                .reduce(new CLIFrame(), (b, t) -> b.isEmpty() ? t : b.merge(t, Direction.EAST, 3))
        )
                .merge(new CLIFrame(ANSI.BACKGROUND_BLUE + ANSI.WHITE + " Reserved Tiles "),
                        Direction.NORTH, 1);
    }

    private CLIFrame getCLIRShipboardInfo() {
        if (isFilled()) {
            // no need to show info: this phase ended
            return new CLIFrame();
        }

        if (isEndedAssembly()) {
            // do not show hand nor reserved tiles. reserved -> lost
            return new CLIFrame(ANSI.BACKGROUND_BLUE + ANSI.WHITE + " Ship assembled! ")
                    .merge(new CLIFrame(ANSI.BLACK + "Wait for other players to finish"), Direction.SOUTH)
                    .merge(new CLIFrame(ANSI.BLACK + "before filling it up"), Direction.SOUTH);
        } else {
            return getCLIRReservedTiles()
                    .merge(getCLIRTileInHand(), Direction.SOUTH, 2);
        }
    }

    private CLIFrame getCLIRCardGroupAvailability(int cgIndex, Boolean available) {
        return new CLIFrame(ANSI.BACKGROUND_YELLOW + ANSI.BLACK + " card group " + cgIndex + " ")
                .merge(new CLIFrame((available == null
                        ? " secret " : (available
                        ? ANSI.GREEN + " available "
                        : ANSI.RED + " unavailable "
                ))), Direction.SOUTH)
                .paintBackground(ANSI.BACKGROUND_BLACK);
    }

    private CLIFrame getCLIRAvailableCardGroups() {
        if (isEndedAssembly()) {
            // no need to show cards groups: player can not interact with them anymore
            return new CLIFrame();
        }

        // frame for available card groups
        List<Boolean> availableCardGroups = listOfAvailableCardGroups();
        CLIFrame result = new CLIFrame();

        for (int i = 0; i < availableCardGroups.size(); i++) {
            if (availableCardGroups.get(i) != null) {
                CLIFrame currentCardGroupInfo = getCLIRCardGroupAvailability(i, availableCardGroups.get(i));
                result = result.isEmpty() ? currentCardGroupInfo : result
                        .merge(currentCardGroupInfo, Direction.SOUTH, 2);
            }
        }

        if (availableCardGroups.isEmpty()) {
            result = result.merge(
                    new CLIFrame(ANSI.RED + "No available")
                            .merge(new CLIFrame(ANSI.RED + "card groups"), Direction.SOUTH),
                    Direction.SOUTH);
        }

        return result.merge(new CLIFrame(ANSI.BACKGROUND_BLUE + ANSI.WHITE + " Card Groups "),
                Direction.NORTH, 1);
    }

    private CLIFrame popupCardGroup(int maxWidth) {
        List<Card> cards = getCardGroupInHand().map(CardsGroup::getGroupCards).orElse(new ArrayList<>());
        List<List<CLIFrame>> cardsAsGrid = null;
        int hSpace = 5;
        int vSpace = 3;
        for (Card card : cards) {
            cardsAsGrid = CLIFrame.addInFramesGrid(cardsAsGrid,
                    card.getCLIRepresentation(), hSpace, maxWidth);
        }
        return CLIFrame.fromFramesGrid(cardsAsGrid, hSpace, vSpace);
    }

    @Override
    public CLIFrame getCLIRepresentation() {
        final int maxWidth = 100;

        // update tile in hand if different from previous
        if ((tileInHand == null) || (!tileInHand.equals(getPlayer().getTileInHand()))) {
            // note: in here also if both are null, but in that case nothing happens -> no problem
            tileInHand = getPlayer().getTileInHand();
        }

        // frame for shipboard
        CLIFrame frameShipboard = getPlayer().getShipBoard().getCLIRepresentation()
                .paintForeground(ANSI.BLACK)
                .merge(new CLIFrame(ANSI.BACKGROUND_BLUE + ANSI.WHITE + " YOUR SHIPBOARD " + ANSI.RESET),
                        Direction.NORTH, 1);

        // create content
        CLIFrame contentFrame = frameShipboard
                .merge(getCLIRAvailableCardGroups(), Direction.WEST, 6)
                .merge(getCLIRShipboardInfo(), Direction.EAST, 6)
                .merge(getCLIRUncoveredTiles(maxWidth), Direction.NORTH, 1)
                // append popup of cards in hand
                .merge(popupCardGroup(maxWidth), AnchorPoint.CENTER, AnchorPoint.CENTER);

        // white bg frame container
        int containerRows = contentFrame.getRows() + 2;
        if (containerRows < 24) containerRows = 24;
        CLIFrame frameContainer = getScreenFrame(containerRows, maxWidth, ANSI.BACKGROUND_WHITE);

		return frameContainer.merge(contentFrame, AnchorPoint.CENTER, AnchorPoint.CENTER);
    }
}
