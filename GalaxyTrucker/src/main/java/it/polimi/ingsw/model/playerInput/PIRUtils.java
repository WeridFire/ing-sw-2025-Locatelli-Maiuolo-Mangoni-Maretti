package it.polimi.ingsw.model.playerInput;

import it.polimi.ingsw.GamesHandler;
import it.polimi.ingsw.model.cards.projectile.Projectile;
import it.polimi.ingsw.enums.PowerType;
import it.polimi.ingsw.enums.ProtectionType;
import it.polimi.ingsw.enums.Rotation;
import it.polimi.ingsw.model.game.Game;
import it.polimi.ingsw.model.game.GameData;
import it.polimi.ingsw.model.playerInput.PIRs.*;
import it.polimi.ingsw.network.GameServer;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.shipboard.LoadableType;
import it.polimi.ingsw.model.shipboard.ShipBoard;
import it.polimi.ingsw.model.shipboard.TileCluster;
import it.polimi.ingsw.model.shipboard.exceptions.NoTileFoundException;
import it.polimi.ingsw.model.shipboard.exceptions.OutOfBuildingAreaException;
import it.polimi.ingsw.model.shipboard.integrity.IShipIntegrityListener;
import it.polimi.ingsw.model.shipboard.integrity.IntegrityProblem;
import it.polimi.ingsw.model.shipboard.tiles.TileSkeleton;
import it.polimi.ingsw.model.shipboard.tiles.exceptions.NotFixedTileException;
import it.polimi.ingsw.model.shipboard.visitors.VisitorCalculatePowers;
import it.polimi.ingsw.util.Coordinates;
import it.polimi.ingsw.util.Default;
import it.polimi.ingsw.util.Logger;
import it.polimi.ingsw.util.Util;
import it.polimi.ingsw.view.cli.ANSI;

