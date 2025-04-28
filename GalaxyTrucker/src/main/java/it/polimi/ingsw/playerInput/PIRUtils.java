package it.polimi.ingsw.playerInput;

import it.polimi.ingsw.cards.projectile.Projectile;
import it.polimi.ingsw.enums.PowerType;
import it.polimi.ingsw.enums.ProtectionType;
import it.polimi.ingsw.enums.Rotation;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.network.GameServer;
import it.polimi.ingsw.network.IClient;
import it.polimi.ingsw.network.messages.ClientUpdate;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.playerInput.PIRs.*;
import it.polimi.ingsw.shipboard.LoadableType;
import it.polimi.ingsw.shipboard.ShipBoard;
import it.polimi.ingsw.shipboard.TileCluster;
import it.polimi.ingsw.shipboard.integrity.IShipIntegrityListener;
import it.polimi.ingsw.shipboard.integrity.IntegrityProblem;
import it.polimi.ingsw.shipboard.tiles.TileSkeleton;
import it.polimi.ingsw.shipboard.tiles.exceptions.NotFixedTileException;
import it.polimi.ingsw.shipboard.visitors.VisitorCalculatePowers;
import it.polimi.ingsw.util.Coordinates;
import it.polimi.ingsw.view.cli.ANSI;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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


	public static class ShipIntegrityListener implements IShipIntegrityListener {
		private final Player player;
		private final PIRHandler pirHandler;

		public ShipIntegrityListener(Player player, PIRHandler pirHandler) {
			this.player = player;
			this.pirHandler = pirHandler;
		}

		private void manageIntegrityProblem(IntegrityProblem integrityProblem) {
			// TODO (to complete): actually perform Player Input Requests to manage correctly this integrity problem
			PIRDelay delayInfo;
			Set<Coordinates> maskTilesToRemove;
			ShipBoard playerShip = player.getShipBoard();

			// 1. notify about problems
			delayInfo = new PIRDelay(player, 3,
					"Unfortunately, your Ship has now some integrity problems...", null);
			pirHandler.setAndRunTurn(delayInfo, false);

			// 2. if presents, notify about all the clusters that must be removed
			maskTilesToRemove = integrityProblem.getClustersToRemove().stream()
					.flatMap(cluster -> cluster.getTiles().stream())
					.map(TileSkeleton::forceGetCoordinates)
					.collect(Collectors.toSet());
			if (!maskTilesToRemove.isEmpty()) {
				delayInfo = new PIRDelay(player, 5,
						"These tiles needs to be removed...",
						playerShip.getCLIRepresentation(maskTilesToRemove, ANSI.RED));
				pirHandler.setAndRunTurn(delayInfo, false);
			}
			// actually remove those
			for (Coordinates placeTileToRemove : maskTilesToRemove) {
				playerShip.forceRemoveTile(placeTileToRemove);
			}

			// TODO: problem: clusters to keep should NOT be any of the clusters to remove!
			// 3. if presents, notify about all the clusters competing to stay in the ship
			// if 0 -> end of flight
			// if >= 2 -> player need to chose which one to keep
			// else -> no problem
			List<TileCluster> clustersToKeep = integrityProblem.getClustersToKeep();
			if (clustersToKeep.isEmpty()) {
				delayInfo = new PIRDelay(player, 6,
						"Your ship has no valid cluster of tiles.\n" +
								"You need to end your flight...", null);
				pirHandler.setAndRunTurn(delayInfo, false);
			}
			else if (clustersToKeep.size() >= 2) {
				// TODO: need interaction to choose cluster to keep
				int choice = 0;
				TileCluster chosenCluster = clustersToKeep.remove(choice);
				// now clustersToKeep are clusters to remove
				maskTilesToRemove = clustersToKeep.stream()
						.flatMap(cluster -> cluster.getTiles().stream())
						.filter(c -> !chosenCluster.getTiles().contains(c))
						.map(TileSkeleton::forceGetCoordinates)
						.collect(Collectors.toSet());
				// actually remove those
				for (Coordinates placeTileToRemove : maskTilesToRemove) {
					playerShip.forceRemoveTile(placeTileToRemove);
				}
			}

			// TODO: notify end of integrity problem
			/*
			UUID playerConnectionUUID = player.getConnectionUUID();
			IClient client = GameServer.getInstance().getClientByUUID(playerConnectionUUID);
            try {
                client.updateClient(new ClientUpdate(playerConnectionUUID));
            } catch (RemoteException e) {
				e.printStackTrace();
            }
            */
        }

		@Override
		public void update(IntegrityProblem integrityProblem) {
			if (!integrityProblem.isProblem()) {
				return;
			}
			// else
			// TOGGLE INTEGRITY CHECK
			new Thread(() -> {
				manageIntegrityProblem(integrityProblem);
				// NOTE: now some tiles have been removed: revalidate ship structure
				player.getShipBoard().validateStructure();
			}).start();
		}
	}
}
