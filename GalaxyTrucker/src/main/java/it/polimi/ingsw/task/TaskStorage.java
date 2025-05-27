package it.polimi.ingsw.task;

import it.polimi.ingsw.task.customTasks.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class TaskStorage {

	private final List<Task> previousTasks;

	private TaskActivateTiles currentTaskActivateTiles;
	private TaskAddLoadables currentTaskAddLoadables;
	private TaskDelay currentTaskDelay;
	private TaskMultipleChoice currentTaskMultipleChoice;
	private TaskRemoveLoadables currentTaskRemoveLoadables;
	private TaskYesNoChoice currentTaskYesNoChoice;

	private final UUID gameId;

	/**
	 * Storage for containing all the pending and past tasks of a game.
	 * @param gameId The game associated to the task storage.
	 */
	public TaskStorage(UUID gameId) {
		this.previousTasks = new ArrayList<Task>();
		this.gameId = gameId;
	}

	public void checkIfTaskActive(){
		if (getPendingTask() != null){
			throw new RuntimeException("Another task is already active.");
		}
	}

	/**
	 * Gets the pending task, generically, as a task. May be null if no tasks are active.
	 * @return The pending task.
	 */
	public Task getPendingTask(){
		List<Task> result =  List.of(currentTaskMultipleChoice, currentTaskYesNoChoice, currentTaskDelay,
					currentTaskActivateTiles, currentTaskAddLoadables, currentTaskRemoveLoadables);
		return result.stream().filter(Objects::nonNull).findFirst().orElse(null);
	}

	public void clearTasks(){
		this.currentTaskDelay = null;
		this.currentTaskActivateTiles = null;
		this.currentTaskAddLoadables = null;
		this.currentTaskRemoveLoadables = null;
		this.currentTaskYesNoChoice = null;
		this.currentTaskMultipleChoice = null;
	}

	/**
	 * Functions that gets called periodically by the main thread.
	 * Gets the active task in the game and checks for its completion, calling the
	 * finish function and removing the current task.
	 */
	public void checkTasks(){
		Task task = getPendingTask();
		if(task != null && task.checkCondition()){
			task.finish();
			clearTasks();
			previousTasks.add(task);
		}
	}


	/***
	 * ADDINGS TASKS TO THE CORRECT BIN
	 */


	/**
	 * Register a new task as the active one that the game waits for completion.
	 * @param task the task to be added.
	 */
	public void addTask(TaskAddLoadables task){
		task.setGameId(gameId);
		checkIfTaskActive();
		currentTaskAddLoadables = task;
	}

	/**
	 * Register a new task as the active one that the game waits for completion.
	 * @param task the task to be added.
	 */
	public void addTask(TaskMultipleChoice task){
		task.setGameId(gameId);
		checkIfTaskActive();
		currentTaskMultipleChoice = task;
	}

	/**
	 * Register a new task as the active one that the game waits for completion.
	 * @param task the task to be added.
	 */
	public void addTask(TaskRemoveLoadables task){
		task.setGameId(gameId);
		checkIfTaskActive();
		currentTaskRemoveLoadables = task;
	}

	/**
	 * Register a new task as the active one that the game waits for completion.
	 * @param task the task to be added.
	 */
	public void addTask(TaskYesNoChoice task){
		task.setGameId(gameId);
		checkIfTaskActive();
		currentTaskYesNoChoice = task;
	}

	/**
	 * Register a new task as the active one that the game waits for completion.
	 * @param task the task to be added.
	 */
	public void addTask(TaskActivateTiles task){
		task.setGameId(gameId);
		checkIfTaskActive();
		currentTaskActivateTiles = task;
	}

	/**
	 * Register a new task as the active one that the game waits for completion.
	 * @param task the task to be added.
	 */
	public void addTask(TaskDelay task){
		task.setGameId(gameId);
		checkIfTaskActive();
		currentTaskDelay = task;
	}

	/***
	 * GETTING ACTIVE TASK BIN
	 */


	public TaskActivateTiles getCurrentTaskActivateTiles() {
		return currentTaskActivateTiles;
	}

	public TaskAddLoadables getCurrentTaskAddLoadables() {
		return currentTaskAddLoadables;
	}

	public TaskDelay getCurrentTaskDelay() {
		return currentTaskDelay;
	}

	public TaskMultipleChoice getCurrentTaskMultipleChoice() {
		return currentTaskMultipleChoice;
	}

	public TaskRemoveLoadables getCurrentTaskRemoveLoadables() {
		return currentTaskRemoveLoadables;
	}

	public TaskYesNoChoice getCurrentTaskYesNoChoice() {
		return currentTaskYesNoChoice;
	}
}
