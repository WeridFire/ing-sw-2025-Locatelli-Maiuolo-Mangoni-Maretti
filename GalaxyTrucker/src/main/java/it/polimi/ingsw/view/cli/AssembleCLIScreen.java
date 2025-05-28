package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.cards.Card;
import it.polimi.ingsw.cards.CardsGroup;
import it.polimi.ingsw.controller.cp.AssembleCommandsProcessor;
import it.polimi.ingsw.controller.states.AssembleState;
import it.polimi.ingsw.controller.states.CommonState;
import it.polimi.ingsw.enums.AnchorPoint;
import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.network.GameClient;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.shipboard.tiles.TileSkeleton;
import it.polimi.ingsw.util.GameLevelStandards;

import java.util.ArrayList;
import java.util.List;

public class AssembleCLIScreen extends CLIScreen {

    public AssembleCLIScreen(GameClient gameClient) {
        super("assemble", false, 0, new AssembleCommandsProcessor(gameClient));
    }

    @Override
    protected boolean switchConditions() {
        return CommonState.isCurrentPhase(GamePhaseType.ASSEMBLE);
    }

    private CLIFrame getCLIRTileWithID(TileSkeleton tile) {
        CLIFrame idLabel = new CLIFrame(ANSI.BACKGROUND_YELLOW + ANSI.BLACK + " ID: " +
                tile.getTileId() + " " + ANSI.RESET);
        return tile.getCLIRepresentation().merge(idLabel, Direction.SOUTH)
                .paintBackground(ANSI.BACKGROUND_BLACK);
    }

    private CLIFrame getCLIRUncoveredTiles(int maxWidth) {
        // drawn and discarded tiles
        List<TileSkeleton> uncoveredTiles = AssembleState.getGameData().getUncoveredTiles();

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
                .merge(new CLIFrame(ANSI.BACKGROUND_BLUE + ANSI.WHITE + " UNCOVERED AND DISCARDED TILES " + ANSI.RESET),
                        Direction.NORTH, 1);
    }

    private CLIFrame getCLIRTileInHand() {
        // frame for tile in hand
        TileSkeleton tileInHand = AssembleState.getTileInHand();
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
        List<TileSkeleton> reservedTiles = AssembleState.getPlayer().getReservedTiles();
        return (reservedTiles.isEmpty()
                ? new CLIFrame(ANSI.RED + "No reserved tiles" + ANSI.RESET)
                : reservedTiles.stream()
                .map(this::getCLIRTileWithID)
                .reduce(new CLIFrame(), (b, t) -> b.isEmpty() ? t : b.merge(t, Direction.EAST, 3))
        )
                .merge(new CLIFrame(ANSI.BACKGROUND_BLUE + ANSI.WHITE + " Reserved Tiles "),
                        Direction.NORTH, 1);
    }

    private CLIFrame getCLIRSelfInfo() {
        if (AssembleState.isShipboardFilled()) {
            // no need to show info: this phase ended
            return new CLIFrame();
        }

        if (AssembleState.isEndedAssembly()) {
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

    private CLIFrame getCLIRTimerInfo() {
        // frame for timer info
        CLIFrame result = new CLIFrame();
        Integer timerSlot = AssembleState.getTimerSlotIndex();

        if (timerSlot != null) {
            // words info
            boolean isTimerRunning = AssembleState.isTimerRunning();
            result = result.merge(new CLIFrame(ANSI.BLACK + "The timer is"), Direction.SOUTH);
            if (!isTimerRunning) {
                result = result.merge(new CLIFrame(ANSI.RED + "NOT"), Direction.SOUTH);
            }
            result = result.merge(new CLIFrame(ANSI.BLACK + "running"), Direction.SOUTH);
            // create info for slot
            StringBuilder slotsInfo = new StringBuilder();
            int totalSlots = GameLevelStandards.getTimerSlotsCount(AssembleState.getGameData().getLevel());
            for (int i = 0; i < totalSlots; i++) {
                if (i > 0) slotsInfo.append(' ');
                if (i == timerSlot) {
                    slotsInfo.append(isTimerRunning ? ANSI.GREEN : ANSI.RED).append("●").append(ANSI.RESET);
                } else {
                    slotsInfo.append("●");
                }
            }
            result = result.merge(new CLIFrame(slotsInfo.toString()), Direction.SOUTH);
        }
        else {
            result = result.merge(
                    new CLIFrame(ANSI.RED + "No available")
                            .merge(new CLIFrame(ANSI.RED + "timer"), Direction.SOUTH),
                    Direction.SOUTH);
        }

        return result.merge(new CLIFrame(ANSI.BACKGROUND_BLUE + ANSI.WHITE + " TIMER "),
                Direction.NORTH, 1);
    }

    private CLIFrame getCLIRAvailableCardGroups() {
        if (AssembleState.isEndedAssembly()) {
            // no need to show cards groups: player can not interact with them anymore
            return new CLIFrame();
        }

        // frame for available card groups
        List<Boolean> availableCardGroups = AssembleState.listOfAvailableCardGroups();
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

    private CLIFrame getCLIRCommonInfo() {
        return getCLIRTimerInfo().merge(getCLIRAvailableCardGroups(), Direction.SOUTH, 2);
    }

    private CLIFrame popupCardGroup(int maxWidth) {
        List<Card> cards = AssembleState.getCardGroupInHand().map(CardsGroup::getGroupCards).orElse(new ArrayList<>());
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
        Player spectatedPlayer = AssembleState.getSpectatedPlayer();

        // frame for shipboard
        CLIFrame frameShipboard = spectatedPlayer.getShipBoard().getCLIRepresentation()
                .paintForeground(ANSI.BLACK)
                .merge(new CLIFrame(ANSI.BACKGROUND_BLUE + ANSI.WHITE + " "
                                + (AssembleState.isSpectatingOther()
                                ? spectatedPlayer.getUsername() + "'s"
                                : "YOUR"
                                ) + " SHIP " + ANSI.RESET),
                        Direction.NORTH, 1);

        // create content
        CLIFrame contentFrame = frameShipboard
                .merge(getCLIRCommonInfo(), Direction.WEST, 5)
                .merge(getCLIRSelfInfo(), Direction.EAST, 5)
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
