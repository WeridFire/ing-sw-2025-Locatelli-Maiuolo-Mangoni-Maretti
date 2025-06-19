package it.polimi.ingsw.integrity;

import it.polimi.ingsw.cards.projectile.Projectile;
import it.polimi.ingsw.enums.ProtectionType;
import it.polimi.ingsw.enums.Rotation;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.shipboard.LoadableType;
import it.polimi.ingsw.shipboard.ShipBoard;
import it.polimi.ingsw.shipboard.exceptions.NoTileFoundException;
import it.polimi.ingsw.shipboard.exceptions.OutOfBuildingAreaException;
import it.polimi.ingsw.task.customTasks.TaskRemoveLoadables;
import it.polimi.ingsw.task.customTasks.TaskYesNoChoice;
import it.polimi.ingsw.util.Coordinates;

import java.util.Set;
import java.util.function.BiConsumer;

public class PlayerProjectileDefendUtils {

	/**
	 * Executes the interaction for protecting the player from a side using shields
	 * <p>
	 * The activation process consists of three phases:
	 * <ol>
	 *     <li>Asking the player if they want to be protected on the side they are being hit on, if they have shields.</li>
	 *     <li>Requesting battery removal for that shield activation.</li>
	 * </ol>
	 * Note: it the projectile is not going to hit the ship, no request is done and the side is considered "defended".
	 *
	 * @param player The player whose shield tiles should be activated.
	 * @param projectile The projectile the player's ship is going to be hit with.
	 * @param game The game data context in which the interaction takes place.
	 * @return {@code true} if the side has been protected, {@code false} if the ship will be hit.
	 */
	public static void runPlayerProjectileDefendRequest(Player player, Projectile projectile, GameData game, BiConsumer<Player, Boolean> defendHit) {
		ShipBoard playerShip = player.getShipBoard();
		// check if ship is going to be hit. if not -> no need to defend
		Coordinates firstTilePlace = playerShip.getFirstTileLocation(projectile.getDirection(), projectile.getCoord());
		if (firstTilePlace == null) {
			defendHit.accept(player, true);
			return;
		}

		// process projectile from lowest-energy method to defend it to most expensive

		// 1. smooth side and bouncy projectile
		if (projectile.isBouncy()) {
			try {
				if (!playerShip.getTile(firstTilePlace).getSide(projectile.getDirection()).isConnector()) {
					defendHit.accept(player, true);
					return;
				}
			} catch (NoTileFoundException | OutOfBuildingAreaException e) {
				throw new RuntimeException(e);  // should never happen -> runtime exception
			}
		}

		// 2. cannons (can be single -> no need for battery usage) and fire-defensible
		if (projectile.isFireDefensible()) {
			ProtectionType defendingCannon = playerShip.getCannonProtection(projectile.getDirection(), projectile.getCoord());
			if(defendingCannon == ProtectionType.SINGLE_CANNON){
				//Player defends automatically thanks to his single cannon
				defendHit.accept(player, true);
				return;

			} else if(defendingCannon == ProtectionType.DOUBLE_CANNON) {

				if(playerShip
						.getVisitorCalculateCargoInfo()
						.getBatteriesInfo()
						.count(LoadableType.BATTERY) == 0){
					//player doesn't have enough batteries.
					defendHit.accept(player, false);
					return;
				}

				//Asking player to activate double cannon
				String message = "You are being hit from direction " + projectile.getDirection().toString() + ". You can defend yourself " +
						"with a double cannon. Do you want to activate it?";
				game.getTaskStorage().addTask(new TaskYesNoChoice(
						player.getUsername(),
						30, message,
						false,
						(p, activateToDefend) -> {
							if(TaskYesNoChoice.isChoiceYes(activateToDefend)){
								game.getTaskStorage().addTask(new TaskRemoveLoadables(
										p,
										30,
										Set.of(LoadableType.BATTERY),
										1,
										(p1) -> {
											defendHit.accept(p1, true);
										}));
							}
						}));
				return;
			}
		}

		// 3. shield and shield-defensible
		if (projectile.isShieldDefensible()) {
			if(playerShip
					.getVisitorCalculateCargoInfo()
					.getBatteriesInfo()
					.count(LoadableType.BATTERY) == 0){
				//player doesn't have enough batteries.
				defendHit.accept(player, false);
				return;
			}

			if(!playerShip
					.getVisitorCalculateShieldedSides()
					.hasShieldFacing(projectile.getDirection()
							.getRotated(Rotation.OPPOSITE))){
				//player doesn't have a shield on that side

				defendHit.accept(player, false);
				return;
			}

			String message = "You are being hit from direction " + projectile.getDirection().toString() + ". You can defend yourself " +
					"with a shield. Do you want to activate it?";
			game.getTaskStorage().addTask(new TaskYesNoChoice(
					player.getUsername(),
					30, message,
					false,
					(p, activateToDefend) -> {
						if(TaskYesNoChoice.isChoiceYes(activateToDefend)){
							game.getTaskStorage().addTask(new TaskRemoveLoadables(
									p,
									30,
									Set.of(LoadableType.BATTERY),
									1,
									(p1) -> {
										defendHit.accept(p1, true);
									}));
						}
					}));
			return;
		}


		defendHit.accept(player, false);
		return;
	}
}
