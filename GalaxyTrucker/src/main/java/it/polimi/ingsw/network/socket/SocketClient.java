package src.main.java.it.polimi.ingsw.network.socket;

import src.main.java.it.polimi.ingsw.network.IClient;
import src.main.java.it.polimi.ingsw.network.IServer;

import java.io.BufferedReader;
import java.io.BufferedWriter;

public class SocketClient implements IClient {
	final BufferedReader input;
	final IServer server;

	public SocketClient(BufferedReader input, BufferedWriter output) {
		this.input = input;
		this.server = new ServerSocketAdapter(output);
	}

	@Override
	public IServer getServer() {
		return server;
	}

	@Override
	public void notifyError(String error) {
		System.out.println(error);
	}

	@Override
	public void showUpdate(String update) {
		System.out.println(update);
	}
}
