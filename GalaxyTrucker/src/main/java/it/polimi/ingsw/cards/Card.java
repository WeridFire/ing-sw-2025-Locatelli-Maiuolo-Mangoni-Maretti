package src.main.java.it.polimi.ingsw.cards;

import src.main.java.it.polimi.ingsw.player.Player;

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
    private UUID gameId;

    /**
     * Instances a card.
     * @param textureName The name of the texture of the card.
     * @param level The level of this card.
     * @param gameId The ID of the game this card is part of.
     */
    public Card(String textureName, int level, UUID gameId){
        this.textureName = textureName;
        this.level = level;
        this.gameId = gameId;
    };

    /**
     * Given a player and an amount of positions, handles the movement of it on the game board.
     * @param player The string name of the player.
     * @param position The amount of positions (or days) to move.
     */
    public static void movePlayer(Player player, int position){
        //TBD - Implement the logic of moving the players.
    }

    /**
     * Generic function to apply the effect of the card on the game.
     * @param gameId The UUID of the game associated to this card, to access the game handler.
     */
    public abstract void playEffect(UUID gameId);

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
