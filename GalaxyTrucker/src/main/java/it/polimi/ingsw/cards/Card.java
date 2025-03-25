package src.main.java.it.polimi.ingsw.cards;

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
        for(Player p : GamesHandler.getInstance().getGame(gameId).getGameData().getPlayers())
        {//calcolo dei giocatori superati
            if(p != player){ //funziona se sono ordinati nell'array
                if(player.getDistFromFirst() - p.getDistFromFirst() < position){
                    position =+ 1;
                }
            }
        }
        if(player.getDistFromFirst() = 0){ //il player è primo
            for(Player p : GamesHandler.getInstance().getGame(gameId).getGameData().getPlayers())
                if(p != player){
                    p.setDistFromFirst(-1*position); //"arretra" tutti gli altri giocatori di position posizioni
                }
        }
        elseif(player.getDistFromFirst() < position){ //il player supera il primo
            tmp = player.getDistFromFirst();
            player.setDistFromFirst(tmp); //è meglio se lo salvo nel game, in modo da non dover chiamare ogni player ogni volta
            movePlayer(player, position-tmp);
        }
        else{ //tutto il resto
            player.setDistFromFirst(position);
        }
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
