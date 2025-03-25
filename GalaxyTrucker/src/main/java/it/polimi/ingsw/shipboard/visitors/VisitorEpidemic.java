package src.main.java.it.polimi.ingsw.shipboard.visitors;

import src.main.java.it.polimi.ingsw.shipboard.LoadableType;
import src.main.java.it.polimi.ingsw.shipboard.SideType;
import src.main.java.it.polimi.ingsw.shipboard.tiles.*;
import src.main.java.it.polimi.ingsw.shipboard.tiles.exceptions.NotEnoughItemsException;
import src.main.java.it.polimi.ingsw.shipboard.tiles.exceptions.NotFixedTileException;
import src.main.java.it.polimi.ingsw.shipboard.tiles.exceptions.UnsupportedLoadableItemException;
import src.main.java.it.polimi.ingsw.util.Coordinates;

import java.util.*;

public class VisitorEpidemic implements TileVisitor {

	/**
	 * A list of the non-empty cabins in the shipboard.
	 */
	private final List<Coordinates> nonEmptyCabins;

	/**
	 * Creates a visitor that handles the application of the epidemic card effect.
	 */
	public VisitorEpidemic() {
		nonEmptyCabins = new ArrayList<>();
	}

	@Override
	public void visitStructural(StructuralTile tile) { }

	@Override
	public void visitLifeSupportSystem(LifeSupportSystemTile tile) { }

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
	public void visitMainCabin(CabinTile tile) {
		visitCabin(tile);
	}

	@Override
	public void visitCabin(CabinTile tile) {
		if(!tile.getLoadedItems().isEmpty()){
			try {
				nonEmptyCabins.add(tile.getCoordinates());
			} catch (NotFixedTileException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Applies the epidemic effect to all the cabins that are occupied and nearby other occupied cabins.
	 * The method accepts a hashmap of the shipboard map, but never edits it.
	 * It removes crew from the cabins based on the rule of the game. It will not update itself after the execution,
	 * so the whole visitor should be recalculated at the end.
	 * @param shipBoard as coordinate-tile entries in the shipboard
	 */
	public void applyEpidemicEffect(Map<Coordinates, TileSkeleton<SideType>> shipBoard){
		Set<Coordinates> result = new HashSet<>();
		for (Coordinates c : nonEmptyCabins) {
			Set<Coordinates> neighbors = c.getNeighbors();
			for (Coordinates neighbor : neighbors) {
				if (neighbor != null && nonEmptyCabins.contains(neighbor)) {
					result.add(c);
					break;
				}
			}
		}
		for(Coordinates c : result){
			CabinTile targetCabin = ((CabinTile) shipBoard.get(c));
			try{
				if(targetCabin.getLoadedItems().contains(LoadableType.HUMAN)){
					targetCabin.removeItems(LoadableType.HUMAN, 1);
				}else if(targetCabin.getLoadedItems().contains(LoadableType.PURPLE_ALIEN)){
					targetCabin.removeItems(LoadableType.PURPLE_ALIEN, 1);
				}else{
					targetCabin.removeItems(LoadableType.BROWN_ALIEN, 1);
				}
			}catch(UnsupportedLoadableItemException | NotEnoughItemsException e){
				throw new RuntimeException(e);
			}
		}
	}
}
