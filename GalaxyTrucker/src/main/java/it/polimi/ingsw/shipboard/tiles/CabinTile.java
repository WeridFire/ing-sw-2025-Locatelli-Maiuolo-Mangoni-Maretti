package it.polimi.ingsw.shipboard.tiles;

import it.polimi.ingsw.shipboard.LoadableType;
import it.polimi.ingsw.shipboard.SideType;
import it.polimi.ingsw.shipboard.tiles.exceptions.AlreadyInitializedCabinException;
import it.polimi.ingsw.shipboard.tiles.exceptions.TooMuchLoadException;
import it.polimi.ingsw.shipboard.tiles.exceptions.UnsupportedLoadableItemException;
import it.polimi.ingsw.shipboard.visitors.TileVisitor;

import java.util.Set;

public class CabinTile extends ContainerTile {

    private LoadableType loadedCrew = null;

    /**
     * Constructs a CabinTile with specified sides and maximum allowed crew.
     *
     * @param sides An array of sides defining the structure of the tile.
     */
    protected CabinTile(SideType[] sides, Set<LoadableType> maxAllowedCrew) {
        super(sides, maxAllowedCrew, Set.of(LoadableType.HUMAN), 2);
        setCLISymbol("cb");
    }

    /**
     * Constructs a standard CabinTile with specified sides.
     * It's initialized as to load only humans, but it's possible to call
     * {@link #setAllowedItems(Set)} to allow the loading of aliens.
     *
     * @param sides An array of sides defining the structure of the tile.
     */
    public CabinTile(SideType[] sides) {
        this(sides, LoadableType.CREW_SET);
    }

    @Override
    public String getName() {
        return "Cabin " + (getCapacity() - getCapacityLeft()) + "/" + getCapacity();
    }

    @Override
    public void accept(TileVisitor visitor) {
        visitor.visitCabin(this);
    }

    /**
     * Fills the cabin with the specified type of loadable item.
     * This method initializes the cabin with a specific {@link LoadableType} and fills it based on the cabin capacity.
     *
     * @param crew The type of crew to fill the cabin with.
     *             Can be {@link LoadableType#HUMAN}, {@link LoadableType#PURPLE_ALIEN}
     *             or {@link LoadableType#BROWN_ALIEN}.
     * @throws AlreadyInitializedCabinException If the cabin has already been initialized with a loadable type.
     * @throws UnsupportedLoadableItemException If the specified loadable type is not supported for this cabin.
     * @throws NullPointerException If {@code crew} is {@code null}.
     */
    public void fillWith(LoadableType crew) throws AlreadyInitializedCabinException, UnsupportedLoadableItemException {
        if (crew == null) {
            throw new NullPointerException("crew to add cannot be null");
        }
        if (loadedCrew != null) {
            throw new AlreadyInitializedCabinException();
        }
        // calculate how many components of the crew are needed to fill the cabin
        int quantity = getCapacityLeft() / crew.getRequiredCapacity();
        // load the crew
        try {
            loadItems(crew, quantity);
        } catch (TooMuchLoadException e) {
            throw new RuntimeException(e);  // should never happen -> runtime error
        }
        // set loaded crew type
        loadedCrew = crew;
    }

    public LoadableType getLoadedCrew() {
        return loadedCrew;
    }

}
