package it.polimi.ingsw.player;

import it.polimi.ingsw.cards.CardsGroup;
import it.polimi.ingsw.enums.Rotation;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.game.exceptions.DrawTileException;
import it.polimi.ingsw.player.exceptions.*;
import it.polimi.ingsw.playerInput.PIRs.PIRHandler;
import it.polimi.ingsw.shipboard.ShipBoard;
import it.polimi.ingsw.shipboard.exceptions.*;
import it.polimi.ingsw.shipboard.tiles.TileSkeleton;
import it.polimi.ingsw.shipboard.tiles.exceptions.FixedTileException;
import it.polimi.ingsw.util.Coordinates;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Player implements Serializable {

    private final String username;
    private final UUID connectionUUID;

    /**
     * Tile currently held by the player
     */
    private TileSkeleton tileInHand;

    /**
     * Tiles reserved by the player during building phase (MAX 2)
     */
    private final List<TileSkeleton> reservedTiles;

    /**
     * true <==> tile in hand has been picked from reserved tiles
     * (can not discard and count as lost if not placed before end of assembly)
     */  // TODO: implement check at the end of assembly to count it as reserved tile
    private boolean isTileInHandFromReserved = false;

    /**
     * Number of tiles that the player will have to pay for at the end of the game (destroyed tiles or reserved and not used tiles)
     */
    private final List<TileSkeleton> lostTiles;

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

    /**
     *  The id of the cardgroup the player is currently holding
     */
    private Integer cardGroupInHand;

    public Player(String username, UUID connectionUUID) {
        this.username = username;
        this.connectionUUID = connectionUUID;
        reservedTiles = new ArrayList<>(2);
        lostTiles = new ArrayList<>();
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
     * @param tileInHand tile to be held by the player
     * @throws AlreadyHaveTileInHandException if already have tile in hand
     * @throws TileCanNotDisappearException if {@code tileInHand == null}: it would make the tile disappear from
     * player's hand
     */
    public void setTileInHand(TileSkeleton tileInHand) throws AlreadyHaveTileInHandException, TileCanNotDisappearException {
        if (tileInHand == null) {
            throw new TileCanNotDisappearException("You have a tile in hand: it can not disappear.");
        }
        if (getTileInHand() != null) {
            throw new AlreadyHaveTileInHandException();
        }
        this.tileInHand = tileInHand;
    }

    /**
     * Clear the hand. Requires to have put the tile in hand somewhere else before.
     */
    private void removeTileFromHand() {
        tileInHand = null;
        isTileInHandFromReserved = false;
    }

    /**
     *
     * @return reserved tiles array
     */
    public List<TileSkeleton> getReservedTiles() {
        return reservedTiles;
    }

    /**
     * Assigns the tile in hand to the first available slot of reservedTiles. Then already removes tile from hand.
     * @throws NoTileInHandException if there is no tile in hand
     * @throws TooManyReservedTilesException if there is no more space to reserve any tile
     */
    public void reserveTile() throws NoTileInHandException, TooManyReservedTilesException {
        if (getTileInHand() == null) {
            throw new NoTileInHandException();
        }
        if (reservedTiles.size() == 2) {
            throw new TooManyReservedTilesException();
        }

        reservedTiles.add(getTileInHand());
        removeTileFromHand();
    }

    /**
     *
     * @return the list of discarded tiles
     */
    public List<TileSkeleton> getLostTiles() {
        return lostTiles;
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
     * Returns the id of the held cardgroup
     *
     * @return the id
     */
    public Integer getCardGroupInHand() {
        return cardGroupInHand;
    }

    /**
     * Sets the id of the held cardgroup
     *
     *
     */
    public void setCardGroupInHand(Integer id) {
        this.cardGroupInHand = id;
    }

    /**
     * Clears the id of the held cardgroup
     *
     *
     */
    public void clearCardGroupInHand() {
        this.cardGroupInHand = null;
    }

    /**
     * Randomly picks a tile from the covered tiles pile. Removes it from the pile and assigns it to the player.
     * @return
     * @throws DrawTileException
     */
    public void drawTile(GameData gameData) throws DrawTileException, AlreadyHaveTileInHandException {
        if (gameData.getCoveredTiles().isEmpty()) {
            throw new DrawTileException("There are no covered tiles available.");
        }
        TileSkeleton t = gameData.getCoveredTiles().removeFirst();
        try{
            setTileInHand(t);
        } catch (TileCanNotDisappearException e) {
            throw new RuntimeException(e);  // should never happen -> runtime exception
        } catch (Exception e){
            //put tile back in place and transmit the error
            gameData.getCoveredTiles().add(t);
            throw e;
        }
    }

    public void discardTile(GameData gameData) throws NoTileInHandException, ReservedTileException {
        TileSkeleton tileInHand = getTileInHand();
        if (tileInHand == null){
            throw new NoTileInHandException();
        }
        if (isTileInHandFromReserved) {
            throw new ReservedTileException("It's not possible to discard a reserved tile.");
        }

        gameData.getUncoveredTiles().add(tileInHand);
        removeTileFromHand();
    }

    public void placeTile(Coordinates coordinates, Rotation rotation) throws NoTileInHandException, NoShipboardException,
            FixedTileException, TileAlreadyPresentException, TileWithoutNeighborException, OutOfBuildingAreaException {
        TileSkeleton tileInHand = getTileInHand();
        ShipBoard shipBoard = getShipBoard();
        if (tileInHand == null) {
            throw new NoTileInHandException();
        }
        if (shipBoard == null) {
            throw new NoShipboardException();
        }

        tileInHand.resetRotation();  // ensure there is no unhandled previous rotation
        tileInHand.rotateTile(rotation);  // apply the desired rotation
        shipBoard.setTile(tileInHand, coordinates);  // place the tile
        // if here, the tile has been correctly placed: remove it from hand
        removeTileFromHand();
    }

    public void pickTile(GameData gameData, Integer id) throws AlreadyHaveTileInHandException, ThatTileIdDoesNotExistsException {
        if (getTileInHand() != null) {
            throw new AlreadyHaveTileInHandException();
        }
        TileSkeleton tile;
        try {  // first search in the discarded tiles
            tile = gameData.getTileWithId(id);
        } catch (ThatTileIdDoesNotExistsException e) {
            // if not found: search in the reserved tiles
            tile = null;
            for (int i = 0; i < reservedTiles.size(); i++) {  // first search in the reserved tiles
                if (reservedTiles.get(i).getTileId() == id) {
                    tile = reservedTiles.remove(i);
                    isTileInHandFromReserved = true;
                    break;
                }
            }
            if (tile == null) {  // if still not found: can't search in other places
                throw e;
            }
        }
        // if here: tile found and no other tiles in hand
        try {
            setTileInHand(tile);
        } catch (AlreadyHaveTileInHandException | TileCanNotDisappearException e) {
            throw new RuntimeException(e);  // should never happen -> runtime error
        }
    }

    public void endAssembly() throws NoShipboardException, AlreadyEndedAssemblyException {
        ShipBoard shipBoard = getShipBoard();
        if (shipBoard == null) {
            throw new NoShipboardException();
        }

        shipBoard.endAssembly();
    }


}