import java.rmi.RemoteException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
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
		if(player.hasRequestedEndFlight()){
			return 0f;
		}

		VisitorCalculatePowers.CalculatorPowerInfo powerInfo = player.getShipBoard().getVisitorCalculatePowers().getInfoPower(powerType);
		if (powerInfo == null) {
			return 0f;
		}
		PIRActivateTiles inputRequest = new PIRActivateTiles(player, Default.PIR_SECONDS, powerType);
		// phase 1: ask activation
		Set<Coordinates> activatedTiles = game.getPIRHandler().setAndRunTurn(inputRequest);

        // phase 2: ask batteries removal for desired activation
		int batteriesToRemove = activatedTiles.size();
		if(batteriesToRemove > 0){
			PIRRemoveLoadables pirRemoveLoadables = new PIRRemoveLoadables(player, Default.PIR_SECONDS, Set.of(LoadableType.BATTERY), batteriesToRemove);
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
	 * Note: it the projectile is not going to hit the ship, no request is done and the side is considered "defended".
	 *
	 * @param player The player whose shield tiles should be activated.
	 * @param projectile The projectile the player's ship is going to be hit with.
	 * @param game The game data context in which the interaction takes place.
	 * @return {@code true} if the side has been protected, {@code false} if the ship will be hit.
	 */
	public static boolean runPlayerProjectileDefendRequest(Player player, Projectile projectile, GameData game) {
		if(player.hasRequestedEndFlight()){
			return true;
		}

		ShipBoard playerShip = player.getShipBoard();
		// check if ship is going to be hit. if not -> no need to defend
		Coordinates firstTilePlace = playerShip.getFirstTileLocation(projectile.getDirection(), projectile.getCoord());
		if (firstTilePlace == null) {
			return true;
		}

		// process projectile from lowest-energy method to defend it to most expensive

		// 1. smooth side and bouncy projectile
		if (projectile.isBouncy()) {
			try {
				if (!playerShip.getTile(firstTilePlace).getSide(projectile.getDirection()).isConnector()) {
					return true;
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
				return true;

			} else if(defendingCannon == ProtectionType.DOUBLE_CANNON) {

				if(playerShip
						.getVisitorCalculateCargoInfo()
						.getBatteriesInfo()
						.count(LoadableType.BATTERY) == 0){
					//player doesn't have enough batteries.
					return false;
				}

				//Asking player to activate double cannon
				String message = "You are being hit from direction " + projectile.getDirection().toString() + ". You can defend yourself " +
						"with a double cannon. Do you want to activate it?";
				PIRYesNoChoice choiceReq = new PIRYesNoChoice(player, Default.PIR_SECONDS, message, false);
				boolean activateToDefend = game.getPIRHandler().setAndRunTurn(choiceReq);
				if (activateToDefend) {
					game.getPIRHandler().setAndRunTurn(
							new PIRRemoveLoadables(player, Default.PIR_SECONDS, Set.of(LoadableType.BATTERY), 1));
					return true;
				}
			}
		}

		// 3. shield and shield-defensible
		if (projectile.isShieldDefensible()) {
			if(playerShip
					.getVisitorCalculateCargoInfo()
					.getBatteriesInfo()
					.count(LoadableType.BATTERY) == 0){
				//player doesn't have enough batteries.
				return false;
			}

			if(!playerShip
					.getVisitorCalculateShieldedSides()
					.hasShieldFacing(projectile.getDirection()
					.getRotated(Rotation.OPPOSITE))){
				//player doesn't have a shield on that side
				return false;
			}

			String message = "You are being hit from direction " + projectile.getDirection().toString() + ". You can defend yourself " +
					"with a shield. Do you want to activate it?";
			PIRYesNoChoice choiceReq = new PIRYesNoChoice(player, Default.PIR_SECONDS, message, false);
			boolean activateToDefend = game.getPIRHandler().setAndRunTurn(choiceReq);
			if (activateToDefend) {
				game.getPIRHandler().setAndRunTurn(
						new PIRRemoveLoadables(player, Default.PIR_SECONDS, Set.of(LoadableType.BATTERY), 1));
				return true;
			}
		}

		// if here: no method used to defend the hit -> player will be hit
        return false;
    }

	public static void runPlayerMovementForward(Player player, int movement, GameData game,
												Function<Integer, String> messageRetriever) {
		game.getPIRHandler().setAndRunTurn(
				new PIRDelay(player, Default.PIR_SHORT_SECONDS,
						messageRetriever.apply(movement), null)
		);
		game.movePlayerForward(player, movement);
	}
	public static void runPlayerMovementForward(Player player, int movement, GameData game) {
		runPlayerMovementForward(player, movement, game, (m) -> "You are going to gain " + m + " flight days");
	}

	public static void runPlayerMovementBackward(Player player, int movement, GameData game,
												 Function<Integer, String> messageRetriever) {
		game.getPIRHandler().setAndRunTurn(
				new PIRDelay(player, Default.PIR_SHORT_SECONDS, messageRetriever.apply(movement), null)
		);
		game.movePlayerBackward(player, movement);
	}
	public static void runPlayerMovementBackward(Player player, int movement, GameData game) {
		runPlayerMovementBackward(player, movement, game, (m) -> "You are going to lose " + m + " flight days");
	}

	private static BiConsumer<Player, PIRHandler> getRunnerProjectileInfo(Player diceTosser, String cause) {
		Function<Player, String> messageRetriever = (p) -> cause
				+ (Objects.equals(p.getUsername(), diceTosser.getUsername())
				? ": The fate is in your hand, toss the dice!"
                : ": " + diceTosser.toColoredString() + " is deciding the fate of the flight..."
        );
		return (p, pirHandler) -> pirHandler.setAndRunTurn(new PIRDelay(p,
				Default.PIR_SHORT_SECONDS, messageRetriever.apply(p), null));
	}

	private static void runPlayerHitAttempt(Player player, Projectile proj, GameData game) {
		// notify hit
		PIRDelay pirDelay = new PIRDelay(player, Default.PIR_SHORT_SECONDS,
				proj.toVerboseString(),
				proj.getCLIRepresentation(player.getShipBoard()
				));
		game.getPIRHandler().setAndRunTurn(pirDelay);
		// actually try to hit
		boolean defended = PIRUtils.runPlayerProjectileDefendRequest(player, proj, game);
		if(!defended){
			try {
				player.getShipBoard().hit(proj.getDirection(), proj.getCoord());
			} catch (NoTileFoundException | OutOfBuildingAreaException e) {
				throw new RuntimeException(e);  // should never happen -> runtime exception
			}
		}
	}

	public static void runProjectile(Player diceTosser, Projectile proj, GameData game,
									 boolean broadcastHit, String cause) throws InterruptedException {
		proj.roll2D6();
		if (broadcastHit) {
			game.getPIRHandler().broadcastPIR(game.getPlayersInFlight(), (p, pirHandler) -> {
				PIRUtils.getRunnerProjectileInfo(diceTosser, cause).accept(p, pirHandler);
				PIRUtils.runPlayerHitAttempt(p, proj, game);
			});
		} else {
			PIRUtils.getRunnerProjectileInfo(diceTosser, cause).accept(diceTosser, game.getPIRHandler());
			PIRUtils.runPlayerHitAttempt(diceTosser, proj, game);
		}
	}

	public static void runEndFlight(Player player, PIRHandler pirHandler) {
		boolean ended = player.endFlight();
		pirHandler.setAndRunTurn(new PIRDelay(player, Default.PIR_SHORT_SECONDS, (ended
				? "Here your flight falls silent, echoing across the stars..."
				: "Against all odds, the stars aligned: you're not done yet, continue your flight!"),
				null));
	}


	public static class ShipIntegrityListener implements IShipIntegrityListener {
		private final Player player;
		private final UUID gameID;
		transient private Game game;
		private final PIRHandler pirHandler;
		private ShipBoard playerShip;
		private PIRAtomicSequence integritySequence;

		public ShipIntegrityListener(Player player, Game game) {
			this.player = player;
			gameID = game.getId();
			pirHandler = game.getGameData().getPIRHandler();
		}

		private void setLostTile(TileSkeleton lostTile) {
			if (lostTile == null) return;
            try {
                lostTile.unplace();
            } catch (NotFixedTileException e) {
                throw new RuntimeException(e);  // should never happen -> runtime exception
            }
            player.setLostTile(lostTile);
		}

		private boolean manageIntegrityProblemClustersToRemove(Set<TileCluster> clustersToRemove) {
			Set<Coordinates> maskTilesToRemove = clustersToRemove.stream()
					.flatMap(cluster -> cluster.getTiles().stream())
					.map(TileSkeleton::forceGetCoordinates)
					.filter(Objects::nonNull)
					.collect(Collectors.toSet());
			if (maskTilesToRemove.isEmpty()) return false;
			// notify
			PIRDelay pirInfo = new PIRDelay(player, Default.PIR_SHORT_SECONDS,
					"These tiles needs to be removed...",
					playerShip.getCLIRepresentation(maskTilesToRemove, ANSI.RED));
			integritySequence.addPlayerInputRequest(pirInfo);
			pirHandler.setAndRunTurn(pirInfo, false);
			// actually remove those
			for (Coordinates placeTileToRemove : maskTilesToRemove) {
				setLostTile(playerShip.forceRemoveTile(placeTileToRemove));
			}
			// NOTE: now some tiles have been removed
			return true;
		}

		private boolean manageIntegrityProblemClustersToKeep(List<TileCluster> clustersToKeep) {
			// different behavior based on clustersToKeep size
			// if 0 -> end of flight
			// if >= 2 -> player need to chose which one to keep
			// else -> no problem
			if (clustersToKeep.size() == 1) return false;
			if (clustersToKeep.isEmpty()) {
				Logger.info("Starting end flight pir.");
				PIRDelay pirInfo = new PIRDelay(player, Default.PIR_SHORT_SECONDS,
						"Your ship has no valid cluster of tiles." +
								" You need to end your flight...", null);
				integritySequence.addPlayerInputRequest(pirInfo);
				pirHandler.setAndRunTurn(pirInfo, false);
				pirHandler.destroyAtomicSequence(integritySequence);
				Logger.info("Ending end flight pir.");
				player.requestEndFlight();
				return false;
			}
			// else: clustersToKeep.size() >= 2
			// list tilecluster -> list set Coordinates
			List<Set<Coordinates>> coordCluster = new ArrayList<>();
			for (TileCluster cluster : clustersToKeep) {
				coordCluster.add(cluster.getTiles().stream().
						map(TileSkeleton::forceGetCoordinates)
						.filter(Objects::nonNull)
						.collect(Collectors.toSet()));
			}

			// prepare colors and options as string
			List<String> ansiColors = ANSI.getRandomColors(clustersToKeep.size(), false,
					List.of(ANSI.WHITE, ANSI.BLACK, ANSI.RED));
			int numOptions = clustersToKeep.size();
			String[] options = new String[numOptions];
			for (int i = 0; i < numOptions; i++) {
				options[i] = clustersToKeep.get(i).toString(Util.getModularAt(ansiColors, i));
			}
			// send player input request
			PIRMultipleChoice pirChoice = new PIRMultipleChoice(player, Default.PIR_SECONDS,
					"Choose one cluster to keep",
					playerShip.getCLIRepresentation(coordCluster, ansiColors),
					options,
					0);
			pirChoice.addTag(IntegrityProblem.TAG);
			integritySequence.addPlayerInputRequest(pirChoice);
			int choice = pirHandler.setAndRunTurn(pirChoice, false);

			TileCluster chosenCluster = clustersToKeep.remove(choice);
			// now clustersToKeep are clusters to remove
			Set<Coordinates> maskTilesToRemove = clustersToKeep.stream()
					.flatMap(cluster -> cluster.getTiles().stream())
					.filter(c -> !chosenCluster.getTiles().contains(c))
					.map(TileSkeleton::forceGetCoordinates)
					.filter(Objects::nonNull)
					.collect(Collectors.toSet());
			// actually remove those
			for (Coordinates placeTileToRemove : maskTilesToRemove) {
				setLostTile(playerShip.forceRemoveTile(placeTileToRemove));
			}

			// NOTE: now some tiles have been removed
			return true;
		}

		private void manageIntegrityProblem(IntegrityProblem integrityProblem) {
			boolean revalidateStructure;

			// 0. if no more humans: end here the flight and the problem
			if (integrityProblem.isNoMoreHumansProblem()) {
				PIRDelay pirInfo = new PIRDelay(player, Default.PIR_SHORT_SECONDS,
						"Your ship has no more humans." +
								" You need to end your flight...", null);
				integritySequence.addPlayerInputRequest(pirInfo);
				pirHandler.setAndRunTurn(pirInfo, false);
				interruptProblem();
				player.requestEndFlight();
				return;
			}

			// 1. notify about problems
			PIRDelay pirInfo = new PIRDelay(player, Default.PIR_SHORT_SECONDS,
					"Unfortunately, your Ship has some integrity problems...", null);
			integritySequence.addPlayerInputRequest(pirInfo);
			pirHandler.setAndRunTurn(pirInfo, false);

			// 2. notify about all the clusters that must be removed
			revalidateStructure = manageIntegrityProblemClustersToRemove(integrityProblem.getClustersToRemove());
			if (revalidateStructure) {
				playerShip.validateStructure();
				return;
			}

			// 3. notify about all the clusters competing to stay in the ship
			revalidateStructure = manageIntegrityProblemClustersToKeep(integrityProblem.getClustersToKeep());
			if (revalidateStructure) {
				playerShip.validateStructure();
				return;
			}

			notifyEndOfIntegrityProblem();
        }

		private void notifyEndOfIntegrityProblem() {
			try {
				GameServer.getInstance().broadcastUpdateShipboardSpectators(game, player);
			} catch (RemoteException e) {
				Logger.error("RemoteException while broadcasting end of integrity problem");
			}
		}

		private void interruptProblem() {
			if (integritySequence != null) {
				// destroy atomic sequence to continue with other PIRs for this player
				pirHandler.destroyAtomicSequence(integritySequence);
				integritySequence = null;
				notifyEndOfIntegrityProblem();
			}
		}

		@Override
		public void update(IntegrityProblem integrityProblem) {
			if (!integrityProblem.isProblem()) {
				interruptProblem();
				return;
			}

			if (integritySequence == null) {
				// create atomic sequence to block other PIRs for this player
				integritySequence = pirHandler.createAtomicSequence(player);
			} else {
				integritySequence.clear();
			}

			if (playerShip == null) {
				playerShip = player.getShipBoard();
			}
			if (game == null) {
				game = GamesHandler.getInstance().getGame(gameID);
			}

			// TOGGLE INTEGRITY CHECK
			new Thread(() -> manageIntegrityProblem(integrityProblem)).start();
		}
	}
}
