package it.polimi.ingsw.game;

import it.polimi.ingsw.GamesHandler;
import it.polimi.ingsw.TilesFactory;
import it.polimi.ingsw.cards.Card;
import it.polimi.ingsw.cards.Deck;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.game.exceptions.ColorAlreadyInUseException;
import it.polimi.ingsw.game.exceptions.GameAlreadyRunningException;
import it.polimi.ingsw.game.exceptions.PlayerAlreadyInGameException;
import it.polimi.ingsw.gamePhases.AdventureGamePhase;
import it.polimi.ingsw.gamePhases.AssembleGamePhase;
import it.polimi.ingsw.gamePhases.LobbyGamePhase;
import it.polimi.ingsw.gamePhases.ScoreScreenGamePhase;
import it.polimi.ingsw.gamePhases.exceptions.IllegalStartingPositionIndexException;
import it.polimi.ingsw.network.GameServer;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.player.exceptions.NoShipboardException;
import it.polimi.ingsw.player.exceptions.TooManyItemsInHandException;
import it.polimi.ingsw.playerInput.PIRUtils;
import it.polimi.ingsw.playerInput.PIRs.PIRDelay;
import it.polimi.ingsw.shipboard.ShipBoard;
import it.polimi.ingsw.shipboard.exceptions.AlreadyEndedAssemblyException;
import it.polimi.ingsw.gamePhases.exceptions.AlreadyPickedPosition;
import it.polimi.ingsw.shipboard.tiles.MainCabinTile;
import it.polimi.ingsw.shipboard.tiles.TileSkeleton;

import java.rmi.RemoteException;
import java.util.*;

/**
 * Represents a game instance with a unique identifier, game data, and a timer.
 */
public class Game {


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

    private void playLobby() throws RemoteException, InterruptedException {
        // play lobby only after no game phase has been started
        if (getGameData().getCurrentGamePhaseType() != GamePhaseType.NONE) {
            return;
        }

        //*******//
        // LOBBY
        System.out.println(this + " In lobby");

        LobbyGamePhase lobby = new LobbyGamePhase(gameData);
        getGameData().setCurrentGamePhase(lobby);
        lobby.playLoop();

        // call function to initialize all players stuff
        System.out.println(this + " Initialization");
        initGame();
    }

    private void playAssemble() throws RemoteException, InterruptedException {
        // play assemble only after lobby
        if (getGameData().getCurrentGamePhaseType() != GamePhaseType.LOBBY) {
            return;
        }

        //**********//
        // ASSEMBLE
        System.out.println(this + " Started assemble phase");

        AssembleGamePhase assemble = new AssembleGamePhase(gameData, () -> {
            // notify all players about the new game state with an expired timer
            try {
                GameServer.getInstance().broadcastUpdate(this);
            } catch (RemoteException e) {
                // ignore exception since there is no other way to notify the players
            }
        });
        getGameData().setCurrentGamePhase(assemble);

        // notify all players about the new game state
        GameServer.getInstance().broadcastUpdate(this);

        assemble.playLoop();
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
        fillUpShipboards();
        System.out.println(this + " Filled all the shipboards");

        gameData.getPIRHandler().joinEndTurn(gameData.getPlayers());

        gameData.saveGameState();
    }

    private void playFlight() throws InterruptedException {
        // play flight only after assemble, or after another adventure... (*1)
        GamePhaseType currentGamePhaseType = getGameData().getCurrentGamePhaseType();
        if (currentGamePhaseType != GamePhaseType.ASSEMBLE && currentGamePhaseType != GamePhaseType.ADVENTURE) {
            return;
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

            // skip the rest if there are no players flying
            if(gameData.getPlayersInFlight().isEmpty()){
                break;
            }

            // create adventure
            adventureGamePhase = new AdventureGamePhase(gameData, currentAdventureCard);
            getGameData().setCurrentGamePhase(adventureGamePhase);
            // notify all the players about the new adventure card
            notifyAdventureToPlayers(gameData.getPlayersInFlight().getFirst(), currentAdventureCard);
            // play the adventure
            adventureGamePhase.playLoop();

            // end flight for players that requested it
            for (Player player : getGameData().getPlayers(Player::hasRequestedEndFlight)) {
                player.endFlight();
            }

            gameData.saveGameState();

            // prepare next adventure
            currentAdventureCard = gameData.getDeck().drawNextCard();
        }

        System.out.println(this + " Ended flight phase");
    }

    private void playEndgame() throws RemoteException, InterruptedException {
        // play end of the game only after adventures and when no other cards are in the deck
        if (getGameData().getCurrentGamePhaseType() != GamePhaseType.ADVENTURE
                || getGameData().getDeck().getCurrentCard() != null) {
            return;
        }

        //********//
        // SCORE SCREEN
        ScoreScreenGamePhase scoreScreenGamePhase;
        scoreScreenGamePhase = new ScoreScreenGamePhase(gameData);
        getGameData().setCurrentGamePhase(scoreScreenGamePhase);
        System.out.println(this + " Started scoring phase");
        scoreScreenGamePhase.playLoop();
        notifyScoresToPlayers(scoreScreenGamePhase);

        // TODO: end scoring phase
        // TODO: delete saved game state file to avoid memory issues
        // TODO: take in consideration to turn back to lobby with all the players instead of deleting the current game
    }

    /**
     * Starts and manages the game loop.
     */
    public void gameLoop() throws InterruptedException, RemoteException {
        playLobby();
        playAssemble();
        playFlight();
        playEndgame();
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

    private void initGame(){
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

    public void testInitGame() {
        initGame();
    }

    /**
     * Blocking function that will wait for all the players to fill up their shipboard in non-obvious cases
     * through multiple PIRs choice, in parallel on new threads. Will return once everyone has filled up their
     */
    private void fillUpShipboards() throws InterruptedException {
        gameData.getPIRHandler().broadcastPIR(gameData.getPlayers(), (player, pirHandler) ->
                player.getShipBoard().fill(player, pirHandler));
    }

    /**
     * Blocking function that will wait for all the players to get notified about a new adventure card drawn
     * by the leader. Will return once everyone has pressed [Enter] or the cooldown ended for all.
     */
    private void notifyAdventureToPlayers(Player leader, Card card) throws InterruptedException {
        String leaderName = leader.toColoredString("[", "]");
        gameData.getPIRHandler().broadcastPIR(gameData.getPlayers(), (player, pirHandler) -> {
            PIRDelay pirDelay = new PIRDelay(player, 6,
                    "The leader " + leaderName + " has drawn a new Adventure Card:",
                    card.getCLIRepresentation());
            pirHandler.setAndRunTurn(pirDelay);
        });
    }

    private void notifyScoresToPlayers(ScoreScreenGamePhase scoreScreen) throws InterruptedException {
        gameData.getPIRHandler().broadcastPIR(gameData.getPlayers(), (player, pirHandler) -> {
            PIRDelay pirDelay = new PIRDelay(player, 6,
                    "GG to all, match is over", scoreScreen.getCLIRepresentation());
            pirHandler.setAndRunTurn(pirDelay);
        });
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
