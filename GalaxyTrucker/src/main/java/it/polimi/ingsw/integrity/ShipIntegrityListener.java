package it.polimi.ingsw.integrity;

import it.polimi.ingsw.GamesHandler;
import it.polimi.ingsw.game.Game;
import it.polimi.ingsw.network.GameServer;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.playerInput.PIRs.PIRAtomicSequence;
import it.polimi.ingsw.playerInput.PIRs.PIRDelay;
import it.polimi.ingsw.playerInput.PIRs.PIRHandler;
import it.polimi.ingsw.playerInput.PIRs.PIRMultipleChoice;
import it.polimi.ingsw.shipboard.ShipBoard;
import it.polimi.ingsw.shipboard.TileCluster;
import it.polimi.ingsw.shipboard.integrity.IShipIntegrityListener;
import it.polimi.ingsw.shipboard.integrity.IntegrityProblem;
import it.polimi.ingsw.shipboard.tiles.TileSkeleton;
import it.polimi.ingsw.shipboard.tiles.exceptions.NotFixedTileException;
import it.polimi.ingsw.task.customTasks.TaskDelay;
import it.polimi.ingsw.task.customTasks.TaskMultipleChoice;
import it.polimi.ingsw.util.Coordinates;
import it.polimi.ingsw.util.Util;
import it.polimi.ingsw.view.cli.ANSI;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ShipIntegrityListener implements IShipIntegrityListener {
	private final Player player;
	private final UUID gameID;
	transient private Game game;

	private ShipBoard playerShip;

	public ShipIntegrityListener(Player player, Game game) {
		this.player = player;
		gameID = game.getId();
	}

	private void setLostTile(TileSkeleton lostTile) {
		if (lostTile == null) return;
		try {
			lostTile.unplace();
		} catch (NotFixedTileException e) {
			throw new RuntimeException(e);  // should never happen -> runtime exception
		}
		player.addLostTile(lostTile);
	}

	private void manageIntegrityProblemClustersToRemove(Set<TileCluster> clustersToRemove, BiConsumer<Player, Boolean> afterChoice) {
		Set<Coordinates> maskTilesToRemove = clustersToRemove.stream()
				.flatMap(cluster -> cluster.getTiles().stream())
				.map(TileSkeleton::forceGetCoordinates)
				.collect(Collectors.toSet());
		if (maskTilesToRemove.isEmpty()){
			afterChoice.accept(player, true);
			return;
		}
		// notify
		game.getGameData().getTaskStorage().addTask(
				new TaskDelay(
						player.getUsername(),
						5,
						"These tiles need to be removed...",
						playerShip.getCLIRepresentation(maskTilesToRemove, ANSI.RED),
						(p) -> {
							// actually remove those
							for (Coordinates placeTileToRemove : maskTilesToRemove) {
								setLostTile(playerShip.forceRemoveTile(placeTileToRemove));
							}
							afterChoice.accept(p, true);
						}
				)
		);
	}

	private void manageIntegrityProblemClustersToKeep(List<TileCluster> clustersToKeep, BiConsumer<Player, Boolean> afterChoice) {
		// different behavior based on clustersToKeep size
		// if 0 -> end of flight
		// if >= 2 -> player need to chose which one to keep
		// else -> no problem

		if (clustersToKeep.size() == 1){
			afterChoice.accept(player, false);
			return;
		}
		if (clustersToKeep.isEmpty()) {
			game.getGameData().getTaskStorage().addTask(
					new TaskDelay(player.getUsername(),
							6,
							"Your ship has no valid cluster of tiles. You lost the game...",
							null,
							(p) -> {
								p.requestEndFlight();
								afterChoice.accept(p, false);
							}));
			return;
		}
		// else: clustersToKeep.size() >= 2
		// list tilecluster -> list set Coordinates
		List<Set<Coordinates>> coordCluster = new ArrayList<>();
		for (TileCluster cluster : clustersToKeep) {
			coordCluster.add(cluster.getTiles().stream().
					map(TileSkeleton::forceGetCoordinates)
					.collect(Collectors.toSet()));
		}

		List<String> ansiColors = ANSI.getRandomColors(clustersToKeep.size(), false,
				List.of(ANSI.WHITE, ANSI.BLACK, ANSI.RED));
		int numOptions = clustersToKeep.size();
		String[] options = new String[numOptions];
		for (int i = 0; i < numOptions; i++) {
			options[i] = clustersToKeep.get(i).toString(Util.getModularAt(ansiColors, i));
		}

		game.getGameData().getTaskStorage().addTask(
				new TaskMultipleChoice(
						player.getUsername(),
						30,
						"Choose one cluster to keep",
						playerShip.getCLIRepresentation(coordCluster, ansiColors),
						options,
						0,
						(p, choice) -> {
							TileCluster chosenCluster = clustersToKeep.remove((int) choice);
							Set<Coordinates> maskTilesToRemove = clustersToKeep.stream()
									.flatMap(cluster -> cluster.getTiles().stream())
									.filter(c -> !chosenCluster.getTiles().contains(c))
									.map(TileSkeleton::forceGetCoordinates)
									.collect(Collectors.toSet());
							// actually remove those
							for (Coordinates placeTileToRemove : maskTilesToRemove) {
								setLostTile(playerShip.forceRemoveTile(placeTileToRemove));
							}
							afterChoice.accept(p, true);
						}
				)
		);
	}

	private void manageIntegrityProblem(IntegrityProblem integrityProblem, Consumer<Player> postCheck) {


		game.getGameData().getTaskStorage().addTask(
				new TaskDelay(
						player.getUsername(),
						6,
				"Unfortunately, your Ship has some integrity problems...",
						null,
						(p) -> {
							manageIntegrityProblemClustersToRemove(
									integrityProblem.getClustersToRemove(),
									(p1, revalidate) -> {
										if(revalidate){
											IntegrityProblem pb = playerShip.validateStructure();
											this.update(pb, postCheck);
											return;
										}

										manageIntegrityProblemClustersToKeep(
												integrityProblem.getClustersToKeep(),
												(p2, revalidate1) -> {
													if(revalidate1){
														IntegrityProblem pb = playerShip.validateStructure();
														this.update(pb, postCheck);
														return;
													}
													notifyEndOfIntegrityProblem(postCheck);

												}
										);
									}
							);
						}));

	}

	private void notifyEndOfIntegrityProblem(Consumer<Player> postCheck) {
		GameServer.getInstance().broadcastUpdateShipboardSpectators(game, player);
		postCheck.accept(player);
	}

	@Override
	public void update(IntegrityProblem integrityProblem, Consumer<Player> postCheck) {
		if (!integrityProblem.isProblem()) {
			postCheck.accept(player);
			return;
		}


		if (playerShip == null) {
			playerShip = player.getShipBoard();
		}
		if (game == null) {
			game = GamesHandler.getInstance().getGame(gameID);
		}

		manageIntegrityProblem(integrityProblem, postCheck);
	}
}