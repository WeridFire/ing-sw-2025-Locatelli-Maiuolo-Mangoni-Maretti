package it.polimi.ingsw.player;

import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.game.Game;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.player.exceptions.TooManyReservedTilesException;
import it.polimi.ingsw.shipboard.SideType;
import it.polimi.ingsw.shipboard.ShipBoard;
import it.polimi.ingsw.shipboard.tiles.TileSkeleton;
import it.polimi.ingsw.util.Coordinates;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Map;

public class Player implements Serializable {

    private final String username;
    private UUID connectionUUID;

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

    public void drawComponent(Game game){
        if(getTileInHand() != null){
            //throw errror
        }
        // TODO: actually draw component  // setTileInHand();
    }

    public void printCliShipboard()
    {
        int gridWidth = 7;
        int gridHeight = 5;

        char[][] grid = new char[gridHeight][gridWidth];
        Map<Coordinates, TileSkeleton<SideType>> tiles = this.getShipBoard().getTilesOnBoard();

        for (int i = 0; i < gridHeight; i++) {
            for (int j = 0; j < gridWidth; j++) {
                grid[i][j] = ' ';
            }
        }

        for (Map.Entry<Coordinates, TileSkeleton<SideType>> entry : tiles.entrySet()) {
            Coordinates coord = entry.getKey();
            TileSkeleton<SideType> tile = entry.getValue();

            // Calcola posizione nella griglia di caratteri
            int startx = (coord.getColumn() - 4) * 5;
            int starty = (coord.getRow() - 5) * 5;

            // Renderizza la tessera
            try{
                renderTile(grid, startx, starty, tile);
            }
            catch (ArrayIndexOutOfBoundsException e)
            {
                System.err.println("Errore rendering tile [" + coord.getColumn() + "," + coord.getRow() + "]");
            }
        }

        //Stampa la griglia
        for (int i = 0; i < gridHeight; i++) {
            for (int j = 0; j < gridWidth; j++) {
                System.out.print(grid[i][j]);
            }
            System.out.println();
        }
    }

    private void renderTile(char[][] grid, int startx, int starty, TileSkeleton<SideType> tile) {
        grid[startx][starty] = '┌';
        grid[startx+4][starty] = '┐';
        grid[startx][starty+4] = '└';
        grid[startx+4][starty+4] = '┘';
        renderNorthSide(grid, startx, starty, tile.getSide(Direction.NORTH));
        renderWestSide(grid, startx, starty, tile.getSide(Direction.WEST));
        renderEastSide(grid, startx, starty, tile.getSide(Direction.EAST));
        renderSouthSide(grid, startx, starty, tile.getSide(Direction.SOUTH));
        //TODO: aggiungere il carattere centrale
    }

    private void renderNorthSide(char[][] grid, int startx, int starty, SideType side) {
        switch(side){
            case SMOOTH:
                grid[startx+1][starty] = ' ';
                grid[startx+2][starty] = ' ';
                grid[startx+3][starty] = ' ';
                break;
            case SINGLE:
                grid[startx+1][starty] = ' ';
                grid[startx+2][starty] = '|';
                grid[startx+3][starty] = ' ';
                break;
            case DOUBLE:
                grid[startx+1][starty] = '|';
                grid[startx+2][starty] = ' ';
                grid[startx+3][starty] = '|';
                break;
            case UNIVERSAL:
                grid[startx+1][starty] = '|';
                grid[startx+2][starty] = '|';
                grid[startx+3][starty] = '|';
                break;
            case CANNON:
                grid[startx+1][starty] = ' ';
                grid[startx+2][starty] = '▴';
                grid[startx+3][starty] = ' ';
                break;
            case ENGINE:
                grid[startx+1][starty] = ' ';
                grid[startx+2][starty] = 'E';
                grid[startx+3][starty] = ' ';
                break;
        }
    }

    public void renderWestSide(char[][] grid, int startx, int starty, SideType side) {
        switch(side){
            case SMOOTH:
                grid[startx][starty+1] = ' ';
                grid[startx][starty+2] = ' ';
                grid[startx][starty+3] = ' ';
                break;
            case SINGLE:
                grid[startx][starty+1] = ' ';
                grid[startx][starty+2] = '-';
                grid[startx][starty+3] = ' ';
                break;
            case DOUBLE:
                grid[startx][starty+1] = '-';
                grid[startx][starty+2] = ' ';
                grid[startx][starty+3] = '-';
                break;
            case UNIVERSAL:
                grid[startx][starty+1] = '-';
                grid[startx][starty+2] = '-';
                grid[startx][starty+3] = '-';
                break;
            case CANNON:
                grid[startx][starty+1] = ' ';
                grid[startx][starty+2] = '◂';
                grid[startx][starty+3] = ' ';
                break;
            case ENGINE:
                grid[startx][starty+1] = ' ';
                grid[startx][starty+2] = 'E';
                grid[startx][starty+3] = ' ';
                break;
        }
    }

    public void renderEastSide(char[][] grid, int startx, int starty, SideType side) {
        switch(side){
            case SMOOTH:
                grid[startx+4][starty+1] = ' ';
                grid[startx+4][starty+2] = ' ';
                grid[startx+4][starty+3] = ' ';
                break;
            case SINGLE:
                grid[startx+4][starty+1] = ' ';
                grid[startx+4][starty+2] = '-';
                grid[startx+4][starty+3] = ' ';
                break;
            case DOUBLE:
                grid[startx+4][starty+1] = '-';
                grid[startx+4][starty+2] = ' ';
                grid[startx+4][starty+3] = '-';
                break;
            case UNIVERSAL:
                grid[startx+4][starty+1] = '-';
                grid[startx+4][starty+2] = '-';
                grid[startx+4][starty+3] = '-';
                break;
            case CANNON:
                grid[startx+4][starty+1] = ' ';
                grid[startx+4][starty+2] = '▸';
                grid[startx+4][starty+3] = ' ';
                break;
            case ENGINE:
                grid[startx+4][starty+1] = ' ';
                grid[startx+4][starty+2] = 'E';
                grid[startx+4][starty+3] = ' ';
                break;
        }

    }

    private void renderSouthSide(char[][] grid, int startx, int starty, SideType side){
        switch(side){
            case SMOOTH:
                grid[startx+1][starty+4] = ' ';
                grid[startx+2][starty+4] = ' ';
                grid[startx+3][starty+4] = ' ';
                break;
            case SINGLE:
                grid[startx+1][starty+4] = ' ';
                grid[startx+2][starty+4] = '|';
                grid[startx+3][starty+4] = ' ';
                break;
            case DOUBLE:
                grid[startx+1][starty+4] = '|';
                grid[startx+2][starty+4] = ' ';
                grid[startx+3][starty+4] = '|';
                break;
            case UNIVERSAL:
                grid[startx+1][starty+4] = '|';
                grid[startx+2][starty+4] = '|';
                grid[startx+3][starty+4] = '|';
                break;
            case CANNON:
                grid[startx+1][starty+4] = ' ';
                grid[startx+2][starty+4] = '▾';
                grid[startx+3][starty+4] = ' ';
                break;
            case ENGINE:
                grid[startx+1][starty+4] = ' ';
                grid[startx+2][starty+4] = 'E';
                grid[startx+3][starty+4] = ' ';
                break;
        }
    }

}