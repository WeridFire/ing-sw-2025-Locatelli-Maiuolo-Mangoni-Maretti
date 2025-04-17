package it.polimi.ingsw.cards;

import it.polimi.ingsw.enums.AnchorPoint;
import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.view.cli.ANSI;
import it.polimi.ingsw.view.cli.CLIFrame;
import it.polimi.ingsw.view.cli.ICLIPrintable;

import java.io.Serializable;

import static it.polimi.ingsw.view.cli.CLIScreen.getScreenFrame;

public abstract class Card implements ICLIPrintable, Serializable {

    /**
     * The level this card is part of.
     */
    private int level;
    /**
     * The texture associated with this card.
     */
    private String textureName;
    /**
     * The name of the card, representative of the card function (e.g. "Open Space")
     */
    private final String title;

    /**
     * Instances a card.
     * @param title The name of the card.
     * @param textureName The name of the texture of the card.
     * @param level The level of this card.
     */
    public Card(String title, String textureName, int level){
        this.title = title;
        this.textureName = textureName;
        this.level = level;
    };

    /**
     * Given a player and an amount of positions, handles the movement of it on the game board.
     * @param player The string name of the player.
     * @param position The amount of positions (or days) to move.
     */

    /**
     * Generic function to apply the effect of the card on the game.
     * @param game The game data, to access and modify the game.
     */
    public abstract void playEffect(GameData game);

    /**
     * Updates the level of the card.
     * @param level the new level of the card.
     */
    public void setLevel(int level){
        this.level = level;
    }

    /**
     *
     * @return The texture associated to this card.
     */
    public String getTextureName(){
        return this.textureName;
    }

    @Override
    public CLIFrame getCLIRepresentation() {
        String backgroundColor = switch(GameLevel.fromInteger(level)) {
            case TESTFLIGHT, ONE -> ANSI.BACKGROUND_CYAN;
            case TWO -> ANSI.BACKGROUND_PURPLE;
            default -> "";
        };
        return getScreenFrame(11, 20, backgroundColor)
                .merge(new CLIFrame(backgroundColor + ANSI.WHITE + " " + title + " "),
                        AnchorPoint.TOP, AnchorPoint.CENTER);
    }
}
