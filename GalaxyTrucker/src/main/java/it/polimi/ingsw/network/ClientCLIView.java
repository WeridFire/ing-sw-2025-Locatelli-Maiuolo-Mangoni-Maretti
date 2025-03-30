package src.main.java.it.polimi.ingsw.network;

public class ClientCLIView {

	private ClientUpdate lastUpdate;

	public ClientCLIView(ClientUpdate newUpdate){
		if(newUpdate == null){
			return;
		}
		this.lastUpdate = newUpdate;
	}

	public void setLastUpdate(ClientUpdate newUpdate){
		System.out.println("Received new client update.");
		if(newUpdate == null){
			throw new RuntimeException("Newly received update cannot be null.");
		}
		this.lastUpdate = newUpdate;
		clear();
		if(lastUpdate.getCurrentGame() == null){
			displayGamesList();
		}
		displayError();;
	}

	private void displayGamesList(){
		System.out.println("- AVAILABLE GAMES -");
		if(!lastUpdate.getAvailableGames().isEmpty()){
			lastUpdate.getAvailableGames().forEach((g) ->
					System.out.printf("[%s] (%d/%d players) %n", g.getGameId().toString(),
							g.getPlayers().size(),
							g.getRequiredPlayers())
			);
			System.out.println(">join <uuid> <username> to join a game.");
		}
		System.out.println(">create <username> to create a game.");

	}

	private void displayError(){
		if(lastUpdate.getError() != null){
			System.err.println("[SERVER ERROR MESSAGE] " + lastUpdate.getError());
		}
	}

	public static void clear() {
		for (int i = 0; i < 50; i++) {
			System.out.println();
		}
	}
}
