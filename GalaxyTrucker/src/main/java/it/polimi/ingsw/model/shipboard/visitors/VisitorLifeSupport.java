package it.polimi.ingsw.model.shipboard.visitors;

import it.polimi.ingsw.model.shipboard.LoadableType;
import it.polimi.ingsw.model.shipboard.tiles.*;
import it.polimi.ingsw.model.shipboard.tiles.exceptions.NotFixedTileException;
import it.polimi.ingsw.model.shipboard.tiles.exceptions.UnsupportedLoadableItemException;
import it.polimi.ingsw.util.Coordinates;

import java.util.*;

public class VisitorLifeSupport implements TileVisitor {

	private final List<LifeSupportSystemTile> lifeSupportSystems;
	private final List<CabinTile> cabinTiles;

	/**
	 * Instances a visitor to update the life supports of cabin tiles.
	 */
	public VisitorLifeSupport() {
		lifeSupportSystems = new ArrayList<>();
		cabinTiles = new ArrayList<>();
	}

	@Override
	public void visitStructural(StructuralTile tile) { }

	@Override
	public void visitCargoHold(CargoHoldTile tile) {}

	@Override
	public void visitBatteryComponent(BatteryComponentTile tile) { }

	@Override
	public void visitCannon(CannonTile tile) { }

	@Override
	public void visitEngine(EngineTile tile) { }

	@Override
	public void visitShieldGenerator(ShieldGeneratorTile tile) { }

	@Override
	public void visitMainCabin(CabinTile tile) { }

	@Override
	public void visitLifeSupportSystem(LifeSupportSystemTile tile) {
		lifeSupportSystems.add(tile);
	}

	@Override
	public void visitCabin(CabinTile tile) {
		cabinTiles.add(tile);
	}

	/**
	 * Iterates through each life support present on the shipboard. Gets the adiacent cabins around all the life
	 * supports, and for each cabin updates the allowed items to also allow the alien type supported.
	 */
	public void updateLifeSupportSystems(){
		HashMap<CabinTile, Set<LoadableType>> supportedLife = new HashMap<>();
		// 1. populate initial supported life as basic
		for (CabinTile cabinTile : cabinTiles) {
			supportedLife.put(cabinTile, new HashSet<>(CabinTile.BASIC_ALLOWED_ITEMS));
		}
		// 2. calculate additional supported life
		for(LifeSupportSystemTile lifeSupport : lifeSupportSystems){
			try {
				for(Coordinates neighbour : lifeSupport.getCoordinates().getNeighbors()){
					cabinTiles.stream()
							.filter((cabin) -> {
								try {
									return cabin.getCoordinates().equals(neighbour);
								} catch (NotFixedTileException e) {
									throw new RuntimeException(e);
								}
							})
							.forEach((cabin) -> {
								supportedLife.get(cabin).add(lifeSupport.getProvidedLifeSupport());
							});
				}
			} catch (NotFixedTileException e) {
				throw new RuntimeException(e);
			}
		}
		// 3. apply new supported life for cabins
		for (CabinTile cabinTile : cabinTiles) {
            try {
                cabinTile.setAllowedItems(supportedLife.get(cabinTile));
            } catch (UnsupportedLoadableItemException e) {
                throw new RuntimeException(e);  // should never happen
            }
        }
	}
}
