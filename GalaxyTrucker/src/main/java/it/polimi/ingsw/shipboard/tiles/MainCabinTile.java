package it.polimi.ingsw.shipboard.tiles;

import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.shipboard.LoadableType;
import it.polimi.ingsw.shipboard.SideType;
import it.polimi.ingsw.shipboard.visitors.TileVisitor;
import it.polimi.ingsw.view.cli.ANSI;
import it.polimi.ingsw.view.cli.CLIFrame;

import java.util.Set;

public class MainCabinTile extends CabinTile {

    public enum Color {
        BLUE, RED, GREEN, YELLOW;

        private static final int numOfColors = Color.values().length;

        public String toANSIColor(boolean background) {
            return switch (this) {
                case BLUE -> background ? ANSI.BACKGROUND_BLUE : ANSI.BLUE;
                case RED -> background ? ANSI.BACKGROUND_RED : ANSI.RED;
                case GREEN -> background ? ANSI.BACKGROUND_GREEN : ANSI.GREEN;
                case YELLOW -> background ? ANSI.BACKGROUND_YELLOW : ANSI.YELLOW;
            };
        }

        public static Color fromPlayerIndex(int playerIndex) {
            if (playerIndex < 0 || playerIndex >= numOfColors) {
                throw new IllegalArgumentException("Invalid player index: " + playerIndex +
                        ". Should be between 0 and " + (numOfColors - 1));
            }
            return Color.values()[playerIndex];
        }
    }

    private final Color color;

    /**
     * Constructs a Main CabinTile.
     * Since it's the main cabin, it's not allowed to load aliens here.
     */
    public MainCabinTile(Color color) {
        super(Direction.sortedArray(SideType.UNIVERSAL, SideType.UNIVERSAL,
                        SideType.UNIVERSAL, SideType.UNIVERSAL).toArray(new SideType[0]),
                Set.of(LoadableType.HUMAN));

        setCLISymbol(color.toANSIColor(true) + "CB" + ANSI.RESET);
        this.color = color;
    }

    @Override
    public void accept(TileVisitor visitor) {
        visitor.visitMainCabin(this);
    }
}
