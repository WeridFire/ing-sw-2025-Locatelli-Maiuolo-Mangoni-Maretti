package src.main.java.it.polimi.ingsw.playerInput;

import src.main.java.it.polimi.ingsw.enums.PowerType;
import src.main.java.it.polimi.ingsw.game.GameData;
import src.main.java.it.polimi.ingsw.player.Player;
import src.main.java.it.polimi.ingsw.shipboard.LoadableType;
import src.main.java.it.polimi.ingsw.util.Coordinates;

import java.util.Map;
import java.util.Set;

public class PlayerTurnUtils {


	public static float runPlayerFireTilesActivationInteraction(Player player, GameData game){

		game.setCurrentPlayerTurn(new PlayerActivateTilesRequest(player, 30, PowerType.FIRE));
		game.getCurrentPlayerTurn().run();

		Set<Coordinates> activatedTiles = player.getShipBoard().getActivatedTiles();
		int batteriesToRemove = activatedTiles.size();
		if(batteriesToRemove > 0){

			game.setCurrentPlayerTurn(
					new PlayerRemoveLoadableRequest(player, 30, Set.of(LoadableType.BATTERY), batteriesToRemove)
			);
			game.getCurrentPlayerTurn().run();

		}
		double doubleFirePower = player.getShipBoard().getVisitorCalculateFirePower()
				.getDoubleCannons()
				.entrySet()
				.stream()
				.filter(entry -> activatedTiles.contains(entry.getKey()))
				.mapToDouble(Map.Entry::getValue)
				.sum();
		double baseFirePower = player.getShipBoard().getVisitorCalculateFirePower().getBaseFirePower();
		double totalFirePower = baseFirePower + doubleFirePower;
		if(totalFirePower > 0 && player.getShipBoard().getVisitorCalculateFirePower().hasBonus()){
			totalFirePower += 2;
		}
		player.getShipBoard().resetActivatedTiles();
		return (float) totalFirePower;
	}


}
