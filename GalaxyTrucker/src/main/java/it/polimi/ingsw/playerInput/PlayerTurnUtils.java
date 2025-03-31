package src.main.java.it.polimi.ingsw.playerInput;

import src.main.java.it.polimi.ingsw.enums.PowerType;
import src.main.java.it.polimi.ingsw.game.GameData;
import src.main.java.it.polimi.ingsw.player.Player;
import src.main.java.it.polimi.ingsw.shipboard.LoadableType;
import src.main.java.it.polimi.ingsw.shipboard.exceptions.NoTileFoundException;
import src.main.java.it.polimi.ingsw.shipboard.exceptions.OutOfBuildingAreaException;
import src.main.java.it.polimi.ingsw.shipboard.visitors.VisitorCalculatePowers;
import src.main.java.it.polimi.ingsw.shipboard.visitors.VisitorCalculateShieldedSides;
import src.main.java.it.polimi.ingsw.util.Coordinates;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class PlayerTurnUtils {

	/**
	 * Executes the interaction for activating power-related tiles for a player.
	 * <p>
	 * The activation process consists of three phases:
	 * <ol>
	 *     <li>Requesting tile activation from the player.</li>
	 *     <li>Requesting battery removal for the activated tiles.</li>
	 *     <li>Calculating the total power generated after activation.</li>
	 * </ol>
	 *
	 * @param player The player whose power tiles should be activated.
	 * @param game The game data context in which the interaction takes place.
	 * @param powerType The type of power being activated. Can be {@link PowerType#FIRE} or {@link PowerType#THRUST}.
	 * @return The total power output after activation.
	 */
	public static float runPlayerPowerTilesActivationInteraction(Player player, GameData game, PowerType powerType) {

		VisitorCalculatePowers.CalculatorPowerInfo powerInfo = player.getShipBoard().getVisitorCalculatePowers().getInfoPower(powerType);
		if (powerInfo == null) {
			// TODO: throw error invalid power type -> shield or none (remove none?)
			return 0f;
		}

		// phase 1: ask activation
		game.setCurrentPlayerTurn(new PlayerActivateTilesRequest(player, 30, powerType));
		PlayerActivateTilesRequest inputRequest = (PlayerActivateTilesRequest) game.getCurrentPlayerTurn();
        try {
            game.getCurrentPlayerTurn().run();
        } catch (InterruptedException e) {
			// TODO: manage InterruptedException
        }

        // phase 2: ask batteries removal for desired activation
		Set<Coordinates> activatedTiles = inputRequest.getActivatedTiles();
		int batteriesToRemove = activatedTiles.size();
		if(batteriesToRemove > 0){
			game.setCurrentPlayerTurn(
					new PlayerRemoveLoadableRequest(player, 30, Set.of(LoadableType.BATTERY), batteriesToRemove)
			);
			try {
				game.getCurrentPlayerTurn().run();
			} catch (InterruptedException e) {
				// TODO: manage InterruptedException
			}
		}

		// phase 3: calculate total power and return it
		float activatedPower = (float) powerInfo.getLocationsToActivate().entrySet().stream()
				.filter(entry -> activatedTiles.contains(entry.getKey()))
				.mapToDouble(Map.Entry::getValue)
				.sum();
		float totalFirePower = powerInfo.getBasePower() + activatedPower;
		totalFirePower += powerInfo.getBonus(totalFirePower);
		// reset activated tile
		return totalFirePower;
	}

	/**
	 * Executes the interaction for activating shield-related tiles for a player.
	 * <p>
	 * The activation process consists of three phases:
	 * <ol>
	 *     <li>Requesting tile activation from the player.</li>
	 *     <li>Requesting battery removal for the activated tiles.</li>
	 *     <li>Calculating the total protected sides generated after activation.</li>
	 * </ol>
	 *
	 * @param player The player whose shield tiles should be activated.
	 * @param game The game data context in which the interaction takes place.
	 * @return The total sides protected after activation.
	 */
	public static List<Boolean> runPlayerShieldsActivationInteraction(Player player, GameData game) {
		// phase 1: ask activation
		game.setCurrentPlayerTurn(new PlayerActivateTilesRequest(player, 30, PowerType.SHIELD));
		PlayerActivateTilesRequest inputRequest = (PlayerActivateTilesRequest) game.getCurrentPlayerTurn();
		try {
			game.getCurrentPlayerTurn().run();
		} catch (InterruptedException e) {
			// TODO: manage InterruptedException
		}


		int batteriesToRemove = inputRequest.getActivatedTiles().size();
		if(batteriesToRemove > 0){
			game.setCurrentPlayerTurn(
					new PlayerRemoveLoadableRequest(player, 30, Set.of(LoadableType.BATTERY), batteriesToRemove)
			);
			try {
				game.getCurrentPlayerTurn().run();
			} catch (InterruptedException e) {
				// TODO: manage InterruptedException
			}
		}

		VisitorCalculateShieldedSides visitor = new VisitorCalculateShieldedSides();
		for(Coordinates c : inputRequest.getActivatedTiles()){
			try {
				player.getShipBoard().getTile(c).accept(visitor);
			} catch (OutOfBuildingAreaException | NoTileFoundException e) {
				e.printStackTrace();
			}
		}
		return List.of(visitor.getProtectedSides());
	}


}
