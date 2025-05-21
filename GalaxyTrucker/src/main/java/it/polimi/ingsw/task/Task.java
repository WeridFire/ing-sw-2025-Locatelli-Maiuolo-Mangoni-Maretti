package it.polimi.ingsw.task;

import java.util.UUID;
import java.util.function.Predicate;

public abstract class Task {

	private UUID gameId;
	private String player;

	protected void setGameId(UUID gameId){
		this.gameId = gameId;
	}

	public boolean checkCondition(){
		return false;
	}

	public void finish(){
		return;
	}



}
