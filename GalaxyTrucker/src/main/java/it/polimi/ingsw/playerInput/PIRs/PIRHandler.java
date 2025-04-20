package it.polimi.ingsw.playerInput.PIRs;

import it.polimi.ingsw.GamesHandler;
import it.polimi.ingsw.network.GameServer;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.playerInput.exceptions.WrongPlayerTurnException;
import it.polimi.ingsw.util.Coordinates;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PIRHandler implements Serializable {


	private Map<Player, PIR> activePIRs = new HashMap<>();

	/**
	 *
	 * @return true if any turn in the player input request is set, or null if none is not set.
	 */
	private boolean isAnyTurnActive(){
		return !activePIRs.isEmpty();
	}

	/**
	 *
	 * @param p the player to check if they have any active turn currently
	 * @return If the player requested has an active turn waiting to finish.
	 */
	private boolean isPlayerTurnActive(Player p){
		return activePIRs.get(p) != null;
	}

	/**
	 * Returns the active PIR for the player. Null if not active.
	 * @param p the player to get the current active turn
	 * @return The active turn for the specified player.
	 */
	public PIR getPlayerPIR(Player p){
		return activePIRs.get(p);
	}

	/**
	 * Generic function which BLOCKS A THREAD until the turn has been fullfilled.
	 * @param pir The generic PIR to run.
	 */
	private void setAndRunGenericTurn(PIR pir) {
		if(isPlayerTurnActive(pir.getCurrentPlayer())){
			throw new RuntimeException("Can not start new turn while another turn has not ended itself");
		}
		this.activePIRs.put(pir.getCurrentPlayer(), pir);
		// notify players about the newly set pir
        try {
            GameServer.getInstance().broadcastUpdate(GamesHandler.getInstance()
					.findGameByClientUUID(pir.getCurrentPlayer().getConnectionUUID()));
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

        try{
			pir.run();
		}catch(InterruptedException e){
			e.printStackTrace();
		}
		//Do not clear in here the map entry after the turn has finished. This because after it we still
		//need to retrieve from the object the result of the turn!
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
		activePIRs.remove(activateTiles.getCurrentPlayer());
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
		activePIRs.remove(addLoadables.getCurrentPlayer());
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
		activePIRs.remove(choice.getCurrentPlayer());
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
		activePIRs.remove(choice.getCurrentPlayer());
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
		activePIRs.remove(removeLoadables.getCurrentPlayer());
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
		activePIRs.remove(delay.getCurrentPlayer());
	}


	/**
	 * This function will force end a turn. It can be called by any player, but it will check ofcourse that the
	 * caller is the one the turn is dedicated to. If that is the case the function will force the turn to end.
	 * @param player The player sending the command to end the turn.
	 * @throws WrongPlayerTurnException If the player is not who the turn is for.
	 */
	public void endTurn(Player player) throws WrongPlayerTurnException {
		if(!isPlayerTurnActive(player)) {
			throw new WrongPlayerTurnException(player);
		}
		PIR active = activePIRs.get(player);
		if(active != null){
			active.endTurn();
		}
		/*
		We don't need in here to nullify generic reference! In fact, check what happens when genericReference ends
		and the caller of genericReference.run() resumes. This happens in this class PIRHandler#setGenericReference.
		As you can see in there the nullification of the reference is already handled.
		 */
	}
}
