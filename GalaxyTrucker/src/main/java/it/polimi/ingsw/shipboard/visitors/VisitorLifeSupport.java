package it.polimi.ingsw.shipboard.visitors;

import it.polimi.ingsw.shipboard.LoadableType;
import it.polimi.ingsw.shipboard.tiles.*;
import it.polimi.ingsw.shipboard.tiles.exceptions.NotFixedTileException;
import it.polimi.ingsw.shipboard.tiles.exceptions.UnsupportedLoadableItemException;
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
								Set<LoadableType> allowed = cabin.getAllowedItems();
								allowed.add(lifeSupport.getProvidedLifeSupport());
								try {
									cabin.setAllowedItems(allowed);
								} catch (UnsupportedLoadableItemException e) {
									throw new RuntimeException(e);
								}
							});
				}
			} catch (NotFixedTileException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
