package it.polimi.ingsw.model.game;

import it.polimi.ingsw.GamesHandler;
import it.polimi.ingsw.model.cards.Deck;
import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.model.game.exceptions.ColorAlreadyInUseException;
import it.polimi.ingsw.model.gamePhases.AssembleGamePhase;
import it.polimi.ingsw.model.gamePhases.exceptions.AlreadyPickedPosition;
import it.polimi.ingsw.model.game.exceptions.GameAlreadyRunningException;
import it.polimi.ingsw.model.game.exceptions.PlayerAlreadyInGameException;
import it.polimi.ingsw.model.gamePhases.PlayableGamePhase;
import it.polimi.ingsw.model.gamePhases.exceptions.IllegalStartingPositionIndexException;
import it.polimi.ingsw.model.gamePhases.exceptions.TimerIsAlreadyRunningException;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.player.exceptions.NoShipboardException;
import it.polimi.ingsw.model.player.exceptions.TooManyItemsInHandException;
import it.polimi.ingsw.model.shipboard.SideType;
import it.polimi.ingsw.model.shipboard.exceptions.*;
import it.polimi.ingsw.model.shipboard.tiles.MainCabinTile;
import it.polimi.ingsw.model.shipboard.tiles.TileSkeleton;
import it.polimi.ingsw.model.shipboard.tiles.exceptions.FixedTileException;
import it.polimi.ingsw.model.shipboard.visitors.TileVisitor;
import it.polimi.ingsw.view.cli.CLIFrame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.TestDescriptor;

