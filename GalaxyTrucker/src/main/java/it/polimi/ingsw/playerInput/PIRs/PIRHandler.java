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


	private final Map<Player, PIR> activePIRs = new HashMap<>();

	private boolean standardRunRefreshAll = true;

	/**
	 *
	 * @return true if any turn in the player input request is set, or null if none is not set.
	 */
	private boolean isAnyTurnActive(){
		synchronized (activePIRs) {
			return !activePIRs.isEmpty();
		}
	}

	/**
	 *
	 * @param p the player to check if they have any active turn currently
	 * @return If the player requested has an active turn waiting to finish.
	 */
	private boolean isPlayerTurnActive(Player p){
		synchronized (activePIRs) {
			return activePIRs.get(p) != null;
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
	 * Generic function which BLOCKS A THREAD until the turn has been fullfilled.
	 * @param pir The generic PIR to run.
	 * @param refreshAllPlayers Set to {@code true} if the newly set PIR will have to update the view of all the players,
	 *                          otherwise {@code false} to update just the PIR current player's view.
	 */
	private void setAndRunGenericTurn(PIR pir, boolean refreshAllPlayers) {
		if(isPlayerTurnActive(pir.getCurrentPlayer())){
			throw new RuntimeException("Can not start new turn while another turn has not ended itself");
		}
		synchronized (activePIRs) {
			activePIRs.put(pir.getCurrentPlayer(), pir);
		}
		// notify players about the newly set pir
		try {
			Game game = GamesHandler.getInstance().findGameByClientUUID(pir.getCurrentPlayer().getConnectionUUID());
			if (refreshAllPlayers) {
				GameServer.getInstance().broadcastUpdate(game);
			} else {
				GameServer.getInstance().broadcastUpdateRefreshOnly(game, Set.of(pir.getCurrentPlayer()));
			}
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}

		try {
			pir.run();
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
		//Do not clear in here the map entry after the turn has finished. This because after it we still
		//need to retrieve from the object the result of the turn!
	}

	/**
	 * Generic function which BLOCKS A THREAD until the turn has been fullfilled.
	 * It asks all the players to refresh their view.
	 * If this behavior is not desired, check out {@link #setAndRunGenericTurn(PIR, boolean)}
	 * @param pir The generic PIR to run.
	 */
	private void setAndRunGenericTurn(PIR pir) {
		setAndRunGenericTurn(pir, standardRunRefreshAll);
	}

	/**
	 * Blocking function that will create a new PIR for the operation of activating
	 * specific tiles on the player's shipboard.
	 * @param activateTiles The PIR to run
	 * @return The activated tiles by the player.
	 */
	public Set<Coordinates> setAndRunTurn(PIRActivateTiles activateTiles) {
		setAndRunGenericTurn(activateTiles);
		Set<Coordinates> result = activateTiles.getActivatedTiles();
		removePIR(activateTiles.getCurrentPlayer());
		return result;
	}

	/**
	 * Blocking function that will create a new PIR for the operation of
	 * adding loadables on the player's shipboard. Doesn't return anything,
	 * as the PIR itself affects the model.
	 * @param addLoadables The PIR to run.
	 */
	public void setAndRunTurn(PIRAddLoadables addLoadables) {
		setAndRunGenericTurn(addLoadables);
		removePIR(addLoadables.getCurrentPlayer());
	}


	/**
	 * Blocking function that will create a new PIR for the operation of making a
	 * Yes or No choice. It will return the choice selected by the player.
	 * @param choice the PIR to run.
	 * @return True if the player chose "YES"
	 */
	public boolean setAndRunTurn(PIRYesNoChoice choice) {
		setAndRunGenericTurn(choice);
		boolean result = choice.isChoiceYes();
		removePIR(choice.getCurrentPlayer());
		return result;
	}

	/**
	 * Blocking function that will create a new PIR for the operation of
	 * making the player select an option out of multiple choices, with custom
	 * messages.
	 * @param choice the PIR to run
	 * @return The selected choice
	 */
	public int setAndRunTurn(PIRMultipleChoice choice) {
		setAndRunGenericTurn(choice);
		int result = choice.getChoice();
		removePIR(choice.getCurrentPlayer());
		return result;
	}

	/**
	 * Blocking function that will create a new PIR for the operation of
	 * removing loadables from the player's shipboard. Doesn't return anything,
	 * as the PIR itself affects the model.
	 * @see PIRHandler#setAndRunTurn(PIRAddLoadables)
	 * @param removeLoadables The PIR to run.
	 */
	public void setAndRunTurn(PIRRemoveLoadables removeLoadables) {
		setAndRunGenericTurn(removeLoadables);
		removePIR(removeLoadables.getCurrentPlayer());
	}

	/**
	 * Blocking function that will create a new PIR for the operation of
	 * showing an info to the player, waiting any input and doing nothing else. Doesn't return anything,
	 * as the PIR itself does not need to affect the model.
	 * @see PIRHandler#setAndRunTurn(PIRAddLoadables)
	 * @param delay The PIR to run.
	 */
	public void setAndRunTurn(PIRDelay delay) {
		setAndRunGenericTurn(delay);
		removePIR(delay.getCurrentPlayer());
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
	 * Blocking function that waits until the specified player has no PIR associated to it anymore
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
	 * @param game The game from which this method will get the players and will apply the broadcast.
	 * @param pirCascadeFunction a consumer with a {@link Player} (the "each player" in the broadcast)
	 *                           and a {@link PIRHandler} to set and run all the desired PIRs.
	 *                           Note that this function can handle a sequence of PIRs, that's why it needs to call the
	 *                           {@link PIRHandler#setAndRunGenericTurn(PIR)} internally.
	 * @throws InterruptedException if one of the instantiated threads throws an {@link InterruptedException}
	 */
	public void broadcastPIR(Game game, BiConsumer<Player, PIRHandler> pirCascadeFunction) throws InterruptedException {
		List<Thread> threads = new ArrayList<>();
		standardRunRefreshAll = false;  // avoid updating view every time another player interacts with the broadcasted pir
		for(Player p : game.getGameData().getPlayers()){
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
