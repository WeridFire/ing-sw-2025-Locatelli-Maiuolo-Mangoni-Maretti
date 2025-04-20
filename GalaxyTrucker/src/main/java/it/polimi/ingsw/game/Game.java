package it.polimi.ingsw.game;

import it.polimi.ingsw.TilesFactory;
import it.polimi.ingsw.cards.Card;
import it.polimi.ingsw.cards.Deck;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.game.exceptions.PlayerAlreadyInGameException;
import it.polimi.ingsw.gamePhases.AdventureGamePhase;
import it.polimi.ingsw.gamePhases.AssembleGamePhase;
import it.polimi.ingsw.gamePhases.LobbyGamePhase;
import it.polimi.ingsw.network.GameServer;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.player.exceptions.NoShipboardException;
import it.polimi.ingsw.player.exceptions.TooManyItemsInHandException;
import it.polimi.ingsw.playerInput.PIRs.PIRDelay;
import it.polimi.ingsw.shipboard.ShipBoard;
import it.polimi.ingsw.shipboard.exceptions.AlreadyEndedAssemblyException;
import it.polimi.ingsw.shipboard.tiles.TileSkeleton;
import it.polimi.ingsw.util.GameLevelStandards;

import java.rmi.RemoteException;
import java.util.*;

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
        //*******//
        // LOBBY
        System.out.println(this + " In lobby");

        LobbyGamePhase lobby = new LobbyGamePhase(id, gameData);
        getGameData().setCurrentGamePhase(lobby);
        lobby.playLoop();

        //Call function to initialize all players stuff.
        System.out.println(this + " Initialization");
        initGame();

        //**********//
        // ASSEMBLE
        System.out.println(this + " Started assemble phase");

        AssembleGamePhase assemble = new AssembleGamePhase(id, gameData);
        getGameData().setCurrentGamePhase(assemble);

        //We notify all players about the new game state
        GameServer.getInstance().broadcastUpdate(this);

        assemble.playLoop();
        System.out.println(this + " Ended assemble phase");
        // if here can be because time ended: force all the players that haven't finished yet to end assembly
        for (Player player : getGameData().getPlayers()) {
            if (!player.getShipBoard().isEndedAssembly()) {
                try {
                    getGameData().endAssembly(player, true);
                    System.out.println(this + " Forced end assemble for player '" + player.getUsername() + "'");
                } catch (AlreadyEndedAssemblyException | NoShipboardException | TooManyItemsInHandException e) {
                    throw new RuntimeException(e);  // should never happen -> runtime exception
                }
            }
        }

        // Blocking function that waits for everyone to finish set up their shipboard aliens
        System.out.println(this + " Started filling the shipboards");
        fillUpShipboards();
        System.out.println(this + " Filled all the shipboards");

        //********//
        // FLIGHT
        System.out.println(this + " Started flight phase");

        // prepare the deck
        gameData.getDeck().mixGroupsIntoCards();
        // manage adventures
        Card currentAdventureCard = gameData.getDeck().drawNextCard();
        AdventureGamePhase adventureGamePhase;
        while (currentAdventureCard != null) {
            // create adventure
            adventureGamePhase = new AdventureGamePhase(id, gameData, currentAdventureCard);
            getGameData().setCurrentGamePhase(adventureGamePhase);
            // notify all the players about the new adventure card
            notifyAdventureToPlayers(gameData.getPlayers().getFirst(), currentAdventureCard);
            // play the adventure
            adventureGamePhase.playLoop();
            // prepare next adventure
            currentAdventureCard = gameData.getDeck().drawNextCard();
        }

        System.out.println(this + " Ended flight phase");

        //TODO: vogliamo fare una fase di ending? Yes: show winner and points (maybe a static leaderboard)

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
                p.getShipBoard().fill(p, gameData.getPIRHandler());
                try {
                    GameServer.getInstance().broadcastUpdateRefreshOnly(this, Set.of(p));
                    // TODO: issue on refresh of the shipboard to empty crew when just main cabin
                } catch (RemoteException e) {
                    throw new RuntimeException(e);  // TODO: manage RemoteException at this level
                }
            });
            th.start();
            threads.add(th);
        }

        for(Thread th : threads){
            th.join();
        }
    }

    /**
     * Blocking function that will wait for all the players to get notified about a new adventure card drawn
     * by the leader. Will return once everyone has pressed [Enter] or the cooldown ended for all.
     */
    private void notifyAdventureToPlayers(Player leader, Card card) throws InterruptedException {
        String leaderName = leader.toColoredString("[", "]");
        gameData.getPIRHandler().broadcastPIR(this, (player, pirHandler) -> {
            PIRDelay pirDelay = new PIRDelay(player, 6,
                    "The leader " + leaderName + " has drawn a new Adventure Card:",
                    card);
            pirHandler.setAndRunTurn(pirDelay);
        });
    }


    @Override
    public String toString() {
        return "[Game " + id + "]";
    }
}
