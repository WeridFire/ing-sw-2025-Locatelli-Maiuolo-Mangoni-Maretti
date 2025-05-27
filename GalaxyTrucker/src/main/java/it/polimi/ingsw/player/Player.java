package it.polimi.ingsw.player;

import it.polimi.ingsw.enums.Rotation;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.game.exceptions.DrawTileException;
import it.polimi.ingsw.player.exceptions.*;
import it.polimi.ingsw.player.kpf.KeepPlayerFlyingPredicate;
import it.polimi.ingsw.shipboard.ShipBoard;
import it.polimi.ingsw.shipboard.exceptions.*;
import it.polimi.ingsw.shipboard.tiles.MainCabinTile;
import it.polimi.ingsw.shipboard.tiles.TileSkeleton;
import it.polimi.ingsw.shipboard.tiles.exceptions.FixedTileException;
import it.polimi.ingsw.util.Coordinates;
import it.polimi.ingsw.view.cli.ANSI;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Player implements Serializable {

    private final String username;
    private UUID connectionUUID;

    /**
     * The player's shipboard
     */
    private ShipBoard shipBoard;

    /**
     * The name of the player that the current player is spectating.
     * String is used instead of the Player instance to avoid NullPointerExceptions during game serialization
     */
    private String spectating;

    /**
     * The player color. Implemented as the same of its ship main cabin color.
     */
    private final MainCabinTile.Color color;

    /**
     * Number of tiles that the player will have to pay for at the end of the game
     * (destroyed tiles, or reserved and not used tiles)
     */
    private final List<TileSkeleton> lostTiles;

    /**
     * Tiles reserved by the player during building phase (MAX 2)
     */
    private final List<TileSkeleton> reservedTiles;

    /**
     * The id of the cardgroup the player is currently holding,
     * or null if this player has not any cardgroup in hand at the moment.
     */
    private Integer cardGroupInHand;

    /**
     * Tile currently held by the player, or null if this player has no tile in hand at the moment.
     */
    private TileSkeleton tileInHand;

    /**
     * true if the tile in hand has been picked from reserved tiles
     * (can not discard and count as lost if not placed before end of assembly)
     */
    private boolean tileInHandFromReserved;

    /**
     * The player's space credits
     */
    private int credits;

    /**
     * The player's absolute position on the flight board,
     * or null if this player has not been placed on the route-board yet (or if it's not on it anymore).
     */
    private Integer position;

    /**
     * true if the player is out of the flight
     */
    private boolean endedFlight;

    /**
     * true if the player desire to end the flight at the end of the current adventure
     */
    private boolean requestedEndFlight;

    /**
     * a predicate to execute just before ending the flight: if it's true, the player is safe and shall not end the flight
     */
    private KeepPlayerFlyingPredicate saveFromEndFlight;

    /**
     * Gets the player that is currently being spectated
     * @return
     */
	public String getSpectating(){
        return spectating;
    }

    /**
     * Sets the player that needs to be spectated. No checks are performed at this state (see usages for checks)
     * @param spectating The username of the target player to spectate
     */
    public void setSpectating(String spectating){
        this.spectating = spectating;
    }


    public Player(String username, UUID connectionUUID, MainCabinTile.Color color) {
        this.username = username;
        this.connectionUUID = connectionUUID;
        this.color = color;

        lostTiles = new ArrayList<>();
        reservedTiles = new ArrayList<>(2);

        cardGroupInHand = null;
        tileInHand = null;
        tileInHandFromReserved = false;

        credits = 0;
        position = null;

        endedFlight = false;
        requestedEndFlight = false;
        saveFromEndFlight = null;

        spectating = username;
    }

    /**
     * @return player's username
     */
    public String getUsername() {
        return username;
    }

    public UUID getConnectionUUID() {
        return connectionUUID;
    }

    public void setConnectionUUID(UUID conn) {
        this.connectionUUID = conn;
    }

    public void disconnect() {
        this.connectionUUID = null;
    }

    public boolean isConnected(){
        return getConnectionUUID() != null;
    }

    /**
     * @return tile held by the player, or {@code null} if this player has no tile in hand
     */
    public TileSkeleton getTileInHand() {
        return tileInHand;
    }

    /**
     * @return {@code true} if the tile in hand has been picked from reserved tiles, {@code false} otherwise
     */
    public boolean isTileInHandFromReserved() {
        return tileInHandFromReserved;
    }

    /**
     * @param tileToHold tile to be held by the player
     * @throws AlreadyHaveTileInHandException if already have tile in hand
     * @throws TileCanNotDisappearException if {@code tileToHold == null}: it would make the tile disappear from
     * player's hand
     */
    public void setTileInHand(TileSkeleton tileToHold) throws AlreadyHaveTileInHandException, TileCanNotDisappearException {
        if (tileToHold == null) {
            throw new TileCanNotDisappearException("You have a tile in hand: it can not disappear.");
        }
        if (getTileInHand() != null) {
            throw new AlreadyHaveTileInHandException();
        }
        tileInHand = tileToHold;
    }

    /**
     * Clear the hand from any tile. Requires to have put the tile in hand somewhere else before.
     */
    private void removeTileFromHand() {
        tileInHand = null;
        tileInHandFromReserved = false;
    }

    /**
     * Set the specified tile object as a tile lost during the flight.
     * It counts at the end as negative points in the leaderboard.
     * @implNote This method does not modify {@code lostTile} in any way.
     * e.g. the caller may want to remove the coordinates associated internally to the tile.
     * @param lostTile the tile lost during flight.
     */
    public void setLostTile(TileSkeleton lostTile) {
        lostTiles.add(lostTile);
    }

    /**
     * @return the list of discarded tiles
     */
    public List<TileSkeleton> getLostTiles() {
        return lostTiles;
    }

    /**
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
     * @return the player's shipboard
     */
    public ShipBoard getShipBoard() {
        return shipBoard;
    }

    /**
     * Assigns the shipboard to the player
     * @param shipBoard this player's shipboard
     * @implSpec the shipboard main cabin color must be equals to this player color
     */
    public void setShipBoard(ShipBoard shipBoard) {
        this.shipBoard = shipBoard;
    }

    /**
     * Returns the player's color. It's exactly the color of his main cabin.
     *
     * @return the player's color.
     */
    public MainCabinTile.Color getColor() {
        return color;
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
     * Sets the current position of the player.
     *
     * @param position the new position to set
     */
    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * Returns the current position of the player.
     *
     * @return the current position, or {@code null} if this player has never been assigned to a position.
     */
    public Integer getPosition() {
        return position;
    }

    /**
     * Returns the <i>opposite</i> of the current position of the player for sorting purposes:
     * ignores if the player has never been assigned to a position (or if it has not one anymore),
     * and in that case suppose the order privilege goes to those players who have a position.
     * <p>
     * <b>NOTE</b>: this is the opposite of the position, to get first the one with the most advanced position.
     *
     * @return the current order of the player.
     */
    public int getOrder() {
        if (position == null) return Integer.MAX_VALUE;
        else return -position;
    }

    /**
     * Returns the id of the held cardgroup
     *
     * @return the id of the cardgroup held by the player,
     * or {@code null} if the player is not keeping any cardgroup in hand
     */
    public Integer getCardGroupInHand() {
        return cardGroupInHand;
    }

    /**
     * Sets the id of the held cardgroup
     * @throws TooManyItemsInHandException if the player it is already holding a cardgroup or a tile in hand
     */
    public void setCardGroupInHand(int id) throws TooManyItemsInHandException {
        if (getTileInHand() != null) {
            throw new AlreadyHaveTileInHandException();
        }
        if (getCardGroupInHand() != null) {
            throw new TooManyItemsInHandException("You are already holding a group of cards.");
        }
        cardGroupInHand = id;
    }

    /**
     * Clears the id of the held cardgroup
     */
    public void clearCardGroupInHand() {
        cardGroupInHand = null;
    }

    /**
     * Randomly picks a tile from the covered tiles pile. Removes it from the pile and assigns it to the player.
     * @throws DrawTileException if there are no tiles from the pile
     * @throws TooManyItemsInHandException if this player already have a tile or group of cards in hand
     */
    public void drawTile(GameData gameData) throws DrawTileException, TooManyItemsInHandException {
        if (gameData.getCoveredTiles().isEmpty()) {
            throw new DrawTileException("There are no covered tiles available.");
        }
        if (getCardGroupInHand() != null) {
            throw new TooManyItemsInHandException("You are already holding a group of cards.");
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

    /**
     * Discards the tile currently held in hand by the player and adds it back to the list
     * of uncovered tiles in {@link GameData}.
     * Reserved tiles cannot be discarded.
     *
     * @param gameData the current game data containing the uncovered tiles pool
     * @throws NoTileInHandException if the player does not have a tile in hand
     * @throws ReservedTileException if the tile in hand was taken from the reserved tiles of the player
     */
    public void discardTile(GameData gameData) throws NoTileInHandException, ReservedTileException {
        TileSkeleton tileInHand = getTileInHand();
        if (tileInHand == null){
            throw new NoTileInHandException();
        }
        if (tileInHandFromReserved) {
            throw new ReservedTileException("It's not possible to discard a reserved tile.");
        }

        gameData.getUncoveredTiles().add(tileInHand);
        removeTileFromHand();
    }

    /**
     * Places the tile currently in hand onto the ship board at the specified coordinates
     * and with the specified rotation.
     * The method ensures the tile is correctly rotated and placed; once placed, it is removed from the player's hand.
     *
     * @param coordinates the target coordinates where the tile should be placed
     * @param rotation the rotation to apply before placing the tile
     * @throws NoTileInHandException If there is no tile in hand.
     * @throws NoShipboardException If the player does not have a ship board.
     * @throws AlreadyEndedAssemblyException If this shipboard has already been consolidated as assembled.
     * @throws OutOfBuildingAreaException If the coordinates are outside the valid building area.
     * @throws TileAlreadyPresentException If there is already a tile at the specified coordinates.
     * @throws FixedTileException If the provided tile has already been placed.
     * @throws TileWithoutNeighborException If the provided coordinates are not adjacent to an already placed tile
     * coordinates.
     */
    public void placeTile(Coordinates coordinates, Rotation rotation) throws NoTileInHandException, NoShipboardException,
            FixedTileException, TileAlreadyPresentException, TileWithoutNeighborException, OutOfBuildingAreaException,
            AlreadyEndedAssemblyException {
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

    /**
     * Picks a tile with the given ID from the uncovered or reserved tiles and sets it as the current tile in hand.
     * This operation is only allowed if the player is not already holding a tile or a group of cards.
     *
     * @param gameData the current game data containing the available tiles
     * @param id the ID of the tile to pick
     * @throws TooManyItemsInHandException if the player is already holding a tile or a card group
     * @throws ThatTileIdDoesNotExistsException if no tile with the given ID exists in any valid pool
     */
    public void pickTile(GameData gameData, Integer id) throws TooManyItemsInHandException, ThatTileIdDoesNotExistsException {
        if (getTileInHand() != null) {
            throw new AlreadyHaveTileInHandException();
        }
        if (getCardGroupInHand() != null) {
            throw new TooManyItemsInHandException("You are already holding a group of cards.");
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
                    tileInHandFromReserved = true;
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

    /**
     * End shipboard assembly and setup player at {@code startingPosition} route place.
     * Handles reserved tile in hand.
     * @param startingPosition the position from which the player starts the flight
     * @throws NoShipboardException if the player has no shipboard
     * @throws AlreadyEndedAssemblyException if the assembly phase has already been called for this player
     * @throws TooManyItemsInHandException if the player has something in hand (non-reserved tile or cards group)
     */
    public void endAssembly(int startingPosition) throws TooManyItemsInHandException, NoShipboardException,
            AlreadyEndedAssemblyException {
        if (cardGroupInHand != null || tileInHand != null) {
            throw new TooManyItemsInHandException("You can not end assembly with items in hand.");
        }
        forceEndAssembly(startingPosition);
    }

    /**
     * Force end assembly (e.g. no more time), without {@link TooManyItemsInHandException}
     * @see #endAssembly(int)
     */
    public void forceEndAssembly(int startingPosition) throws NoShipboardException, AlreadyEndedAssemblyException {
        ShipBoard shipBoard = getShipBoard();
        if (shipBoard == null) {
            throw new NoShipboardException();
        }

        // if tile in hand is reserved: add to the list of lost tiles
        if ((tileInHand != null) && tileInHandFromReserved) {
            setLostTile(tileInHand);
        }
        removeTileFromHand();
        // all the reserved tiles in lost tiles
        for (TileSkeleton reservedTile : reservedTiles) {
            setLostTile(reservedTile);
        }
        reservedTiles.clear();
        // clears card group in hand reference
        clearCardGroupInHand();

        // actually call the end of ship assembly
        setPosition(startingPosition);
        shipBoard.endAssembly();
    }

    @Override
    public String toString() {
        return getUsername();
    }

    /**
     * Get an ANSI-friendly colored username of this player.
     * Color is this player's color, if present. If this player has no color, the color is default.
     * @return The colored username.
     */
    public String toColoredString() {
        return toColoredString(getUsername());
    }

    /**
     * Get the specified string as ANSI-friendly colored string.
     * Color is this player's color, if present. If this player has no color, the color is default.
     * @param toShow The string to "paint" with the player's color.
     * @return The colored string {@code toShow}.
     */
    public String toColoredString(String toShow) {
        MainCabinTile.Color color = getColor();
        StringBuilder name = new StringBuilder();
        if (color != null) {
            name.append(color.toANSIColor(false));
        }
        name.append(toShow);
        if (color != null) {
            name.append(ANSI.RESET);
        }
        return name.toString();
    }

    /**
     * Get an ANSI-friendly colored username of this player.
     * Colors include both prefix and suffix.
     * Color is this player's color, if present. If this player has no color, the color is default.
     * @param prefix A string to show before the name, but with the same color
     * @param suffix A string to show after the name, but with the same color
     * @return The colored username.
     */
    public String toColoredString(String prefix, String suffix) {
        return toColoredString(prefix + getUsername() + suffix);
    }

    /**
     * Returns whether the flight has officially ended for this player.
     *
     * @return {@code true} if the flight has ended; {@code false} otherwise
     */
    public boolean isEndedFlight() {
        return endedFlight;
    }

    /**
     * Attempts to end the flight for this player.
     * If a {@code saveFromEndFlight} predicate is defined and returns {@code true},
     * the end is prevented and the predicate is cleared.
     * Otherwise: the flight is ended, the end request is cleared, and any related data (e.g. position) is reset.
     */
    public void endFlight() {
        if (saveFromEndFlight != null && saveFromEndFlight.test(this)) {
            // player has been saved from ending the flight
            saveFromEndFlight = null;
            return;
        }
        endedFlight = true;
        requestedEndFlight = false;
        saveFromEndFlight = null;
        position = null;
    }

    /**
     * Returns whether the player has requested to end the flight.
     *
     * @return {@code true} if the player has requested the end of flight; {@code false} otherwise
     */
    public boolean hasRequestedEndFlight() {
        return requestedEndFlight;
    }

    /**
     * Requests to end the flight for this player without conditions.
     */
    public void requestEndFlight() {
        requestedEndFlight = true;
    }

    /**
     * Requests to end the flight for this player, but allows for conditional cancellation.
     * If the provided {@code saveFromEndFlight} predicate returns {@code true} when {@link #endFlight()}
     * is called, the end of flight will be aborted.
     *
     * @param saveFromEndFlight a <strong>serializable</strong> predicate to be checked upon attempting to end the flight.
     *                          If it evaluates to {@code true}, the flight will not be ended.
     */
    public void requestEndFlight(KeepPlayerFlyingPredicate saveFromEndFlight) {
        requestedEndFlight = true;
        this.saveFromEndFlight = saveFromEndFlight;
    }

}