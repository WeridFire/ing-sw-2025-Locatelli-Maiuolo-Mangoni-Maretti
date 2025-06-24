package it.polimi.ingsw.model.shipboard.visitors;

import it.polimi.ingsw.model.shipboard.LoadableType;
import it.polimi.ingsw.model.shipboard.tiles.*;
import it.polimi.ingsw.model.shipboard.tiles.exceptions.NotEnoughItemsException;
import it.polimi.ingsw.model.shipboard.tiles.exceptions.UnsupportedLoadableItemException;
import it.polimi.ingsw.util.ContrabandCalculator;

import java.util.Map;
import java.util.PriorityQueue;

public class VisitorSmugglers implements TileVisitor {
    private final int maxRemovableItems;
    private int minimumContrabandValue;
    private final PriorityQueue<Map.Entry<ContainerTile, LoadableType>> contrabandTargets;
    private boolean alreadyRemoved;

    /**
     * Construct a new VisitorSmugglers.
     *
     * @param maxRemovableItems The maximum number of items to remove.
     * @throws IllegalArgumentException If {@code maxRemovableItems <= 0}.
     */
    public VisitorSmugglers(int maxRemovableItems) {
        if (maxRemovableItems <= 0) {
            throw new IllegalArgumentException("maxRemovableItems (provided: " + maxRemovableItems
                    + ") must be greater than 0");
        }

        this.maxRemovableItems = maxRemovableItems;
        minimumContrabandValue = 0;
        contrabandTargets = new PriorityQueue<>(maxRemovableItems + 1,
                (e1, e2)
                -> ContrabandCalculator.ascendingContrabandComparator.compare(e1.getValue(), e2.getValue())
        );
        alreadyRemoved = false;
    }

    @Override
    public void visitStructural(StructuralTile tile) { }
    @Override
    public void visitLifeSupportSystem(LifeSupportSystemTile tile) { }
    @Override
    public void visitCabin(CabinTile tile) { }
    @Override
    public void visitMainCabin(CabinTile tile) { }
    @Override
    public void visitCannon(CannonTile tile) { }
    @Override
    public void visitEngine(EngineTile tile) { }
    @Override
    public void visitShieldGenerator(ShieldGeneratorTile tile) { }

    @Override
    public void visitCargoHold(CargoHoldTile tile) {
        visitContainer(tile);
    }

    @Override
    public void visitBatteryComponent(BatteryComponentTile tile) {
        visitContainer(tile);
    }

    /**
     * Update the internal state of the visitor with info about the distribution of most valuable cargo.
     * @param tile The container tile to visit.
     */
    private void visitContainer(ContainerTile tile) {
        // no need to visit the tile if the minimum value is already the maximum possible
        if (minimumContrabandValue == ContrabandCalculator.maxCargoValue) {
            return;
        }

        // retrieve contraband cargo loaded onto this tile (in descending order of contraband value)
        PriorityQueue<LoadableType> cargo = tile.getContrabandMostValuableItems(maxRemovableItems, minimumContrabandValue);

        while (!cargo.isEmpty()) {
            LoadableType mostValuableCargo = cargo.poll();
            contrabandTargets.offer(Map.entry(tile, mostValuableCargo));

            // keep the size within quantity by removing the least valuable item (head)
            if (contrabandTargets.size() > maxRemovableItems) {
                contrabandTargets.poll();
                // update minimum contraband value to optimize next iteration
                minimumContrabandValue = ContrabandCalculator.getContrabandValue(contrabandTargets.peek().getValue());
                /* interrupt cargo parsing if this element is already "worse" (in cargo value)
                of the minimum removed, since cargo is sorted in descending order and
                no next value will ever be strictly better than the minimum (until next cargo in another tile)
                */
                if (ContrabandCalculator.getContrabandValue(mostValuableCargo) <= minimumContrabandValue) {
                    break;
                }
            }
        }
    }

    /**
     * Perform the removal of {@code quantity} cargo items among the most valuable visited.
     * @param quantity The quantity of items to remove.
     * @throws IllegalArgumentException If {@code quantity} > maximum number of items to removed,
     * info set while constructing this instance.
     * @throws IllegalStateException If this instance already removed items with this function.
     * To remove other items: create a new instance of this visitor!
     */
    public void removeMostValuableItems(int quantity) {
        if (alreadyRemoved) {
            throw new IllegalStateException("Attempt to remove most valuable tile items twice from one"
                    + " VisitorSmugglers: instantiate another one to do so.");
        }

        if (quantity > maxRemovableItems) {
            throw new IllegalArgumentException("Quantity must be less than or equal to " + maxRemovableItems
                + " as set in the constructor; " + quantity + " provided.");
        }

        for (int i = 0; i < quantity; i++) {
            Map.Entry<ContainerTile, LoadableType> entry = contrabandTargets.poll();
            if (entry == null) {
                break;  // if there are no more contraband items: smugglers stop to take them
            }
            ContainerTile tile = entry.getKey();
            LoadableType mostValuableCargo = entry.getValue();
            try {
                tile.removeItems(mostValuableCargo, 1);
            } catch (UnsupportedLoadableItemException | NotEnoughItemsException e) {
                throw new RuntimeException(e);  // should never happen -> runtime error
            }
        }

        alreadyRemoved = true;
    }
}
