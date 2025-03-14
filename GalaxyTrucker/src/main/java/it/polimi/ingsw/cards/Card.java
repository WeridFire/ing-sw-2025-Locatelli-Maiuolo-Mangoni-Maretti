package src.main.java.it.polimi.ingsw.cards;

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
     * Given a player and an amount of positions, handles the movement of it on the game board.
     * @param playerName The string name of the player.
     * @param position The amount of positions (or days) to move.
     */
    public void movePlayer(String playerName, int position){

    }



    public abstract void playEffect();

}
