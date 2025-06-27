package it.polimi.ingsw.model.game;

import it.polimi.ingsw.GamesHandler;
import it.polimi.ingsw.TilesFactory;
import it.polimi.ingsw.model.cards.Card;
import it.polimi.ingsw.model.cards.Deck;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.model.game.exceptions.ColorAlreadyInUseException;
import it.polimi.ingsw.model.game.exceptions.GameAlreadyRunningException;
import it.polimi.ingsw.model.game.exceptions.PlayerAlreadyInGameException;
import it.polimi.ingsw.model.gamePhases.AdventureGamePhase;
import it.polimi.ingsw.model.gamePhases.AssembleGamePhase;
import it.polimi.ingsw.model.gamePhases.LobbyGamePhase;
import it.polimi.ingsw.model.gamePhases.ScoreGamePhase;
import it.polimi.ingsw.model.gamePhases.exceptions.IllegalStartingPositionIndexException;
import it.polimi.ingsw.network.GameServer;
import it.polimi.ingsw.network.messages.ClientUpdate;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.exceptions.NoShipboardException;
import it.polimi.ingsw.model.player.exceptions.TooManyItemsInHandException;
import it.polimi.ingsw.model.playerInput.PIRUtils;
import it.polimi.ingsw.model.shipboard.ShipBoard;
import it.polimi.ingsw.model.shipboard.exceptions.AlreadyEndedAssemblyException;
import it.polimi.ingsw.model.gamePhases.exceptions.AlreadyPickedPosition;
import it.polimi.ingsw.model.shipboard.tiles.MainCabinTile;
import it.polimi.ingsw.model.shipboard.tiles.TileSkeleton;

import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Represents a game instance with a unique identifier, game data, and a timer.
 */
public class Game {

