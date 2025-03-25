package src.main.java.it.polimi.ingsw.player;

import src.main.java.it.polimi.ingsw.player.exceptions.TooManyReservedTilesException;
import src.main.java.it.polimi.ingsw.shipboard.SideType;
import src.main.java.it.polimi.ingsw.shipboard.ShipBoard;
import src.main.java.it.polimi.ingsw.shipboard.tiles.TileSkeleton;

import java.util.ArrayList;
import java.util.List;

public class Player {

    private final String username;

    /**
     * Tile currently held by the player
     */
    private TileSkeleton<SideType> tileInHand;

    /**
     * Tiles reserved by the player during building phase (MAX 2)
     */
    private final List<TileSkeleton<SideType>> reservedTiles;

    /**
     * Number of tiles that the player will have to pay for at the end of the game (destroyed tiles or reserved and not used tiles)
     */
    private final List<TileSkeleton<SideType>> discardedTiles;

    /**
     * The player's shipboard
     */
    private ShipBoard shipBoard;

    /**
     * The player's space credits
     */
    private int credits;

    /**
     * The player's absolute position on the flight board
     */
    private int position;

    public Player(String username) {
        this.username = username;
        this.reservedTiles = new ArrayList<>(2);
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
    public TileSkeleton<SideType> getTileInHand() {
        return tileInHand;
    }

    /**
     *
     * @param tileInHand tile held by the player
     */
    public void setTileInHand(TileSkeleton<SideType> tileInHand) {
        this.tileInHand = tileInHand;
    }

    /**
     *
     * @return reserved tiles array
     */
    public List<TileSkeleton<SideType>> getReservedTiles() {
        return reservedTiles;
    }

    /**
     * Assigns the tile to the first slot of reservedTiles
     * @param reservedTile tile to save in the array
     * @throws TooManyReservedTilesException called if the array is already full
     */
    public void setReservedTiles(TileSkeleton<SideType> reservedTile) throws TooManyReservedTilesException {
        if (reservedTiles.size() == 2) {
            throw new TooManyReservedTilesException();
        }
        reservedTiles.add(reservedTile);
    }

    /**
     *
     * @return the list of discarded tiles
     */
    public List<TileSkeleton<SideType>> getDiscardedTiles() {
        return discardedTiles;
    }

    /**
     *
     * @param tile tile to add to the list
     */
    public void addDiscardedTiles(TileSkeleton<SideType> tile) {
        discardedTiles.add(tile);
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

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }
}