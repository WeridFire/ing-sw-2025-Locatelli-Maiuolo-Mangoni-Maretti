package src.main.java.it.polimi.ingsw.game;

import src.main.java.it.polimi.ingsw.TilesFactory;
import src.main.java.it.polimi.ingsw.cards.Deck;
import src.main.java.it.polimi.ingsw.enums.GamePhaseType;
import src.main.java.it.polimi.ingsw.enums.GameState;
import src.main.java.it.polimi.ingsw.game.exceptions.PlayerAlreadyInGameException;
import src.main.java.it.polimi.ingsw.gamePhases.AdventureGamePhase;
import src.main.java.it.polimi.ingsw.gamePhases.AssembleGamePhase;
import src.main.java.it.polimi.ingsw.gamePhases.exceptions.IncorrectGamePhaseTypeException;
import src.main.java.it.polimi.ingsw.player.Player;
import src.main.java.it.polimi.ingsw.timer.Timer;

import java.util.UUID;

/**
 * Represents a game instance with a unique identifier, game data, and a timer.
 */
public class Game {

    /**
     * Unique identifier for the game.
     */
    private final UUID id;

    /**
     * The game data associated with this game instance.
     */
    private GameData gameData;

    /**
     * Timer for managing game-related events.
     */
    private final Timer timer;

    /**
     * Creates a new game instance with a randomly generated UUID and a Timer.
     */
    public Game() {
        this.id = UUID.randomUUID();
        this.timer = new Timer();
    }

    /**
     * Returns the unique identifier of this game.
     *
     * @return the {@code UUID} of the game
     */
    public UUID getId() {
        return id;
    }

    /**
     * Returns the timer of this game.
     *
     * @return the {@code Timer} of the game
     */
    public Timer getTimer() {
        return timer;
    }

    /**
     * Returns the game data of this game.
     * @return the {@code GameData} of the game.
     */
    public GameData getGameData() {return gameData;}

    /**
     * Starts and manages the game loop.
     * <p>
     * This method is currently a placeholder and needs to be implemented.
     * </p>
     */
    public void gameLoop() throws IncorrectGamePhaseTypeException, InterruptedException {

        //nota sta nel playloop di una phase cambiare il suo stato in "ENDED"
        AssembleGamePhase a = new AssembleGamePhase(id, GamePhaseType.ASSEMBLE, gameData);
        Thread thread = new Thread(a::playLoop);
        thread.start();

        // ASSEMBLE phase
        thread.join();

        gameData.getDeck().drawNextCard();
        AdventureGamePhase adventureGamePhase = null;
        while(gameData.getDeck().getTopCard() != null) {

            //create adventure
            adventureGamePhase = new AdventureGamePhase(id, GamePhaseType.ADVENTURE, gameData, gameData.getDeck().getTopCard());

            thread = new Thread(adventureGamePhase::playLoop);
            thread.start();

            //adventure phase
            thread.join();

            gameData.getDeck().drawNextCard();
        }

        //TODO: vogliamo fare una fase di ending?

    }

    /**
     * Loads game data into the current game instance.
     *
     * @param gameData the game data to load
     * @return {@code true} if the game data was successfully loaded, {@code false} if the provided data is {@code null}
     */
    public boolean loadGameData(GameData gameData) {
        if (gameData == null) return false;

        this.gameData = gameData;
        return true;
    }


    public void addPlayer(Player player) throws PlayerAlreadyInGameException {
        if(gameData.getCurrentGamePhaseType() == GamePhaseType.LOBBY){
            gameData.addPlayer(player);
            if(gameData.getPlayers().size() >= gameData.getRequiredPlayers()){
                startGame();
            }
        }
        //IN here we should handle when a player reconnects. That's why it is handled on the Game level, and not
        //GameData.
    }

    private void startGame(){
        switch(gameData.getLevel()){
            case TESTFLIGHT, ONE -> gameData.setLapSize(18);
            case TWO -> gameData.setLapSize(24);
        }
        gameData.setCoveredTiles(TilesFactory.createPileTiles());
        gameData.setDeck(new Deck(gameData.getLevel()));

        if(gameData.getCurrentGamePhaseType() == GamePhaseType.LOBBY){
			try {
				gameLoop();
			} catch (IncorrectGamePhaseTypeException | InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
    }


}
