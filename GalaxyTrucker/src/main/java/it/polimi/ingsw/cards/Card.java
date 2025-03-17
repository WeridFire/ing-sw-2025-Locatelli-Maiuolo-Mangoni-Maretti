package src.main.java.it.polimi.ingsw.cards;

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


    public Card(String textureName, int level){
        this.textureName = textureName;
        this.level = level;
    };

    /**
     * Given a player and an amount of positions, handles the movement of it on the game board.
     * @param playerName The string name of the player.
     * @param position The amount of positions (or days) to move.
     */
    public void movePlayer(String playerName, int position){
        //TBD - Implement the logic of moving the players.
    }

    /**
     * Generic function to apply the effect of the card on the game.
     * @param gameId The UUID of the game associated to this card, to access the game handler.
     */
    public abstract void playEffect(UUID gameId);

}
