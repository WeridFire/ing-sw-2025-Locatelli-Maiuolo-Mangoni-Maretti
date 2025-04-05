package it.polimi.ingsw.game;

import it.polimi.ingsw.cards.Deck;
import it.polimi.ingsw.enums.GameLevel;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.game.exceptions.PlayerAlreadyInGameException;
import it.polimi.ingsw.gamePhases.PlayableGamePhase;
import it.polimi.ingsw.gamePhases.exceptions.TimerIsAlreadyRunningException;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.shipboard.exceptions.ThatTileIdDoesNotExistsException;
import it.polimi.ingsw.shipboard.tiles.TileSkeleton;
import it.polimi.ingsw.shipboard.visitors.TileVisitor;
import it.polimi.ingsw.view.cli.CLIFrame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.TestDescriptor;

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
        assertEquals(GamePhaseType.LOBBY, gameData.getCurrentGamePhaseType());
        assertTrue(gameData.getPlayers().isEmpty());
        assertEquals(2, gameData.getRequiredPlayers());
        assertNull(gameData.getGameLeader());
        assertNotNull(gameData.getPIRHandler());
        assertTrue(gameData.getCoveredTiles().isEmpty());
        assertNull(gameData.getDeck());
    }

    @Test
    void testSetLevel() {
        gameData.setLevel(GameLevel.ONE);
        assertEquals(GameLevel.ONE, gameData.getLevel());

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
        PlayableGamePhase mockPhase = new PlayableGamePhase(gameId, GamePhaseType.ADVENTURE, gameData) {
            @Override
            public void playLoop() {}

            /**
             * Used to implement starting timer logic
             */
            @Override
            public void startTimer() throws TimerIsAlreadyRunningException {

            }
        };

        gameData.setCurrentGamePhase(mockPhase);
        assertEquals(mockPhase, gameData.getCurrentGamePhase());
        assertEquals(GamePhaseType.ADVENTURE, gameData.getCurrentGamePhaseType());
    }

    @Test
    void testAddPlayer() throws PlayerAlreadyInGameException {
        Player player1 = new Player("Player1", UUID.randomUUID());
        Player player2 = new Player("Player2", UUID.randomUUID());

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
        Player player = new Player("Player", UUID.randomUUID());

        assertDoesNotThrow(() -> gameData.addPlayer(player));

        // Try to add the same player again
        assertThrows(PlayerAlreadyInGameException.class, () -> gameData.addPlayer(player));

        // Try to add a different player with the same username
        Player sameNamePlayer = new Player("Player", UUID.randomUUID());
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
    void testGetDrawnTiles() {
        // TODO: trova un modo per testarlo inizializzando il game
        List<TileSkeleton> drawnTiles = gameData.getDrawnTiles();
        assertNotNull(drawnTiles);
        assertTrue(drawnTiles.isEmpty());
    }

    @Test
    void testSetLapSize() {
        gameData.setLapSize(18);
        assertEquals(18, getLapSize(gameData));

        gameData.setLapSize(24);
        assertEquals(24, getLapSize(gameData));
    }

    @Test
    void testMovePlayerForward() throws PlayerAlreadyInGameException {
        Player player = new Player("Player", UUID.randomUUID());
        player.setPosition(5);
        gameData.addPlayer(player);

        gameData.movePlayerForward(player, 3);
        assertEquals(8, player.getPosition());
    }

    @Test
    void testMovePlayerBackward() throws PlayerAlreadyInGameException {
        Player player = new Player("Player", UUID.randomUUID());
        player.setPosition(5);
        gameData.addPlayer(player);

        gameData.movePlayerBackward(player, 3);
        assertEquals(2, player.getPosition());
    }

    @Test
    void testPlayerMovementWithOtherPlayers() throws PlayerAlreadyInGameException {
        Player player1 = new Player("Player1", UUID.randomUUID());
        Player player2 = new Player("Player2", UUID.randomUUID());
        Player player3 = new Player("Player3", UUID.randomUUID());

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

    private int getLapSize(GameData gameData) {
        try {
            java.lang.reflect.Field field = GameData.class.getDeclaredField("lapSize");
            field.setAccessible(true);
            return (int) field.get(gameData);
        } catch (Exception e) {
            return -1;
        }
    }

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
        return new TileSkeleton(null) {
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
        };
    }

    private Deck createMockDeck() {
        return new Deck(GameLevel.TESTFLIGHT);
    }

}