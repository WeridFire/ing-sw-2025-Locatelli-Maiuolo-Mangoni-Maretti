package it.polimi.ingsw.playerInput.PIRs;

import it.polimi.ingsw.GamesHandler;
import it.polimi.ingsw.game.Game;
import it.polimi.ingsw.network.GameServer;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.playerInput.exceptions.WrongPlayerTurnException;
import it.polimi.ingsw.util.Coordinates;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.*;
import java.util.function.BiConsumer;

public class PIRHandler implements Serializable {


	private final Map<Player, PIR> activePIRs = new HashMap<>(4);
	private final List<PIRAtomicSequence> atomicSequences = new ArrayList<>(4);

	private boolean standardRunRefreshAll = true;

	/**
	 * @return the currently running PIRs atomic sequence for the specified player, or {@code null} if there is no
	 * atomic sequence running for the player
	 */
	private PIRAtomicSequence getRunningAtomicSequence(Player player) {
		synchronized (atomicSequences) {
			return atomicSequences.stream().filter(piras -> piras.getPlayer() == player).findFirst().orElse(null);
		}
	}

	/**
	 *
	 * @param p the player to check if they have any active turn currently.
	 *          Note: running atomic sequences are considered as active turns.
	 * @return If the player requested has an active turn waiting to finish.
	 */
	public boolean isPlayerTurnActive(Player p){
		synchronized (activePIRs) {
			if (activePIRs.get(p) != null) return true;
			synchronized (atomicSequences) {
				return getRunningAtomicSequence(p) != null;
			}
		}
	}

	/**
	 * @param player the player to check if it has no other player's input requests running.
	 * @return {@code true} if a new pir can be set and run for the specified player, {@code false} otherwise.
	 */
	public boolean isPlayerReadyForInputRequest(Player player){
		synchronized (activePIRs) {
			return  (activePIRs.get(player) == null);
		}
	}

	/**
	 * Returns the active PIR for the player. Null if not active.
	 * @param p the player to get the current active turn
	 * @return The active turn for the specified player.
	 */
	public PIR getPlayerPIR(Player p){
		synchronized (activePIRs) {
			return activePIRs.get(p);
		}
	}

	/**
	 * @return {@code true} if the specified pir is valid in the player's PIRs atomic sequence, or if there is no
	 * atomic sequence running for the player; {@code false} otherwise.
	 */
	private boolean validateAtomicSequence(PIR pir) {
		synchronized (atomicSequences) {
			PIRAtomicSequence playerAtomicSequence = getRunningAtomicSequence(pir.getCurrentPlayer());
			if (playerAtomicSequence == null) return true;
			else return playerAtomicSequence.isValid(pir);
		}
	}