import java.rmi.RemoteException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class GameDataTest {

    private GameData gameData;
    private UUID gameId;

    @BeforeEach
    void setUp() {
        gameId = UUID.randomUUID();
        gameData = new GameData(gameId);
    }

    @Test
    void testConstructor() {
        assertEquals(gameId, gameData.getGameId());
        assertEquals(GameLevel.TESTFLIGHT, gameData.getLevel());
        assertEquals(GamePhaseType.NONE, gameData.getCurrentGamePhaseType());
        assertTrue(gameData.getPlayers().isEmpty());
        assertEquals(2, gameData.getRequiredPlayers());
        assertNull(gameData.getGameLeader());
        assertNotNull(gameData.getPIRHandler());
        assertTrue(gameData.getCoveredTiles().isEmpty());
        assertNull(gameData.getDeck());
    }

    @Test
    void testSetLevel() {
        GameLevel prevGL = gameData.getLevel();
        gameData.setLevel(GameLevel.ONE);
        assertEquals(prevGL, gameData.getLevel());
        // does not change because level ONE is not compliant

        gameData.setLevel(GameLevel.TWO);
        assertEquals(GameLevel.TWO, gameData.getLevel());
    }

    @Test
    void testSetGamePhaseType() {
        gameData.setCurrentGamePhaseType(GamePhaseType.ASSEMBLE);
        assertEquals(GamePhaseType.ASSEMBLE, gameData.getCurrentGamePhaseType());

        gameData.setCurrentGamePhaseType(GamePhaseType.ADVENTURE);
        assertEquals(GamePhaseType.ADVENTURE, gameData.getCurrentGamePhaseType());

        gameData.setCurrentGamePhaseType(GamePhaseType.ENDGAME);
        assertEquals(GamePhaseType.ENDGAME, gameData.getCurrentGamePhaseType());
    }

    @Test
    void testSetCurrentGamePhase() {
        PlayableGamePhase mockPhase = new PlayableGamePhase(GamePhaseType.ADVENTURE, gameData) {
            @Override
            public void playLoop() {}

            /**
             * Used to implement starting timer logic
             */
            @Override
            public void startTimer(Player p) throws TimerIsAlreadyRunningException {

            }
        };

        gameData.setCurrentGamePhase(mockPhase);
        assertEquals(mockPhase, gameData.getCurrentGamePhase());
        assertEquals(GamePhaseType.ADVENTURE, gameData.getCurrentGamePhaseType());
    }

    @Test
    void testResumeGameInAssembly() throws AlreadyPickedPosition, AlreadyEndedAssemblyException, NoShipboardException, FixedTileException, TileAlreadyPresentException, TileWithoutNeighborException, RemoteException, InterruptedException, PlayerAlreadyInGameException, OutOfBuildingAreaException, GameAlreadyRunningException, TooManyItemsInHandException, IllegalStartingPositionIndexException, ColorAlreadyInUseException {
        UUID gameId = runAndSaveGameUntilStep(0);
        Thread.sleep(1000);
        Game g = GamesHandler.getInstance().getGame(gameId);

        assertNotNull(g);

        GameData gameData = GameData.loadFromState(gameId);

        assertThrows(GameAlreadyRunningException.class,
                    () ->  GamesHandler.getInstance().resumeGame(gameData, UUID.randomUUID()));

        g.stopGame();

        GamesHandler.getInstance().resumeGame(gameData, UUID.randomUUID());
    }


    private void stopAndSaveGame(GameData gameData) {
        for (Player p : gameData.getPlayers()) {
            p.disconnect();
        }
        gameData.saveGameState();
    }

    /**
     * Step -1: stops in lobby (shouldnt serialize a game in lobby)
     * Step 0: stops at initial assembly phase.
     * Step 1: stops with built shipboards
     * Step 2+: stops with adventure phase started
     * @param step
     * @return
     */
    UUID runAndSaveGameUntilStep(int step) throws AlreadyPickedPosition, AlreadyEndedAssemblyException, FixedTileException, TileAlreadyPresentException, TileWithoutNeighborException, RemoteException, OutOfBuildingAreaException, TooManyItemsInHandException, NoShipboardException, InterruptedException, PlayerAlreadyInGameException, IllegalStartingPositionIndexException, GameAlreadyRunningException, ColorAlreadyInUseException {
        Game g = GamesHandler.getInstance().createGame("Pippo", UUID.randomUUID(), MainCabinTile.Color.BLUE);
        UUID gameId = g.getId();
        GameData gameData = g.getGameData();

        Player player1 = gameData.getPlayer(p -> Objects.equals(p.getUsername(), gameData.getGameLeader()));
        assertThrows(ColorAlreadyInUseException.class, () ->
                g.addPlayer("Player2", UUID.randomUUID(), MainCabinTile.Color.BLUE));
        Player player2 = g.addPlayer("Player2", UUID.randomUUID(), MainCabinTile.Color.RED);

        if(step == -1){
            stopAndSaveGame(gameData);
            return gameId;
        }

        g.testInitGame();
        gameData.setCurrentGamePhase(new AssembleGamePhase(gameId, gameData));

        if(step == 0){
            stopAndSaveGame(gameData);
            return gameId;
        }

        Thread.sleep(1000);

        Cheats.cheatStandardShipboard(g.getGameData(), player1);
        Cheats.cheatStandardShipboard(g.getGameData(), player2);
        if(step == 1){
            stopAndSaveGame(gameData);
            return gameId;
        }

        gameData.endAssembly(player1, false, 1);
        gameData.endAssembly(player2, false, 2);

        Thread.sleep(2000);
        if(step == 2){
            stopAndSaveGame(gameData);
            return gameId;
        }


        stopAndSaveGame(gameData);
        return gameId;
    }

    @Test
    void testAddPlayer() throws PlayerAlreadyInGameException {
        Player player1 = new Player("Player1", UUID.randomUUID(), MainCabinTile.Color.BLUE);
        Player player2 = new Player("Player2", UUID.randomUUID(), MainCabinTile.Color.RED);

        gameData.addPlayer(player1);
        assertEquals(1, gameData.getPlayers().size());
        assertTrue(gameData.getPlayers().contains(player1));
        assertEquals("Player1", gameData.getGameLeader());

        gameData.addPlayer(player2);
        assertEquals(2, gameData.getPlayers().size());
        assertTrue(gameData.getPlayers().contains(player2));
        assertEquals("Player1", gameData.getGameLeader()); // Leader should remain the first player
    }

    @Test
    void testAddDuplicatePlayer() {
        Player player = new Player("Player", UUID.randomUUID(), MainCabinTile.Color.BLUE);

        assertDoesNotThrow(() -> gameData.addPlayer(player));

        // Try to add the same player again
        assertThrows(PlayerAlreadyInGameException.class, () -> gameData.addPlayer(player));

        // Try to add a different player with the same username
        Player sameNamePlayer = new Player("Player", UUID.randomUUID(), MainCabinTile.Color.RED);
        assertThrows(PlayerAlreadyInGameException.class, () -> gameData.addPlayer(sameNamePlayer));
    }

    @Test
    void testSetRequiredPlayers() {
        gameData.setRequiredPlayers(3);
        assertEquals(3, gameData.getRequiredPlayers());

        gameData.setRequiredPlayers(4);
        assertEquals(4, gameData.getRequiredPlayers());

        // Test bounds
        gameData.setRequiredPlayers(5); // Above max (4)
        assertEquals(4, gameData.getRequiredPlayers()); // Should remain 4

        gameData.setRequiredPlayers(1); // Below min (2)
        assertEquals(4, gameData.getRequiredPlayers()); // Should remain 4
    }

    @Test
    void testSetCoveredTiles() {
        List<TileSkeleton> mockTiles = new ArrayList<>();
        TileSkeleton mockTile1 = createMockTile(1);
        TileSkeleton mockTile2 = createMockTile(2);
        mockTiles.add(mockTile1);
        mockTiles.add(mockTile2);

        gameData.setCoveredTiles(mockTiles);
        assertEquals(2, gameData.getCoveredTiles().size());
        assertTrue(gameData.getCoveredTiles().contains(mockTile1));
        assertTrue(gameData.getCoveredTiles().contains(mockTile2));
    }

    @Test
    void testSetDeck() {
        Deck mockDeck = createMockDeck();

        gameData.setDeck(mockDeck);
        assertEquals(mockDeck, gameData.getDeck());
    }

    @Test
    void testGetUncoveredTiles() {
        // TODO: trova un modo per testarlo inizializzando il game
        List<TileSkeleton> drawnTiles = gameData.getUncoveredTiles();
        assertNotNull(drawnTiles);
        assertTrue(drawnTiles.isEmpty());
    }

    @Test
    void testMovePlayerForward() throws PlayerAlreadyInGameException {
        Player player = new Player("Player", UUID.randomUUID(), MainCabinTile.Color.BLUE);
        player.setPosition(5);
        gameData.addPlayer(player);

        gameData.movePlayerForward(player, 3);
        assertEquals(8, player.getPosition());
    }

    @Test
    void testMovePlayerBackward() throws PlayerAlreadyInGameException {
        Player player = new Player("Player", UUID.randomUUID(), MainCabinTile.Color.BLUE);
        player.setPosition(5);
        gameData.addPlayer(player);

        gameData.movePlayerBackward(player, 3);
        assertEquals(2, player.getPosition());
    }

    @Test
    void testPlayerMovementWithOtherPlayers() throws PlayerAlreadyInGameException {
        Player player1 = new Player("Player1", UUID.randomUUID(), MainCabinTile.Color.BLUE);
        Player player2 = new Player("Player2", UUID.randomUUID(), MainCabinTile.Color.RED);
        Player player3 = new Player("Player3", UUID.randomUUID(), MainCabinTile.Color.GREEN);

        player1.setPosition(5);
        player2.setPosition(7); // Player on the path
        player3.setPosition(3); // Player behind

        gameData.addPlayer(player1);
        gameData.addPlayer(player2);
        gameData.addPlayer(player3);

        gameData.movePlayerForward(player1, 3);
        assertEquals(9, player1.getPosition()); // Should be 5+3+1=9 (skipped player2)

        // Now move player1 backward
        gameData.movePlayerBackward(player1, 3);
        assertEquals(5, player1.getPosition()); // Should be 9-3-1=5 (skipped player2)
    }

    @Test
    void testGetTileWithId() {
        // TODO: trova un modo per testarlo inizializzando il game
        // fail perche faila il test 2

        List<TileSkeleton> mockTiles = new ArrayList<>();
        TileSkeleton tile1 = createMockTile(1);
        TileSkeleton tile2 = createMockTile(2);
        mockTiles.add(tile1);
        mockTiles.add(tile2);

        gameData.setCoveredTiles(mockTiles);

        // Access using reflection since method is probably package-private
        assertThrows(ThatTileIdDoesNotExistsException.class, () -> {
            try {
                getTileWithId(gameData, 3); // Non-existent ID
            } catch (Exception e) {
                throw e.getCause();
            }
        });
    }

    // Helper methods to access private methods/fields

    private TileSkeleton getTileWithId(GameData gameData, int id) throws Exception {
        try {
            java.lang.reflect.Method method = GameData.class.getDeclaredMethod("getTileWithId", Integer.class);
            method.setAccessible(true);
            return (TileSkeleton) method.invoke(gameData, id);
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw e;
        }
    }

    // Mock object creation helpers

    private TileSkeleton createMockTile(int id) {
        return new TileSkeleton(Direction.sortedArray(SideType.UNIVERSAL, SideType.UNIVERSAL,
                SideType.UNIVERSAL, SideType.UNIVERSAL).toArray(SideType[]::new)) {
            /**
             * Method to accept a visitor for this tile in the Visitor Pattern.
             *
             * @param visitor The visitor that will visit this tile.
             */
            @Override
            public void accept(TileVisitor visitor) {

            }

            public void accept(TestDescriptor.Visitor visitor) {}

            @Override
            public CLIFrame getCLIRepresentation() {
                return null;
            }

            {
                setTileId(id);
            }

            @Override
            public String getName() {
                return "";
            }
        };
    }

    private Deck createMockDeck() {
        return Deck.random(GameLevel.TESTFLIGHT);
    }

}