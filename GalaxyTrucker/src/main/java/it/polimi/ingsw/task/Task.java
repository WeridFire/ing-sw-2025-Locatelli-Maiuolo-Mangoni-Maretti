package it.polimi.ingsw.task;

import it.polimi.ingsw.GamesHandler;
import it.polimi.ingsw.game.Game;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.playerInput.PIRType;
import it.polimi.ingsw.playerInput.exceptions.InputNotSupportedException;
import it.polimi.ingsw.playerInput.exceptions.TileNotAvailableException;
import it.polimi.ingsw.playerInput.exceptions.WrongPlayerTurnException;
import it.polimi.ingsw.shipboard.LoadableType;
import it.polimi.ingsw.shipboard.tiles.exceptions.TooMuchLoadException;
import it.polimi.ingsw.shipboard.tiles.exceptions.UnsupportedLoadableItemException;
import it.polimi.ingsw.util.Coordinates;
import it.polimi.ingsw.view.cli.ICLIPrintable;

import java.util.*;
import java.util.function.Predicate;

public abstract class Task implements ICLIPrintable {

	/**
	 * The gameID associated to this task.
	 */
	private UUID gameId;

	/**
	 * The username of the player associated to this task. Can be null.
	 */
	private String player;

	/**
	 * The duration of the task, after which it will be autocompleted
	 */
	private int duration;

	/**
	 * The expiration of the task, set based on the duration upon registration of the task to the task handler.
	 */
	private int expiration;

	/**
	 * The type of the current task.
	 */
	private TaskType taskType;


	public Task(String player, int duration, TaskType taskType){
		this.player = player;
		this.duration = duration;
		this.taskType = taskType;
	}


	/*
	* Task Class Getters
	* */


	/**
	 * Sets the gameID of the game associated to this task. This should be called only when finalizing the task and
	 * registering it onto the task handler.
	 * @param gameId the gameID
	 */
	protected void setGameId(UUID gameId){
		this.gameId = gameId;
	}

	/**
	 * Gets the current epoch timestamp, in seconds
	 * @return the current epoch timestamp in seconds
	 */
	public static int getEpochTimestamp(){
		return (int) (System.currentTimeMillis() / 1000);
	}

	/**
	 * Returns a reference to the instance of the player this task is dedicated to.
	 * @return The player instance.
	 */
	protected Player getPlayer(){
		Game game = GamesHandler.getInstance().getGame(gameId);
		return game
				.getGameData()
				.getPlayer(
						(p) -> p.getUsername()
								.equals(getUsername()),
				null);
	}

	/**
	 * Gets the duration of the task, in seconds.
	 * @return the duration of the task.
	 */
	public int getDuration(){
		return this.duration;
	}

	/**
	 * Gets the scheduled expiration epoch timestamp in seconds.
	 * @return the scheduled expiration epoch.
	 */
	public int getExpiration(){
		return this.expiration;
	}

	/**
	 * Gets the type of the current task.
	 * @return the type of the current task.
	 */
	public TaskType getTaskType(){
		return this.taskType;
	}

	/**
	 * Gets the ID of the game this task is linked to.
	 * @return the UUID of the game.
	 */
	public UUID getGameId(){
		return this.gameId;
	}

	/**
	 * Gets the username of the player this task is dedicated to.
	 * @return The username of the player.
	 */
	public String getUsername(){
		return this.getUsername();
	}

	// UTILS FOR SPECIFIC TASKS

	public abstract Set<Coordinates> getHighlightMask();

	protected void checkForTileMask(Coordinates coordinate) throws TileNotAvailableException {
		if(!getHighlightMask().contains(coordinate)){
			throw new TileNotAvailableException(coordinate, taskType);
		}
	}

	protected void checkForTurn(String checkingPlayer) throws WrongPlayerTurnException {
		if(!Objects.equals(this.player, player)){
			throw new WrongPlayerTurnException(player, checkingPlayer, this.taskType);
		}
	}

	/**
	 * This function gets called by the TaskStorage upon completion of a task.
	 */
	protected abstract void finish();


	/**
	 * Function that checks some task requirements for completion, and then returns if it is complete or not.
	 * @return whether if the task has been completed or not.
	 */
	public boolean checkCondition(){
		return false;
	}




	//METHODS FOR SPECIFIC TASKS

	public void makeChoice(Player player, int choice) throws WrongPlayerTurnException, InputNotSupportedException {
		throw new InputNotSupportedException(getTaskType());
	}

	public void addLoadables(Player player, Map<Coordinates, List<LoadableType>> cargoToAdd) throws WrongPlayerTurnException, TileNotAvailableException, UnsupportedLoadableItemException, TooMuchLoadException, InputNotSupportedException {
		throw new InputNotSupportedException(getTaskType());
	}



}
