package it.polimi.ingsw.playerInput.PIRs;

import it.polimi.ingsw.enums.AnchorPoint;
import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.playerInput.PIRType;
import it.polimi.ingsw.playerInput.exceptions.TileNotAvailableException;
import it.polimi.ingsw.playerInput.exceptions.WrongPlayerTurnException;
import it.polimi.ingsw.shipboard.LoadableType;
import it.polimi.ingsw.shipboard.tiles.ContainerTile;
import it.polimi.ingsw.shipboard.tiles.exceptions.NotEnoughItemsException;
import it.polimi.ingsw.shipboard.tiles.exceptions.UnsupportedLoadableItemException;
import it.polimi.ingsw.util.Coordinates;
import it.polimi.ingsw.view.cli.ANSI;
import it.polimi.ingsw.view.cli.CLIFrame;
import it.polimi.ingsw.view.cli.CLIScreen;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PIRRearrangeLoadables extends PIR{

    private int targetAmount;
    private int amountToRemove;
    private final Set<LoadableType> allowedCargo;
    private int[] quantities = new int[4];

    public PIRRearrangeLoadables(Player currentPlayer, int cooldown, Set<LoadableType> allowedCargo) {
        super(currentPlayer, cooldown, PIRType.REARRANGE_CARGO);
        this.allowedCargo = allowedCargo;

        this.amountToRemove = currentPlayer
                .getShipBoard()
                .getVisitorCalculateCargoInfo()
                .getInfoAllContainers().countAll(allowedCargo);
        this.targetAmount = 0;
    }

    private Map<Coordinates, ContainerTile> getContainerTiles() {
        return currentPlayer.getShipBoard()
                .getVisitorCalculateCargoInfo()
                .getInfoAllContainers()
                .getLocationsWithLoadedItems(allowedCargo, 1);
    }


    @Override
    public Set<Coordinates> getHighlightMask() {
        return getContainerTiles()
                .keySet();
    }


    public int getCargoAmount(){
        return currentPlayer.getShipBoard()
                .getVisitorCalculateCargoInfo()
                .getInfoAllContainers()
                .countAll(allowedCargo);
    }

    @Override
    public void run() throws InterruptedException {
        synchronized (lock){
            lock.wait(getCooldown()* 1000L);
            if(getCargoAmount() > targetAmount){
                currentPlayer.getShipBoard().loseBestGoods(getAmountToRemove());
            }
        }
    }

    @Override
    void endTurn() {
        synchronized (lock){
            lock.notifyAll();
        }
    }

    /**
     * Function used to remove loadables from the player's shipboard, storing them in an array, based on the player request. The function will
     * check that the current turn and game state allow for this.
     * @param player The player that requested the action
     * @param cargoToRemove The list of tiles and the cargo to remove
     * @throws WrongPlayerTurnException The player that asked for the action is not the one the turn is for.
     * @throws TileNotAvailableException The tile requested is not supported for this operation.
     * @throws NotEnoughItemsException The tile requested does not have enough items.
     * @throws UnsupportedLoadableItemException The tile requested does not support the requested loadable.
     */
    @Override
    public void rearrangeLoadables(Player player, Map<Coordinates, List<LoadableType>> cargoToRemove) throws WrongPlayerTurnException, TileNotAvailableException, UnsupportedLoadableItemException, NotEnoughItemsException {
        checkForTurn(player);
        for(Coordinates c : cargoToRemove.keySet()){
            checkForTileMask(c);
        }
        for(Map.Entry<Coordinates, List<LoadableType>> entry : cargoToRemove.entrySet()){
            if(!allowedCargo.containsAll(entry.getValue())){
                throw new UnsupportedLoadableItemException(new HashSet<>(entry.getValue()), allowedCargo);
            }
            ContainerTile containerTile = currentPlayer.getShipBoard()
                    .getVisitorCalculateCargoInfo()
                    .getInfoAllContainers()
                    .getLocations()
                    .get(entry.getKey());
            if(containerTile == null){
                //shouldn't really happen tbh
                throw new TileNotAvailableException(entry.getKey(), getPIRType());
            }
            for(LoadableType loadable : entry.getValue()){
                if(loadable == LoadableType.RED_GOODS){
                    quantities[0] += 1;
                }
                else if(loadable == LoadableType.YELLOW_GOODS){
                    quantities[1] += 1;
                }
                else if(loadable == LoadableType.GREEN_GOODS){
                    quantities[2] += 1;
                }
                else if(loadable == LoadableType.BLUE_GOODS){
                    quantities[3] += 1;
                }
                containerTile.removeItems(loadable, 1);
            }
        }
        endTurn();
    }

    public int getAmountToRemove() {
        return amountToRemove;
    }

    public Set<LoadableType> getAllowedCargo() {
        return allowedCargo;
    }

    public int[] getQuantities()
    {
        return quantities.clone();
    }

    //TODO: this down here
    @Override
    public CLIFrame getCLIRepresentation() {

        CLIFrame frame = new CLIFrame(ANSI.BACKGROUND_RED + ANSI.WHITE + " REMOVE CARGO FROM CONTAINERS " + ANSI.RESET)
                .merge(new CLIFrame(""), Direction.SOUTH);

        frame = frame.merge(
                new CLIFrame(" Remove any of these Cargo Types: "),
                Direction.SOUTH, 1
        );

        for (LoadableType type : getAllowedCargo()) {
            frame = frame.merge(
                    new CLIFrame( "  - " + type.getUnicodeColoredString() + "[" + type.toString() +"]"),
                    Direction.SOUTH, 0
            );
        }

        frame = frame.merge(new CLIFrame(""), Direction.SOUTH, 1);
        frame = frame.merge(
                new CLIFrame(ANSI.CYAN + " Containers with Removable Cargo: " + ANSI.RESET),
                Direction.SOUTH, 0
        );

        Map<Coordinates, ContainerTile> containers = getContainerTiles();

        if (containers.isEmpty()) {
            frame = frame.merge(
                    new CLIFrame(ANSI.RED + " No containers currently match the allowed cargo for removal." + ANSI.RESET),
                    Direction.SOUTH, 0
            );
        } else {
            for (Map.Entry<Coordinates, ContainerTile> entry : containers.entrySet()) {
                Coordinates coords = entry.getKey();
                ContainerTile tile = entry.getValue();

                String containerInfo = String.format(" (%d, %d): ", coords.getColumn(), coords.getRow()) + tile.getName();
                frame = frame.merge(
                        new CLIFrame(containerInfo),
                        Direction.SOUTH, 0
                );
            }
        }

        frame = frame.merge(new CLIFrame(""), Direction.SOUTH, 2);
        frame = frame.merge(
                new CLIFrame(ANSI.GREEN + " Commands:" + ANSI.RESET),
                Direction.SOUTH, 0
        );
        frame = frame.merge(
                new CLIFrame(" >remove (x, y) <LoadableType> <amount>"),
                Direction.SOUTH, 0
        );
        frame = frame.merge(
                new CLIFrame(" >confirm"),
                Direction.SOUTH, 0
        );

        frame = frame.merge(new CLIFrame(""), Direction.SOUTH, 0);
        frame = frame.merge(
                new CLIFrame(ANSI.WHITE + "You have " + ANSI.YELLOW + getCooldown() + " seconds" + ANSI.RESET + " to remove the requested cargo."),
                Direction.SOUTH, 0
        );

        int containerRows = Math.max(frame.getRows() + 2, 24);
        int containerColumns = 100;

        CLIFrame screenFrame = CLIScreen.getScreenFrame(containerRows, containerColumns, ANSI.BACKGROUND_BLACK, ANSI.WHITE);

        return screenFrame.merge(frame, AnchorPoint.CENTER, AnchorPoint.CENTER);
    }

}
