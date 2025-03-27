package src.main.java.it.polimi.ingsw.client.socket;

import java.util.List;

public class SocketMessage {

	private final String type;
	private final List<String> args;
	private final int argsAmount;

	public SocketMessage(String type, List<String> args) {
		this.type = type;
		this.args = args;
		this.argsAmount = args.size();
	}

	public String getType() {
		return type;
	}

	public List<String> getArgs() {
		return args;
	}

	public int getArgsAmount() {
		return argsAmount;
	}
}
