package it.polimi.ingsw.cards;

import it.polimi.ingsw.enums.AnchorPoint;
import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.shipboard.exceptions.NoTileFoundException;
import it.polimi.ingsw.shipboard.exceptions.OutOfBuildingAreaException;
import it.polimi.ingsw.util.GameLevelStandards;
import it.polimi.ingsw.view.cli.ANSI;
import it.polimi.ingsw.view.cli.CLIFrame;
import it.polimi.ingsw.view.cli.ICLIPrintable;

import java.io.Serializable;

import static it.polimi.ingsw.view.cli.CLIScreen.getScreenFrame;

public abstract class Card implements ICLIPrintable, Serializable {
    /**
     * The texture associated with this card.
     */
    private final String textureName;
    /**
     * The name of the card, representative of the card function (e.g. "Open Space")
     */
    private final String title;
    /**
     * The level this card is part of.
     */
    private GameLevel level;
    /**
     * The ANSI background color for this card.
     * It depends on the {@link #level} and it's used in {@link #getCLIRepresentation()}
     * -> calculated only once for performance optimization.
     */
    private String backgroundColor;

    /**
     * Instances a card.
     * @param title The name of the card.
     * @param textureName The name of the texture of the card.
     * @param level The level of this card.
     */
    public Card(String title, String textureName, GameLevel level){
        this.title = title;
        this.textureName = textureName;
        setLevel(level);
    }

    /**
     * Instances a card.
     * @param title The name of the card.
     * @param textureName The name of the texture of the card.
     * @param level The level of this card in integer format.
     */
    public Card(String title, String textureName, int level){
        this(title, textureName, GameLevel.fromInteger(level));
    }

    /**
     * Sets the level for this card.
     * Can change dynamically, but once a card has been created it should stay the same level.
     * @param level the new level of this card
     */
    public void setLevel(GameLevel level) {
        this.level = level;
        backgroundColor = GameLevelStandards.getColorANSI(level, true);
    }

    /**
     * Sets the level for this card.
     * Can change dynamically, but once a card has been created it should stay the same level.
     * @param level the new level of this card in integer format
     */
    public void setLevel(int level) {
        setLevel(GameLevel.fromInteger(level));
    }

    /**
     * Generic function to apply the effect of the card on the game.
     * @param game The game data, to access and modify the game.
     */
    public abstract void playTask(GameData game, Player player);

    public abstract void proceedTaskLoop(GameData game, Player player);

    /**
     * @return The texture associated to this card.
     */
    public String getTextureName(){
        return textureName;
    }

    /**
     * @return The game level associated to this card.
     */
    public GameLevel getLevel(){
        return level;
    }

    @Override
    public CLIFrame getCLIRepresentation() {
        return getScreenFrame(11, 20, backgroundColor)
                .merge(new CLIFrame(backgroundColor + ANSI.WHITE + " " + title + " "),
                        AnchorPoint.TOP, AnchorPoint.CENTER);
    }
}