    ScheduledExecutorService schedulerPeriodicSaver = Executors.newSingleThreadScheduledExecutor();
    private transient Thread gameThread;

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
        id = resumeGame.getGameId();
        loadGameData(resumeGame);
    }

    /**
     * Creates a new game, with a new game.
     */
    public Game(){
        id = UUID.randomUUID();
        loadGameData(new GameData(id));
    }

    public Thread getGameThread() {
        return gameThread;
    }

    public void setGameThread(Thread gameThread) {
        this.gameThread = gameThread;
        gameThread.start();
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
    public GameData getGameData() {
        return gameData;
    }

    /**
     * Perform the Lobby phase of this game
     * @return {@code true} if lobby has been completed successfully, {@code false} otherwise
     */
    private boolean playLobby() throws RemoteException {
        // play lobby only after no game phase has been started
        if (getGameData().getCurrentGamePhaseType() != GamePhaseType.NONE) {
            return true;
        }

        //*******//
        // LOBBY
        System.out.println(this + " In lobby");

        LobbyGamePhase lobby = new LobbyGamePhase(gameData);
        getGameData().setCurrentGamePhase(lobby);
        try {
            lobby.playLoop();
        } catch (InterruptedException e) {
            return false;
        }

        // call function to initialize all players stuff
        System.out.println(this + " Initialization");
        initGame();

        return true;
    }

    private void startPeriodicSave() {
        schedulerPeriodicSaver.scheduleAtFixedRate(gameData::saveGameState, 0, 15, TimeUnit.SECONDS);
    }

    private void stopPeriodicSave() {
        schedulerPeriodicSaver.shutdown();
        try {
            if (!schedulerPeriodicSaver.awaitTermination(1, TimeUnit.SECONDS)) {
                schedulerPeriodicSaver.shutdownNow();
            }
        } catch (InterruptedException e) {
            schedulerPeriodicSaver.shutdownNow();
        }
    }

    private AssembleGamePhase retrieveAssembleGamePhase(GamePhaseType currentGamePhaseType) {
        Runnable onTimerSwitch = () -> {
            // notify all players about the new game state with an expired timer
            try {
                GameServer.getInstance().broadcastUpdate(this);
            } catch (RemoteException e) {
                // ignore exception since there is no other way to notify the players
            }
        };
        AssembleGamePhase assemble;
        if (currentGamePhaseType == GamePhaseType.ASSEMBLE) {  // resume previous assemble
            assemble = new AssembleGamePhase((AssembleGamePhase) gameData.getCurrentGamePhase(), onTimerSwitch);
        } else {  // create new assemble
            assemble = new AssembleGamePhase(gameData, onTimerSwitch);
        }
        return assemble;
    }

    /**
     * Perform the Assemble phase of this game
     * @return {@code true} if assemble has been completed successfully, {@code false} otherwise
     */
    private boolean playAssemble() throws RemoteException {
        // play assemble only after lobby or from previous assemble
        GamePhaseType currentGamePhaseType = getGameData().getCurrentGamePhaseType();
        if (currentGamePhaseType != GamePhaseType.LOBBY && currentGamePhaseType != GamePhaseType.ASSEMBLE) {
            return true;
        }

        //**********//
        // ASSEMBLE
        System.out.println(this + " Started assemble phase");

        // create schedule to save game in assemble every tot seconds
        startPeriodicSave();

        AssembleGamePhase assemble = retrieveAssembleGamePhase(currentGamePhaseType);
        getGameData().setCurrentGamePhase(assemble);

        // notify all players about the new game state
        GameServer.getInstance().broadcastUpdate(this);

        // actually run assemble
        try {
            assemble.playLoop();
        } catch (InterruptedException e) {
            stopPeriodicSave();
            return false;
        }

        // end periodic save
        stopPeriodicSave();

        System.out.println(this + " Ended assemble phase");
        // if here can be because time ended: force all the players that haven't finished yet to end assembly
        for (Player player : getGameData().getPlayers()) {
            if (!player.getShipBoard().isEndedAssembly()) {
                try {
                    getGameData().endAssembly(player, true, null);
                    System.out.println(this + " Forced end assemble for player '" + player.getUsername() + "'");
                } catch (AlreadyEndedAssemblyException | NoShipboardException | TooManyItemsInHandException |
                         AlreadyPickedPosition | IllegalStartingPositionIndexException e) {
                    throw new RuntimeException(e);  // should never happen -> runtime exception
                }
            }
        }

        // Blocking function that waits for everyone to finish set up their shipboard aliens
        System.out.println(this + " Started filling the shipboards");
        try {
            fillUpShipboards();
        } catch (InterruptedException e) {
            return false;  // no need to do anything: simply don't save
        }
        //resetting visitor after fillup is necessary to recalculate fire powers.
        gameData.getPlayers().forEach(p -> p.getShipBoard().resetVisitors());
        System.out.println(this + " Filled all the shipboards");

        gameData.getPIRHandler().joinEndTurn(gameData.getPlayers());


        return true;
    }

    /**
     * Perform the Flight phase of this game
     * @return {@code true} if flight has been completed successfully, {@code false} otherwise
     */
    private boolean playFlight() throws RemoteException {
        // play flight only after assemble, or after another adventure... (*1)
        GamePhaseType currentGamePhaseType = getGameData().getCurrentGamePhaseType();
        if (currentGamePhaseType != GamePhaseType.ASSEMBLE && currentGamePhaseType != GamePhaseType.ADVENTURE) {
            return true;
        }

        //********//
        // FLIGHT
        System.out.println(this + " Started flight phase");

        // (*1) ... in that case: no need to prepare the deck -> only prepare if the game is arriving from assemble
        if (currentGamePhaseType == GamePhaseType.ASSEMBLE) {
            // prepare the deck
            gameData.getDeck().mixGroupsIntoCards();
        }

        // manage adventures
        Card currentAdventureCard = gameData.getDeck().drawNextCard();
        AdventureGamePhase adventureGamePhase;
        while (currentAdventureCard != null) {
            // endgame if 0 players are flying.
            // we don't check for <= 1 players connected because the game still progresses in this case.
            if (getGameData().getPlayersInFlight().isEmpty()) {
                System.out.println(this + " Zero alive players... Exiting game.");
                GameServer.getInstance().broadcastUpdateRefreshOnlyIf(this, _ -> false);
                return true;
            }

            // create adventure
            adventureGamePhase = new AdventureGamePhase(gameData, currentAdventureCard);
            getGameData().setCurrentGamePhase(adventureGamePhase);
            gameData.saveGameState();
            // play the adventure
            try {
                adventureGamePhase.playLoop();
            } catch (InterruptedException e) {
                return false;
            }

            // revalidate structure at the end of each adventure for non-notified-yet problems (e.g. 0 crew)
            List<Player> playersInFlight = getGameData().getPlayersInFlight();
            for (Player player : playersInFlight) {
                player.getShipBoard().validateStructure();
            }
            gameData.getPIRHandler().joinEndTurn(playersInFlight);

            // end flight for players that requested it
            for (Player player : getGameData().getPlayers(Player::hasRequestedEndFlight)) {
                player.endFlight();
            }

            gameData.saveGameState();

            // prepare next adventure
            currentAdventureCard = gameData.getDeck().drawNextCard();
        }

        System.out.println(this + " Ended flight phase");
        return true;
    }

    private void playEndgame() {
        System.out.println(this + " Started scoring phase");

        //********//
        // SCORE SCREEN
        ScoreGamePhase scoreScreenGamePhase = new ScoreGamePhase(gameData);
        getGameData().setCurrentGamePhase(scoreScreenGamePhase);
        try {
            scoreScreenGamePhase.playLoop();
        } catch (InterruptedException e) {
            return;
        }

        // Delete game save file to prevent the resuming of a finished game.
        GamesHandler.deleteGameSave(getId());

        // Disconnect all players. Notify of the disconnection.
        getGameData().getPlayers(Player::isConnected).forEach((p) -> {
            UUID connectionUUID = p.getConnectionUUID();
            p.disconnect();
			try {
				GameServer.getInstance()
                        .getClient(connectionUUID)
                        .updateClient(new ClientUpdate(connectionUUID, true));
			} catch (RemoteException e) {
				// player may have disconnected, will let the gameserver discover it.
			}
		});

        System.out.println(this + " Ended scoring phase.");

        stopGame();
    }

    /**
     * Starts and manages the game loop.
     */
    public void gameLoop() throws RemoteException {
        boolean continueLoop = playLobby();
        if (continueLoop) continueLoop = playAssemble();
        if (continueLoop) continueLoop = playFlight();
        if (continueLoop) playEndgame();
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

    /**
     * Create a Player with the specified username, assign it the specified client connection UUID and return it.
     * If this game has already started check that the username matches among those of previously connected players,
     * and if so also whether that player actually disconnected.
     *
     * @param username the new player's unique (for this game) name
     * @param connectionUUID the connection UUID of the client associated to the new player
     * @param desiredColor the color this player wants to use for the game.
     * Note: if the player to add is trying to reconnect, the desired color is ignored and the previous is kept.
     *
     * @return the created player
     *
     * @throws PlayerAlreadyInGameException if the player is already in this game
     * @throws GameAlreadyRunningException if this instance of game is not in main menu nor lobby and the username is
     * NOT of a disconnected player.
     * @throws ColorAlreadyInUseException if the desired color is already taken by another player.
     */
    public Player addPlayer(String username, UUID connectionUUID, MainCabinTile.Color desiredColor)
            throws PlayerAlreadyInGameException, GameAlreadyRunningException, ColorAlreadyInUseException {
        GamePhaseType currentGamePhaseType = getGameData().getCurrentGamePhaseType();
        if(currentGamePhaseType == GamePhaseType.ENDGAME){
            throw new GameAlreadyRunningException("Attempted to join a game that has already started.");
        }

        if (currentGamePhaseType == GamePhaseType.NONE || currentGamePhaseType == GamePhaseType.LOBBY) {
            if (gameData.getPlayer(p -> p.getColor().equals(desiredColor)) != null) {
                throw new ColorAlreadyInUseException(desiredColor);
            }
            Player newPlayer = new Player(username, connectionUUID, desiredColor);
            gameData.addPlayer(newPlayer);
            return newPlayer;
        }

        Player existingDisconnectedPlayer = gameData.getPlayer(p ->
                p.getUsername().equals(username) && !p.isConnected());
        if (existingDisconnectedPlayer == null) {
            throw new GameAlreadyRunningException("Attempted to join a game that has already started!");
        } else {
            existingDisconnectedPlayer.setConnectionUUID(connectionUUID);
            return existingDisconnectedPlayer;
        }
    }

    public void initGame(){
        //After the lobby phase has ended, we initialize the game.

        List<TileSkeleton> t = TilesFactory.createPileTiles();
        Collections.shuffle(t);

        //assign numeric progressive id to shuffled tiles
        for (int i = 0; i < t.size(); i++) {
            t.get(i).setTileId(i);
        }

        gameData.setCoveredTiles(t);

        gameData.setDeck(Deck.random(gameData.getLevel()));

        for (Player player : gameData.getPlayers()) {
            ShipBoard shipBoard = ShipBoard.create(gameData.getLevel(), player.getColor());
            shipBoard.attachIntegrityListener(new PIRUtils.ShipIntegrityListener(player, this));
            player.setShipBoard(shipBoard);
        }
    }

    /**
     * Blocking function that will wait for all the players to fill up their shipboard in non-obvious cases
     * through multiple PIRs choice, in parallel on new threads. Will return once everyone has filled up their
     */
    private void fillUpShipboards() throws InterruptedException {
        gameData.getPIRHandler().broadcastPIR(
                gameData.getPlayers(),
                (player, pirHandler) ->
                player.getShipBoard().fill(player, pirHandler));
    }

    /**
     * Calls an interrupts on the game thread, making it basically stop as soon as the game halts.
     * Removes the list from the existing games list in the game handler.
     */
    public void stopGame(){
        if(getGameThread() != null){
            getGameThread().interrupt();
        }
        GamesHandler.getInstance().getGames().remove(this);
        getGameData().getPlayers(Player::isConnected).forEach(p -> {
            UUID clientId = p.getConnectionUUID();
            p.disconnect();
			try {
				GameServer.getInstance().getClient(clientId).updateClient(new ClientUpdate(clientId, true));
			} catch (RemoteException e) {
				System.out.println("Could not update client " + clientId + " after ending a game.");
			}
		});
    }

    public void disconnectPlayer(Player player){
        if(gameData.getCurrentGamePhase().getGamePhaseType() == GamePhaseType.LOBBY){
            //If we are in lobby, just remove the player.
            gameData.getUnorderedPlayers().remove(player);
            if(gameData.getGameLeader().equals(player.getUsername()) &&
                !gameData.getPlayers().isEmpty()){
                //The leader has quit. If there are more players, the first one in the list becomes the new leader.
                gameData.setGameLeader(gameData.getPlayers().getFirst().getUsername());
            }
        }else{
            player.disconnect();
        }

        if(gameData.getUnorderedPlayers().isEmpty() ||
            gameData.getUnorderedPlayers().stream().noneMatch(Player::isConnected)){
            //stop the main thread.
            stopGame();
        }
    }


    @Override
    public String toString() {
        return "[Game " + id + "]";
    }
}
