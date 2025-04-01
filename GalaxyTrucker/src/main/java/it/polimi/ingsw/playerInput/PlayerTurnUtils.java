package src.main.java.it.polimi.ingsw.playerInput;

import src.main.java.it.polimi.ingsw.cards.projectile.Projectile;
import src.main.java.it.polimi.ingsw.enums.Direction;
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
		PlayerActivateTilesRequest inputRequest = new PlayerActivateTilesRequest(player, 30, powerType);
		// phase 1: ask activation
		game.setCurrentPlayerTurn(inputRequest);

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
	 * Executes the interaction for protecting the player from a side using shields
	 * <p>
	 * The activation process consists of three phases:
	 * <ol>
	 *     <li>Asking the player if they want to be protected on the side they are being hit on, if they have shields.</li>
	 *     <li>Requesting battery removal for that shield activation.</li>
	 * </ol>
	 *
	 * @param player The player whose shield tiles should be activated.
	 * @param game The game data context in which the interaction takes place.
	 * @return If the sides has been protected or not.
	 */
	public static boolean runPlayerProjectileDefendRequest(Player player, Projectile projectile, GameData game) {

		if(!projectile.isShieldDefendable() && !projectile.isFireDefendable()){
			//Player can't defend that side.
			return false;
		}


		if(player.getShipBoard().getVisitorCalculateCargoInfo().getBatteriesInfo().count(LoadableType.BATTERY) == 0){
			//player doesn't have enough batteries.
			return false;
		}
		String message = "You are being hit from direction " + shieldDirection.toString() + ". You can defend yourself " +
				"with a shield. Do you want to activate it?";
		PlayerChoiceRequest choiceReq = new PlayerChoiceRequest(player, 30, message, false);
		game.setCurrentPlayerTurn(choiceReq);
		try {
			choiceReq.run();
		} catch (InterruptedException e) {
			//TODO: Manage interruptedException
			e.printStackTrace();
		}
		boolean choice = choiceReq.getChoice();
		if(!choice){
			return false;
		}

		game.setCurrentPlayerTurn(
				new PlayerRemoveLoadableRequest(player, 30, Set.of(LoadableType.BATTERY), 1)
		);
		try {
			game.getCurrentPlayerTurn().run();
		} catch (InterruptedException e) {
			e.printStackTrace();
			// TODO: manage InterruptedException
		}
		return true;
	}


}
