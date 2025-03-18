package src.main.java.it.polimi.ingsw.player;

import src.main.java.it.polimi.ingsw.player.exceptions.TooManyReservedTilesException;
import src.main.java.it.polimi.ingsw.shipboard1.tiles.Tile;
import src.main.java.it.polimi.ingsw.shipboard1.ShipBoard;

import java.util.ArrayList;
import java.util.List;

public class Player {

    private final String username;

    /**
     * Tile currently held by the player
     */
    private Tile tileInHand;

    /**
     * Tiles reserved by the player during building phase (MAX 2)
     */
    private Tile[] reservedTiles;

    /**
     * Number of tiles that the player will have to pay for at the end of the game (destroyed tiles or reserved and not used tiles)
     */
    private List<Tile> discardedTiles;

    /**
     * The player's shipboard
     */
    private ShipBoard shipBoard;

    /**
     * The player's space credits
     */
    private int credits;

    public Player(String username) {
        this.username = username;
        this.reservedTiles = new Tile[2];
        this.discardedTiles = new ArrayList<>();
        credits = 0;
    }

    /**
     *
     * @return player's username
     */
    public String getUsername() {
        return username;
    }

    /**
     *
     * @return tile held by the player
     */
    public Tile getTileInHand() {
        return tileInHand;
    }

    /**
     *
     * @param tileInHand tile held by the player
     */
    public void setTileInHand(Tile tileInHand) {
        this.tileInHand = tileInHand;
    }

    /**
     *
     * @return reserved tiles array
     */
    public Tile[] getReservedTiles() {
        return reservedTiles;
    }

    /**
     * Assigns the tile to the first slot of reservedTiles
     * @param reservedTiles tile to save in the array
     * @throws TooManyReservedTilesException called if the array is already full
     */
    public void setReservedTiles(Tile reservedTiles) throws TooManyReservedTilesException {
        if(this.reservedTiles[0] == null) {
            this.reservedTiles[0] = reservedTiles;
        }
        else if (this.reservedTiles[1] == null) {
            this.reservedTiles[1] = reservedTiles;
        }
        else{
            throw new TooManyReservedTilesException();
        }
    }

    /**
     *
     * @return the list of discarded tiles
     */
    public List<Tile> getDiscardedTiles() {
        return discardedTiles;
    }

    /**
     *
     * @param tile tile to add to the list
     */
    public void addDiscardedTiles(Tile tile) {
        this.discardedTiles.add(tile);
    }

    /**
     *
     * @return the player's shipboard
     */
    public ShipBoard getShipBoard() {
        return shipBoard;
    }

    /**
     * Assigns the shipboard to the player
     * @param shipBoard this player's shipboard
     */
    public void setShipBoard(ShipBoard shipBoard) {
        this.shipBoard = shipBoard;
    }

    public void addCredits(int credits) {
        this.credits += credits;
    }

    public int getCredits() {
        return credits;
    }
}