	/**
	 * Generic function which BLOCKS A THREAD until the turn has been fullfilled.
	 * @param pir The generic PIR to run.
	 * @param refreshAllPlayers Set to {@code true} if the newly set PIR will have to update the view of all the players,
	 *                          otherwise {@code false} to update just the PIR current player's view.
	 */
	private void setAndRunGenericTurn(PIR pir, boolean refreshAllPlayers) {
		// verify correctness of pir and atomic sequences
		synchronized (atomicSequences) {
			while (!validateAtomicSequence(pir)) {
                try {
                    atomicSequences.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
		}
		// setup the pir
		if (!isPlayerReadyForInputRequest(pir.getCurrentPlayer())) {
			throw new RuntimeException("Can not start new turn while another turn has not ended itself");
		}
		synchronized (activePIRs) {
			activePIRs.put(pir.getCurrentPlayer(), pir);
			activePIRs.notifyAll();
		}
		// notify players about the newly set pir
		try {
			Game game = GamesHandler.getInstance().findGameByClientUUID(pir.getCurrentPlayer().getConnectionUUID());
			if(game != null){
				//TODO: find a cleaner way to do this. Currently sometimes game is null because the player associated
				// to the pir is not connected.
				if (refreshAllPlayers) {
					GameServer.getInstance().broadcastUpdate(game);
				} else {
					GameServer.getInstance().broadcastUpdateRefreshOnly(game, Set.of(pir.getCurrentPlayer()));
				}
			}

		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
		// run the pir
		try {
			pir.run();
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
		//Do not clear in here the map entry after the turn has finished. This because after it we still
		//need to retrieve from the object the result of the turn!
	}

	/**
	 * Blocking function that will create a new PIR for the operation of activating
	 * specific tiles on the player's shipboard.
	 * @param activateTiles The PIR to run
	 * @param refreshAllPlayers Set to {@code true} if the newly set PIR will have to update the view of
	 *                          all the players, otherwise {@code false} to update just the PIR current player's view.
	 * @return The activated tiles by the player.
	 */
	public Set<Coordinates> setAndRunTurn(PIRActivateTiles activateTiles, boolean refreshAllPlayers) {
		setAndRunGenericTurn(activateTiles, refreshAllPlayers);
		Set<Coordinates> result = activateTiles.getActivatedTiles();
		removePIR(activateTiles.getCurrentPlayer());
		return result;
	}
	/**
	 * Like {@link #setAndRunTurn(PIRActivateTiles, boolean)}, but uses default value for refreshing all players
	 * ({@code true}).
	 */
	public Set<Coordinates> setAndRunTurn(PIRActivateTiles activateTiles) {
		return setAndRunTurn(activateTiles, standardRunRefreshAll);
	}

	/**
	 * Blocking function that will create a new PIR for the operation of
	 * adding loadables on the player's shipboard. Doesn't return anything,
	 * as the PIR itself affects the model.
	 * @param addLoadables The PIR to run.
	 * @param refreshAllPlayers Set to {@code true} if the newly set PIR will have to update the view of
	 *                          all the players, otherwise {@code false} to update just the PIR current player's view.
	 */
	public void setAndRunTurn(PIRAddLoadables addLoadables, boolean refreshAllPlayers) {
		setAndRunGenericTurn(addLoadables, refreshAllPlayers);
		removePIR(addLoadables.getCurrentPlayer());
	}
	/**
	 * Like {@link #setAndRunTurn(PIRAddLoadables, boolean)}, but uses default value for refreshing all players
	 * ({@code true}).
	 */
	public void setAndRunTurn(PIRAddLoadables addLoadables) {
		setAndRunTurn(addLoadables, standardRunRefreshAll);
	}

	/**
	 * Blocking function that will create a new PIR for the operation of making a
	 * Yes or No choice. It will return the choice selected by the player.
	 * @param choice the PIR to run.
	 * @param refreshAllPlayers Set to {@code true} if the newly set PIR will have to update the view of
	 *                          all the players, otherwise {@code false} to update just the PIR current player's view.
	 * @return True if the player chose "YES"
	 */
	public boolean setAndRunTurn(PIRYesNoChoice choice, boolean refreshAllPlayers) {
		setAndRunGenericTurn(choice, refreshAllPlayers);
		boolean result = choice.isChoiceYes();
		removePIR(choice.getCurrentPlayer());
		return result;
	}
	/**
	 * Like {@link #setAndRunTurn(PIRYesNoChoice, boolean)}, but uses default value for refreshing all players
	 * ({@code true}).
	 */
	public boolean setAndRunTurn(PIRYesNoChoice choice) {
		return setAndRunTurn(choice, standardRunRefreshAll);
	}

	/**
	 * Blocking function that will create a new PIR for the operation of
	 * making the player select an option out of multiple choices, with custom
	 * messages.
	 * @param choice the PIR to run
	 * @param refreshAllPlayers Set to {@code true} if the newly set PIR will have to update the view of
	 *                          all the players, otherwise {@code false} to update just the PIR current player's view.
	 * @return The selected choice
	 */
	public int setAndRunTurn(PIRMultipleChoice choice, boolean refreshAllPlayers) {
		setAndRunGenericTurn(choice, refreshAllPlayers);
		int result = choice.getChoice();
		removePIR(choice.getCurrentPlayer());
		return result;
	}
	/**
	 * Like {@link #setAndRunTurn(PIRMultipleChoice, boolean)}, but uses default value for refreshing all players
	 * ({@code true}).
	 */
	public int setAndRunTurn(PIRMultipleChoice choice) {
		return setAndRunTurn(choice, standardRunRefreshAll);
	}

	public int[] setAndRunTurn(PIRRearrangeLoadables loadablesQuantities, boolean refreshAllPlayers){
		setAndRunGenericTurn(loadablesQuantities, refreshAllPlayers);
		int[] quantities = loadablesQuantities.getQuantities();
		removePIR(loadablesQuantities.getCurrentPlayer());
		return quantities;
	}

	/**
	 * Blocking function that will create a new PIR for the operation of
	 * removing loadables from the player's shipboard. Doesn't return anything,
	 * as the PIR itself affects the model.
	 * @see PIRHandler#setAndRunTurn(PIRAddLoadables)
	 * @param removeLoadables The PIR to run.
	 * @param refreshAllPlayers Set to {@code true} if the newly set PIR will have to update the view of
	 *                          all the players, otherwise {@code false} to update just the PIR current player's view.
	 */
	public void setAndRunTurn(PIRRemoveLoadables removeLoadables, boolean refreshAllPlayers) {
		setAndRunGenericTurn(removeLoadables, refreshAllPlayers);
		removePIR(removeLoadables.getCurrentPlayer());
	}
	/**
	 * Like {@link #setAndRunTurn(PIRRemoveLoadables, boolean)}, but uses default value for refreshing all players
	 * ({@code true}).
	 */
	public void setAndRunTurn(PIRRemoveLoadables removeLoadables) {
		setAndRunTurn(removeLoadables, standardRunRefreshAll);
	}

	/**
	 * Blocking function that will create a new PIR for the operation of
	 * showing an info to the player, waiting any input and doing nothing else. Doesn't return anything,
	 * as the PIR itself does not need to affect the model.
	 * @param delay The PIR to run.
	 * @param refreshAllPlayers Set to {@code true} if the newly set PIR will have to update the view of
	 *                          all the players, otherwise {@code false} to update just the PIR current player's view.
	 */
	public void setAndRunTurn(PIRDelay delay, boolean refreshAllPlayers) {
		setAndRunGenericTurn(delay, refreshAllPlayers);
		removePIR(delay.getCurrentPlayer());
	}
	/**
	 * Like {@link #setAndRunTurn(PIRDelay, boolean)}, but uses default value for refreshing all players
	 * ({@code true}).
	 */
	public void setAndRunTurn(PIRDelay delay) {
		setAndRunTurn(delay, standardRunRefreshAll);
	}

	/**
	 * This function will remove any PIR related to the specified player and notify all waiting threads on activePIRs
	 * @param player The player to which remove the related PIR
	 */
	private void removePIR(Player player) {
		synchronized (activePIRs) {
			activePIRs.remove(player);
			activePIRs.notifyAll();
		}
	}

	/**
	 * Creates a new {@link PIRAtomicSequence} for the specified player.
	 * If another atomic sequence is already active for the same player, this method
	 * will block until it completes. Only one atomic sequence per player is allowed at a time.
	 *
	 * @param player the player for whom to create the atomic sequence
	 * @return the newly created {@link PIRAtomicSequence}
	 */
	public PIRAtomicSequence createAtomicSequence(Player player) {
		synchronized (atomicSequences) {
			// block until all the previous atomic sequences are done
			PIRAtomicSequence piras = getRunningAtomicSequence(player);
			while (piras != null) {
                try {
                    atomicSequences.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                piras = getRunningAtomicSequence(player);
			}
			atomicSequences.add(new PIRAtomicSequence(player));
			atomicSequences.notifyAll();
		}
		return getRunningAtomicSequence(player);
	}

	/**
	 * Destroys the given {@link PIRAtomicSequence}, allowing any waiting threads
	 * (on this or other players' sequences) to proceed.
	 * Also notifies the {@code activePIRs} monitor, which may be used to track turn progression.
	 *
	 * @param pirAtomicSequence the sequence to destroy and remove
	 */
	public void destroyAtomicSequence(PIRAtomicSequence pirAtomicSequence) {
		synchronized (atomicSequences) {
			atomicSequences.remove(pirAtomicSequence);
			atomicSequences.notifyAll();
		}
		// it's like ending a turn -> notify also activePIRs
		synchronized (activePIRs) {
			activePIRs.notifyAll();
		}
	}

	/**
	 * Blocking function that waits until the specified player has no PIR or atomic sequences of PIRs associated to it anymore
	 * @param player The player to check for turn-end
	 */
	public void joinEndTurn(Player player) {
		synchronized (activePIRs) {
			while (isPlayerTurnActive(player)) {
				try {
					activePIRs.wait();
				} catch (InterruptedException e) {
					return;  // TODO: check if it's ok to do so
				}
			}
		}
	}

	/**
	 * Blocking function that waits until the specified player has no PIR associated to it anymore.
	 * Can be inside an atomic sequence: call this function to allow join during atomic sequence running.
	 * @param player The player to check for turn-end
	 */
	public void joinEndInteraction(Player player) {
		synchronized (activePIRs) {
			while (!isPlayerReadyForInputRequest(player)) {
				try {
					activePIRs.wait();
				} catch (InterruptedException e) {
					return;  // TODO: check if it's ok to do so
				}
			}
		}
	}

	public void joinEndTurn(List<Player> players){
		synchronized (activePIRs) {
			boolean allEnded = false;
			while (!allEnded) {
				allEnded = true;
				for (Player player : players) {
					if (isPlayerTurnActive(player)) {
						allEnded = false;
						break;
					}
				}
				if (!allEnded) {
					try {
						activePIRs.wait();
					} catch (InterruptedException e) {
						return;  // TODO: check if it's ok to do so
					}
				}
			}
		}
	}

	/**
	 * This function will force end a turn. It can be called by any player, but it will check ofcourse that the
	 * caller is the one the turn is dedicated to. If that is the case the function will force the turn to end.
	 * <p>
	 * Please call also {@link #joinEndTurn(Player)} after this on the same player if the caller wants to actually
	 * waits until the internal data structure of players and PIRs gets updated for this instance.
	 * @param player The player sending the command to end the turn.
	 * @throws WrongPlayerTurnException If the player is not who the turn is for.
	 */
	public void endTurn(Player player) throws WrongPlayerTurnException {
		if (!isPlayerTurnActive(player)) {
			throw new WrongPlayerTurnException(player);
		}
		synchronized (activePIRs) {
			PIR active = activePIRs.get(player);
			if (active != null) {
				active.endTurn();
			}
		}
		/*
		We don't need in here to nullify generic reference! In fact, check what happens when genericReference ends
		and the caller of genericReference.run() resumes. This happens in this class PIRHandler#setGenericReference.
		As you can see in there the nullification of the reference is already handled.
		 */
	}

	/**
	 * Blocking function that
	 * applies the specified {@code pirCascadeFunction} to each player concurrently,
	 * wait for all the players to produce the input
	 * then return
	 * @param players The list of players to which apply the broadcast.
	 * @param pirCascadeFunction a consumer with a {@link Player} (the "each player" in the broadcast)
	 *                           and a {@link PIRHandler} to set and run all the desired PIRs.
	 *                           Note that this function can handle a sequence of PIRs, that's why it needs to call the
	 *                           {@link PIRHandler#setAndRunGenericTurn(PIR, boolean)} internally.
	 * @throws InterruptedException if one of the instantiated threads throws an {@link InterruptedException}
	 */
	public void broadcastPIR(List<Player> players, BiConsumer<Player, PIRHandler> pirCascadeFunction) throws InterruptedException {
		List<Thread> threads = new ArrayList<>();
		standardRunRefreshAll = false;  // avoid updating view every time another player interacts with the broadcasted pir
		for(Player p : players){
			Thread th = new Thread(() -> pirCascadeFunction.accept(p, this));
			th.start();
			threads.add(th);
		}
		for (Thread th : threads) {
			th.join();
		}
		standardRunRefreshAll = true;  // reset standard view update to refresh all the players view
	}
}
