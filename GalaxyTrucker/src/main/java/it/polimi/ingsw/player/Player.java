package it.polimi.ingsw.player;

import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.game.exceptions.DrawTileException;
import it.polimi.ingsw.network.messages.ClientUpdate;
import it.polimi.ingsw.player.exceptions.AlreadyHaveTileInHandException;
import it.polimi.ingsw.player.exceptions.NoTileInHandException;
import it.polimi.ingsw.player.exceptions.TooManyReservedTilesException;
import it.polimi.ingsw.shipboard.ShipBoard;
import it.polimi.ingsw.shipboard.exceptions.ThatTileIdDoesNotExistsException;
import it.polimi.ingsw.shipboard.tiles.TileSkeleton;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Player implements Serializable {

    private final String username;
    private UUID connectionUUID;

    /**
     * Tile currently held by the player
     */
    private TileSkeleton tileInHand;

    /**
     * Tiles reserved by the player during building phase (MAX 2)
     */
    private final List<TileSkeleton> reservedTiles;

    /**
     * Number of tiles that the player will have to pay for at the end of the game (destroyed tiles or reserved and not used tiles)
     */
    private final List<TileSkeleton> discardedTiles;

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

    public Player(String username, UUID connectionUUID) {
        this.username = username;
        this.reservedTiles = new ArrayList<>(2);
        this.discardedTiles = new ArrayList<>();
        this.connectionUUID = connectionUUID;
        credits = 0;
    }

    /**
     *
     * @return player's username
     */
    public String getUsername() {
        return username;
    }

    public UUID getConnectionUUID() {
        return connectionUUID;
    }

    /**
     *
     * @return tile held by the player
     */
        public TileSkeleton getTileInHand() {
        return tileInHand;
    }

    /**
     * @param tileInHand tile held by the player. Can be null to reset an empty hand
     */
    public void setTileInHand(TileSkeleton tileInHand) throws AlreadyHaveTileInHandException {
        if (getTileInHand() != null && tileInHand != null) {
            throw new AlreadyHaveTileInHandException("You already are holding a tile.");
        }
        this.tileInHand = tileInHand;
    }

    /**
     *
     * @return reserved tiles array
     */
    public List<TileSkeleton> getReservedTiles() {
        return reservedTiles;
    }

    /**
     * Assigns the tile to the first slot of reservedTiles
     * @param reservedTile tile to save in the array
     * @throws TooManyReservedTilesException called if the array is already full
     */
    public void setReservedTiles(TileSkeleton reservedTile) throws TooManyReservedTilesException, NoTileInHandException {
        if (reservedTiles.size() == 2) {
            throw new TooManyReservedTilesException();
        }
        reservedTiles.add(reservedTile);
    }

    /**
     *
     * @return the list of discarded tiles
     */
    public List<TileSkeleton> getDiscardedTiles() {
        return discardedTiles;
    }

    /**
     *
     * @param tile tile to add to the list
     */
    public void addDiscardedTiles(TileSkeleton tile) {
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

    /**
     * Adds the specified number of credits to the current total.
     *
     * @param credits the number of credits to add
     */
    public void addCredits(int credits) {
        this.credits += credits;
    }

    /**
     * Returns the current number of credits.
     *
     * @return the current credit balance
     */
    public int getCredits() {
        return credits;
    }

    /**
     * Sets the current position of the player or entity.
     *
     * @param position the new position to set
     */
    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * Returns the current position of the player or entity.
     *
     * @return the current position
     */
    public int getPosition() {
        return position;
    }

    /**
     * Randomly picks a tile from the covered tiles pile. Removes it from the pile and assigns it to the player.
     * @return
     * @throws DrawTileException
     */
    public void drawTile(GameData gameData) throws DrawTileException, AlreadyHaveTileInHandException {
        if(gameData.getCurrentGamePhaseType() != GamePhaseType.ASSEMBLE){
            throw new DrawTileException("You can only do this during Assembly.");
        }
        if (gameData.getCoveredTiles().isEmpty()) {
            throw new DrawTileException("There are no covered tiles available.");
        }
        TileSkeleton t = gameData.getCoveredTiles().removeFirst();
        try{
            setTileInHand(t);
        }catch(Exception e){
            //put tile back in place and transmit the error
            gameData.getCoveredTiles().add(t);
            throw e;
        }
    }

    public void discardTile(GameData gameData) throws NoTileInHandException {

        if (getTileInHand() == null){
            throw new NoTileInHandException();
        }

        TileSkeleton t = getTileInHand();
        gameData.getDrawnTiles().add(t);
        this.tileInHand = null;
    }

    public void pickTile(GameData gameData, Integer id) throws AlreadyHaveTileInHandException, ThatTileIdDoesNotExistsException {
        TileSkeleton tile = gameData.getTileWithId(id);
        setTileInHand(tile);
    }
}