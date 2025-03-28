package src.main.java.it.polimi.ingsw.network.socket;

import src.main.java.it.polimi.ingsw.network.ClientUpdate;
import src.main.java.it.polimi.ingsw.network.GameClient;
import src.main.java.it.polimi.ingsw.network.IClient;
import src.main.java.it.polimi.ingsw.network.IServer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

public class SocketClient implements IClient {
	final BufferedReader input;
	final IServer server;
	final GameClient gameClient;

	//TODO: implement the loop for reading client incoming messages and parsing them into updates,
	// to then send to the RMI client.
	public SocketClient(BufferedReader input, BufferedWriter output, GameClient gameClient) {
		this.input = input;
		this.server = new ServerSocketAdapter(output);
		this.gameClient = gameClient;
	}

	private void runVirtualServer() throws IOException {
		String line;

		while ((line = input.readLine()) != null) {
			System.out.println(line);
			//Here we should parse line into a ClientUpdate, and call updateClient.
		}
	}




	@Override
	public IServer getServer() {
		return server;
	}

	/**
	 * Sends an update to the client via SOCKET, which will process it.
	 * @param clientUpdate The update.
	 */
	@Override
	public void updateClient(ClientUpdate clientUpdate) {
		//Now to be completely symmetric with the server, it would come spontaneous to let the RMI handle this call.
		//Basically we deserialized the function and now we send it to the RMI to process properly. HOWEVER,
		// the RMICLIENT does not exist!! There can only be either the RMI or the SOCKET, not BOTH! So we have to move
		// The implementation of this function in the generic GameClient, and both the SOCKET and RMI will call it!
		gameClient.updateClient(clientUpdate);
	}

}
