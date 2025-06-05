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
import it.polimi.ingsw.util.Coordinates;
import it.polimi.ingsw.util.Util;
import it.polimi.ingsw.view.cli.ANSI;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ShipIntegrityListener implements IShipIntegrityListener {
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
				.collect(Collectors.toSet());
		if (maskTilesToRemove.isEmpty()) return false;
		// notify
		PIRDelay pirInfo = new PIRDelay(player, 5,
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
			PIRDelay pirInfo = new PIRDelay(player, 6,
					"Your ship has no valid cluster of tiles." +
							"You need to end your flight...", null);
			integritySequence.addPlayerInputRequest(pirInfo);
			pirHandler.setAndRunTurn(pirInfo, false);
			player.requestEndFlight();
			return false;
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

		PIRMultipleChoice pirChoice = new PIRMultipleChoice(player, 30,
				"Choose one cluster to keep",
				playerShip.getCLIRepresentation(coordCluster, ansiColors),
				options,
				0);
		integritySequence.addPlayerInputRequest(pirChoice);
		int choice = pirHandler.setAndRunTurn(pirChoice, false);

		TileCluster chosenCluster = clustersToKeep.remove(choice);
		// now clustersToKeep are clusters to remove
		Set<Coordinates> maskTilesToRemove = clustersToKeep.stream()
				.flatMap(cluster -> cluster.getTiles().stream())
				.filter(c -> !chosenCluster.getTiles().contains(c))
				.map(TileSkeleton::forceGetCoordinates)
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

		// 1. notify about problems
		PIRDelay pirInfo = new PIRDelay(player, 6,
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
		GameServer.getInstance().broadcastUpdateShipboardSpectators(game, player);
	}

	@Override
	public void update(IntegrityProblem integrityProblem) {
		if (!integrityProblem.isProblem()) {
			if (integritySequence != null) {
				// destroy atomic sequence to continue with other PIRs for this player
				pirHandler.destroyAtomicSequence(integritySequence);
				integritySequence = null;
				notifyEndOfIntegrityProblem();
			}
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
		// TODO (issue): error on skipping the delay PIRs: not accepted but also saved and done all after the cooldown (mega error)
		new Thread(() -> manageIntegrityProblem(integrityProblem)).start();
	}
}