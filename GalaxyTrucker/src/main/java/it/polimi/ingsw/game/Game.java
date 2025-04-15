package it.polimi.ingsw.game;

import it.polimi.ingsw.TilesFactory;
import it.polimi.ingsw.cards.Deck;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.game.exceptions.PlayerAlreadyInGameException;
import it.polimi.ingsw.gamePhases.AdventureGamePhase;
import it.polimi.ingsw.gamePhases.AssembleGamePhase;
import it.polimi.ingsw.gamePhases.LobbyGamePhase;
import it.polimi.ingsw.network.GameServer;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.shipboard.ShipBoard;
import it.polimi.ingsw.shipboard.tiles.TileSkeleton;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
     * Creates a new game, based on a GameData. Creates an ID and a timer for it.
     * @param resumeGame The game data to resume.
     */
    public Game(GameData resumeGame) {
        this.id = resumeGame.getGameId();
        loadGameData(resumeGame);
    }

    /**
     * Creates a new game, with a new game.
     */
    public Game(){
        this.id = UUID.randomUUID();
        loadGameData(new GameData(id));
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
    public void gameLoop() throws InterruptedException, RemoteException {

        LobbyGamePhase l = new LobbyGamePhase(id, gameData);
        getGameData().setCurrentGamePhase(l);
        l.playLoop();

        //Call function to initialize all players stuff.
        initGame();

        AssembleGamePhase a = new AssembleGamePhase(id, gameData);
        getGameData().setCurrentGamePhase(a);

        //We notify all players about the new game state
        GameServer.getInstance().broadcastUpdate(this);

        //Blocking function that waits for everyone to finish set up their shipboard aliens
        fillUpShipboards();

        a.playLoop();

        gameData.getDeck().drawNextCard();
        AdventureGamePhase adventureGamePhase = null;
        while(gameData.getDeck().getTopCard() != null) {
            //create adventure
            adventureGamePhase = new AdventureGamePhase(id, gameData, gameData.getDeck().getTopCard());
            getGameData().setCurrentGamePhase(adventureGamePhase);

            //We notify all players about the new game state
            GameServer.getInstance().broadcastUpdate(this);

            adventureGamePhase.playLoop();

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
    private boolean loadGameData(GameData gameData) {
        if (gameData == null) return false;

        this.gameData = gameData;
        return true;
    }


    public void addPlayer(Player player) throws PlayerAlreadyInGameException {
        if(gameData.getCurrentGamePhaseType() == GamePhaseType.LOBBY){
            gameData.addPlayer(player);
        }
        //IN here we should handle when a player reconnects. That's why it is handled on the Game level, and not
        //GameData.
    }

    private void initGame(){
        //After the lobby phase has ended, we initialize the game.

        switch(gameData.getLevel()){
            case TESTFLIGHT, ONE -> gameData.setLapSize(18);
            case TWO -> gameData.setLapSize(24);
        }

        List<TileSkeleton> t = TilesFactory.createPileTiles();
        Collections.shuffle(t);

        //assign numeric progressive id to shuffled tiles
        for (int i = 0; i < t.size(); i++) {
            t.get(i).setTileId(i);
        }

        gameData.setCoveredTiles(t);

        gameData.setDeck(new Deck(gameData.getLevel()));

        int playerIndex = 0;
        for (Player player : gameData.getPlayers()) {
            player.setShipBoard(ShipBoard.create(gameData.getLevel(), playerIndex));
            playerIndex++;
        }
    }

    /**
     * Blocking function that will wait for all the players to fill up their shipboard in non-obvious cases
     * through multiple PIRs choice, in parallel on new threads. Will return once everyone has filled up their
     * @throws InterruptedException
     */
    private void fillUpShipboards() throws InterruptedException {
        List<Thread> threads = new ArrayList<>();
        for(Player p : gameData.getPlayers()){
            Thread th = new Thread(() -> {
                p.getShipBoard().fillShipboard(p, gameData.getPIRHandler());
            });
            th.start();
            threads.add(th);
        }

        for(Thread th : threads){
            th.join();
        }
    }


}
