package it.polimi.ingsw.playerInput;

import it.polimi.ingsw.cards.projectile.Projectile;
import it.polimi.ingsw.enums.PowerType;
import it.polimi.ingsw.enums.ProtectionType;
import it.polimi.ingsw.enums.Rotation;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.playerInput.PIRs.*;
import it.polimi.ingsw.shipboard.LoadableType;
import it.polimi.ingsw.shipboard.integrity.IShipIntegrityListener;
import it.polimi.ingsw.shipboard.integrity.IntegrityProblem;
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
		Set<Coordinates> activatedTiles = game.getPIRHandler().setAndRunTurn(inputRequest);

        // phase 2: ask batteries removal for desired activation
		int batteriesToRemove = activatedTiles.size();
		if(batteriesToRemove > 0){
			PIRRemoveLoadables pirRemoveLoadables = new PIRRemoveLoadables(player, 30, Set.of(LoadableType.BATTERY), batteriesToRemove);
			game.getPIRHandler().setAndRunTurn(
					pirRemoveLoadables
			);
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
			PIRYesNoChoice choiceReq = new PIRYesNoChoice(player, 30, message, false);

			boolean choice = game.getPIRHandler().setAndRunTurn(choiceReq);
			if(!choice){
				return false;
			}
			PIRRemoveLoadables pirRemoveLoadables = new PIRRemoveLoadables(player, 30, Set.of(LoadableType.BATTERY), 1);
			game.getPIRHandler().setAndRunTurn(pirRemoveLoadables);

			return true;

		}else if(projectile.isFireDefendable()){
			ProtectionType defendingCannon = player.getShipBoard().getCannonProtection(projectile.getDirection(), projectile.getCoord());
			if(defendingCannon == ProtectionType.SINGLE_CANNON){
				//Player defends automatically thanks to his single cannon
				return true;

			} else if(defendingCannon == ProtectionType.DOUBLE_CANNON) {

				if(player.getShipBoard()
						.getVisitorCalculateCargoInfo()
						.getBatteriesInfo()
						.count(LoadableType.BATTERY) == 0){
					//player doesn't have enough batteries.
					return false;
				}

				//Asking player to activate double cannon
				String message = "You are being hit from direction " + projectile.getDirection().toString() + ". You can defend yourself " +
						"with a double cannon. Do you want to activate it?";
				PIRYesNoChoice choiceReq = new PIRYesNoChoice(player, 30, message, false);
				boolean choice = game.getPIRHandler().setAndRunTurn(choiceReq);;
				if(!choice){
					return false;
				}
				PIRRemoveLoadables pirRemoveLoadables = new PIRRemoveLoadables(player, 30, Set.of(LoadableType.BATTERY), 1);
				game.getPIRHandler().setAndRunTurn(
						pirRemoveLoadables
				);

				return true;

			} else if (defendingCannon == ProtectionType.NONE) {
				return false;
			}
		}else{
			return false;
		}
        return false;
    }


	public class ShipIntegrityListener implements IShipIntegrityListener {
		private final Player player;
		private final PIRHandler pirHandler;

		public ShipIntegrityListener(Player player, PIRHandler pirHandler) {
			this.player = player;
			this.pirHandler = pirHandler;
		}

		@Override
		public void update(IntegrityProblem integrityProblem) {
			if (!integrityProblem.isProblem()) {
				return;
			}
			// TODO: actually perform Player Input Requests to manage correctly this integrity problem
		}
	}
}
