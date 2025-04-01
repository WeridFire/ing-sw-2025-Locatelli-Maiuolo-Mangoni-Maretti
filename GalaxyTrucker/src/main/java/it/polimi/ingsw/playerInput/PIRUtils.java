package it.polimi.ingsw.playerInput;

import it.polimi.ingsw.cards.projectile.Projectile;
import it.polimi.ingsw.enums.PowerType;
import it.polimi.ingsw.enums.Rotation;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.shipboard.LoadableType;
import it.polimi.ingsw.shipboard.visitors.VisitorCalculatePowers;
import it.polimi.ingsw.util.Coordinates;

import java.util.Map;
import java.util.Set;

public class PIRUtils {

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
		PIRActivateTiles inputRequest = new PIRActivateTiles(player, 30, powerType);
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
					new PIRRemoveLoadables(player, 30, Set.of(LoadableType.BATTERY), batteriesToRemove)
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

		if(projectile.isShieldDefendable()){
			if(player.getShipBoard()
					.getVisitorCalculateCargoInfo()
					.getBatteriesInfo()
					.count(LoadableType.BATTERY) == 0){
				//player doesn't have enough batteries.
				return false;
			}

			if(!player.getShipBoard()
					.getVisitorCalculateShieldedSides()
					.hasShieldFacing(projectile.getDirection()
					.getRotated(Rotation.OPPOSITE))){
				//player doesn't have a shield on that side
				return false;
			}

			String message = "You are being hit from direction " + projectile.getDirection().toString() + ". You can defend yourself " +
					"with a shield. Do you want to activate it?";
			PIRChoice choiceReq = new PIRChoice(player, 30, message, false);
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
					new PIRRemoveLoadables(player, 30, Set.of(LoadableType.BATTERY), 1)
			);

			try {
				game.getCurrentPlayerTurn().run();
			} catch (InterruptedException e) {
				e.printStackTrace();
				// TODO: manage InterruptedException
			}
			return true;

		}else if(projectile.isFireDefendable()){
			//TODO: check that projectile is defendable by a single cannon, so no request necessary and return true
			//TODO: check that projectile is defendable by a double cannon, if so proceed with request
			String message = "You are being hit from direction " + projectile.getDirection().toString() + ". You can defend yourself " +
					"with a double cannon. Do you want to activate it?";
			PIRChoice choiceReq = new PIRChoice(player, 30, message, false);
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
					new PIRRemoveLoadables(player, 30, Set.of(LoadableType.BATTERY), 1)
			);

			try {
				game.getCurrentPlayerTurn().run();
			} catch (InterruptedException e) {
				e.printStackTrace();
				// TODO: manage InterruptedException
			}
			return true;

		}else{
			return false;
		}
	}


}
