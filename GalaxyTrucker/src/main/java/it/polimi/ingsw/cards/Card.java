package src.main.java.it.polimi.ingsw.cards;

import src.main.java.it.polimi.ingsw.game.GameData;
import src.main.java.it.polimi.ingsw.player.Player;
import src.main.java.it.polimi.ingsw.GamesHandler;

import java.util.UUID;

public abstract class Card {

    /**
     * The level this card is part of.
     */
    private int level;
    /**
     * The texture associated with this card.
     */
    private String textureName;

    /**
     * Instances a card.
     * @param textureName The name of the texture of the card.
     * @param level The level of this card.
     */
    public Card(String textureName, int level){
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
}
