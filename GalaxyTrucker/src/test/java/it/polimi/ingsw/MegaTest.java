package it.polimi.ingsw;

import it.polimi.ingsw.model.cards.Deck;
import it.polimi.ingsw.model.cards.DeckFactory;
import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.enums.Rotation;
import it.polimi.ingsw.model.game.Game;
import it.polimi.ingsw.model.game.GameData;
import it.polimi.ingsw.network.GameClientMock;
import it.polimi.ingsw.network.GameServer;
import it.polimi.ingsw.network.exceptions.AlreadyRunningServerException;
import it.polimi.ingsw.model.shipboard.SideType;
import it.polimi.ingsw.model.shipboard.exceptions.NoTileFoundException;
import it.polimi.ingsw.model.shipboard.exceptions.OutOfBuildingAreaException;
import it.polimi.ingsw.model.shipboard.tiles.BatteryComponentTile;
import it.polimi.ingsw.model.shipboard.tiles.MainCabinTile;
import it.polimi.ingsw.model.shipboard.tiles.TileSkeleton;
import it.polimi.ingsw.util.Coordinates;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test suite for simulating and validating multi-client interactions for a complete usage of the program.
 * <p>
 * The test is written to validate a real-time client-server multiplayer architecture in a simulated environment
 * using mocks.
 */
public class MegaTest {

    /** number of mock clients used in this integration test */
    private final int N_CLIENTS = 10;

    /**
     * greek alphabet letters in order, useful for mock names.
     */
    private final String[] COOL_NAMES = {
            "alpha", "beta", "gamma", "delta", "epsilon", "zeta", "eta", "theta", "iota", "kappa", "lambda", "mu",
            "nu", "xi", "omicron", "pi", "rho", "sigma", "tau", "upsilon", "phi", "chi", "psi", "omega"
    };
    private String getCoolName(int index) {
        int times = index / COOL_NAMES.length;
        return COOL_NAMES[index % COOL_NAMES.length] + (times > 0 ? ("-" + times) : "");
    }

    /** array of initialized mock clients */
    private GameClientMock[] clients;

    /** shared reference to propagate asynchronous errors from test threads */
    AtomicReference<Throwable> error = new AtomicReference<>();

    /**
     * Ensures all client threads complete execution and fails if any error was raised during their execution.
     * This method should be called after each test step to synchronize and validate all side effects.
     */
    private void syncClients() {
        for (GameClientMock client : clients) {
            client.joinAll();
        }
        if (error.get() != null) {
            fail(error.get().getMessage());
        }
    }

    /**
     * Ensures all the specified client threads complete execution and fails if any error was raised during
     * their execution.
     */
    private void syncClients(int... indexes) {
        for (int i : indexes) {
            clients[i].joinAll();
        }
        if (error.get() != null) {
            fail(error.get().getMessage());
        }
    }

    /**
     * Ensures the client threads for the client at the specified index complete execution
     * and fails if any error was raised during their execution.
     * This method should be called after a test step applied to a single client to synchronize
     * and validate all side effects.
     */
    private void syncClient(int index) {
        clients[index].joinAll();
        if (error.get() != null) {
            fail(error.get().getMessage());
        }
    }

    /**
     * Initializes the game server and mock clients before each test execution.
     *
     * @throws NotBoundException if server binding fails
     * @throws IOException if an I/O error occurs during setup
     * @throws InterruptedException if thread synchronization fails
     */
    @BeforeEach
    public void setup() throws NotBoundException, IOException, InterruptedException {
        assertDoesNotThrow(GameServer::start);  // start the server
        assertThrows(AlreadyRunningServerException.class, GameServer::start);  // can't run multiple servers

        // initialize clients and expect their initial refresh
        clients = new GameClientMock[N_CLIENTS];
        for (int i = 0; i < N_CLIENTS; i++) {
            clients[i] = new GameClientMock(getCoolName(i), error).assertRefresh();
        }
    }

    /**
     * Called after each test to synchronize all clients and validate execution integrity.
     */
    @AfterEach
    public void teardown() {
        syncClients();
    }

    /**
     * Ensures that only the client at the specified index receives a view refresh.
     *
     * @param index index of the client expected to refresh
     * @return the client mock that should receive the refresh
     */
    private GameClientMock expectRefreshOnlyFor(int index) {
        syncClients();
        for (int i = 0; i < N_CLIENTS; i++) {
            if (i == index) {
                clients[i].assertRefresh();
            } else {
                clients[i].assertNoRefresh();
            }
        }
        return clients[index];
    }

    /**
     * Ensures that a specific subset of clients (including the one to return) are expected to receive a view refresh.
     * <p>
     * You must provide exactly one of {@code otherExpectedRefreshes} or {@code noExpectedRefreshes}.
     * The method asserts this at runtime.
     * If {@code otherExpectedRefreshes} is provided, the clients at those indices
     * (in addition to {@code refreshAndGet}) will be expected to refresh.
     * If {@code noExpectedRefreshes} is provided, all clients not in that set (including {@code refreshAndGet})
     * will be expected to refresh.
     *
     * @param refreshAndGet the index of the client to return and always expect a refresh from
     * @param otherExpectedRefreshes optional set of other client indices expected to receive a refresh;
     *                               must be {@code null} if {@code noExpectedRefreshes} is provided
     * @param noExpectedRefreshes optional set of client indices expected to not receive a refresh;
     *                            must be {@code null} if {@code otherExpectedRefreshes} is provided
     * @return the {@link GameClientMock} instance at index {@code refreshAndGet}
     * @throws AssertionError if both or neither of {@code otherExpectedRefreshes} and {@code noExpectedRefreshes}
     * are provided
     */
    private GameClientMock expectRefreshForAndGet(int refreshAndGet, Set<Integer> otherExpectedRefreshes,
                                                  Set<Integer> noExpectedRefreshes) {
        assert (otherExpectedRefreshes == null) != (noExpectedRefreshes == null);

        syncClients();

        Set<Integer> toRefresh = new HashSet<>();
        toRefresh.add(refreshAndGet);
        if (otherExpectedRefreshes != null) {
            toRefresh.addAll(otherExpectedRefreshes);
        } else {  // noExpectedRefreshes != null
            for (int i = 0; i < N_CLIENTS; i++) {
                if (!noExpectedRefreshes.contains(i)) {
                    toRefresh.add(i);
                }
            }
        }

        for (int i = 0; i < N_CLIENTS; i++) {
            if (toRefresh.contains(i)) {
                clients[i].assertRefresh();
            } else {
                clients[i].assertNoRefresh();
            }
        }
        return clients[refreshAndGet];
    }

    /**
     * Expects all clients to refresh and returns the one at the given index.
     *
     * @param index index of the client to return
     * @return the client mock at the specified index
     */
    private GameClientMock expectRefreshForAllAndGet(int index) {
        syncClients();
        for (GameClientMock client : clients) {
            client.assertRefresh();
        }
        return clients[index];
    }

    /**
     * Asserts that all clients at the specified indexes have entered the given game phase.
     *
     * @param phase the expected {@link GamePhaseType}
     * @param indexes indexes of clients to check
     */
    private void expectGamePhase(GamePhaseType phase, int... indexes) {
        for (int i : indexes) {
            clients[i].awaitConditionOnUpdate(gcm -> {
                GameData gd = gcm.getMockThis().getLinkedState().getLastUpdate().getCurrentGame();
                return gd != null && gd.getCurrentGamePhaseType() == phase;
            }, "game phase == " + phase);
        }
    }

    /**
     * Main test case verifying client-server interaction, game creation, joining,
     * refresh propagation, and state transitions.
     */
    @Test
    public void test() {

        // =============== network tests (connections, state of all...) ============== //

        // ensure no game at the beginning
        ArrayList<Game> games = GamesHandler.getInstance().getGames();
        assertEquals(0, games.size());

        // client 0 pings -> expect refresh only for client 0
        expectRefreshOnlyFor(0).simulateCommand("ping");

        // --------- alpha game ---------- //

        // client 0 creates game -> expect refresh for all
        expectRefreshForAllAndGet(0).simulateCommand("create", COOL_NAMES[0]);
        syncClients();

        // ensure client 0 is now a player with the first color available
        assertEquals(MainCabinTile.Color.values()[0],
                clients[0].getMockThis().getLinkedState().getLastUpdate().getClientPlayer().getColor());

        // ensure client 1 is now a player with in its linked state one game with one player with the first color available
        assertEquals(MainCabinTile.Color.values()[0],
                clients[1].getMockThis().getLinkedState()
                        .getLastUpdate().getAvailableGames().getFirst()
                        .getPlayers().getFirst()
                        .getColor());

        // validate game was created and stored
        UUID alphaGameUUID = clients[0].getMockThis().getLinkedState().getLastUpdate().getCurrentGame().getGameId();
        assertEquals(1, games.size());
        Game alphaGame = games.getFirst();
        assertEquals(alphaGame.getId(), alphaGameUUID);

        // validate client-game associations
        assertEquals(alphaGame, GamesHandler.getInstance().findGameByClientUUID(clients[0].getClientUUID()));
        for (int i = 1; i < N_CLIENTS; i++) {
            assertNull(GamesHandler.getInstance().findGameByClientUUID(clients[i].getClientUUID()));
        }

        // client 1 wrong attempt in joining client 0's game with invalid UUID
        // -> expect to catch and block command client-side
        clients[1].awaitConditionOnRefresh(gcm -> {
                    try {
                        return gcm.getMockView().getErrors().getLast().startsWith("Error_Invalid UUID: [false-UUID]");
                    } catch (NoSuchElementException e) {
                        return false;
                    }
                }, "expected error on join with invalid UUID: [false-UUID]"
        ).simulateCommand("join", "false-UUID", COOL_NAMES[1]);
        syncClient(1);

        // client 1 wrong attempt in joining client 0's game with valid UUID but not of a game
        // -> expect to catch and block command client-side
        UUID wrongUUID = UUID.randomUUID();
        clients[1].awaitConditionOnUpdate(gcm -> {
                    String error = gcm.getMockThis().getLinkedState().getLastUpdate().getError();
                    if (error == null) return false;
                    return error.startsWith("Could not find a game with UUID " + wrongUUID);
                }, "expected error on join with wrong UUID"
        ).simulateCommand("join", wrongUUID.toString(), COOL_NAMES[1]);
        syncClient(1);

        // client 1 wrong attempt in joining client 0's game with alpha game's UUID but already used color option
        // -> expect to catch and block command client-side
        clients[1].awaitConditionOnRefresh(gcm -> {
                    try {
                        return gcm.getMockView().getWarnings().getLast().startsWith("Warning_Invalid color 'BLUE'.");
                    } catch (NoSuchElementException e) {
                        return false;
                    }
                }, "warning on already used option 'color' blue"
        ).simulateCommand("join", alphaGameUUID.toString(), COOL_NAMES[1], "--color", "blue");
        syncClient(1);

        // client 1 joins client 0's game
        // -> expect to enter in assemble phase for client 0 and 1 (those in the game)
        expectGamePhase(GamePhaseType.ASSEMBLE, 0, 1);
        clients[1].simulateCommand("join", alphaGameUUID.toString(), COOL_NAMES[1]);
        syncClient(1);

        // ensure client 1 is now a player with the second color available
        assertEquals(MainCabinTile.Color.values()[1],
                clients[1].getMockThis().getLinkedState().getLastUpdate().getClientPlayer().getColor());

        // validate client 1 joined the correct game
        assertEquals(alphaGame, GamesHandler.getInstance().findGameByClientUUID(clients[0].getClientUUID()));
        assertEquals(alphaGame, GamesHandler.getInstance().findGameByClientUUID(clients[1].getClientUUID()));
        for (int i = 2; i < N_CLIENTS; i++) {
            assertNull(GamesHandler.getInstance().findGameByClientUUID(clients[i].getClientUUID()));
        }

        // --------- gamma game ---------- //

        // client 2 independently tries to create another game with 'color' option empty
        clients[2].awaitConditionOnRefresh(gcm -> {
                    try {
                        return gcm.getMockView().getWarnings().getLast().startsWith(
                                "Warning_Usage for optional parameter 'color' is [-c | --color <color>].");
                    } catch (NoSuchElementException e) {
                        return false;
                    }
                }, "warning on empty option 'color'"
        ).simulateCommand("create", COOL_NAMES[2], "--color");
        syncClient(2);

        // client 2 independently tries to create another game with 'color' option wrong
        clients[2].awaitConditionOnRefresh(gcm -> {
                    try {
                        return gcm.getMockView().getWarnings().getLast().startsWith(
                                "Warning_Invalid color 'PURPLE'.");
                    } catch (NoSuchElementException e) {
                        return false;
                    }
                }, "warning on wrong option 'color' as purple"
        ).simulateCommand("create", COOL_NAMES[2], "-c", "purple");
        syncClient(2);

        // client 2 independently creates another game with 'color' GREEN ->
        // all but those already in a game expect a refresh
        expectRefreshForAndGet(2, null, Set.of(0, 1))
                .simulateCommand("create", COOL_NAMES[2], "-c", "GreEn");
        syncClients();

        // ensure client 2 is now a player with color GREEN
        assertEquals(MainCabinTile.Color.GREEN,
                clients[2].getMockThis().getLinkedState().getLastUpdate().getClientPlayer().getColor());

        // client 2 wrong command ping as "pig" -> refresh only for it and ensure it's a wrong command
        clients[2].awaitConditionOnRefresh(gcm -> {
            try {
                return gcm.getMockView().getErrors().getLast().startsWith("Error_Rejected command: pig");
            } catch (NoSuchElementException e) {
                return false;
            }
            }, "expected error on wrong command: [pig]"
        ).simulateCommand("pig");

        // client 2 ping -> refresh only for it
        expectRefreshOnlyFor(2).simulateCommand("ping");
        syncClients();

        // validate game was created and stored correctly
        UUID gammaGameUUID = clients[2].getMockThis().getLinkedState().getLastUpdate().getCurrentGame().getGameId();
        assertEquals(2, games.size());
        Game gammaGame = games.get(1);
        assertEquals(gammaGame.getId(), gammaGameUUID);
        assertNotEquals(alphaGame, gammaGame);
        assertNotEquals(alphaGameUUID, gammaGameUUID);

        // validate client 2 joined the correct game
        assertEquals(alphaGame, GamesHandler.getInstance().findGameByClientUUID(clients[0].getClientUUID()));
        assertEquals(alphaGame, GamesHandler.getInstance().findGameByClientUUID(clients[1].getClientUUID()));
        assertEquals(gammaGame, GamesHandler.getInstance().findGameByClientUUID(clients[2].getClientUUID()));
        for (int i = 3; i < N_CLIENTS; i++) {
            assertNull(GamesHandler.getInstance().findGameByClientUUID(clients[i].getClientUUID()));
        }

        // ------ ensure no other client can join alpha game (it is already started) ------ //

        syncClients();
        clients[3].awaitConditionOnUpdate(gcm -> {
                String error = gcm.getMockThis().getLinkedState().getLastUpdate().getError();
                if (error == null) return false;
                return error.startsWith("Attempted to join a game that has already started!");
            }, "expected not join in alpha game"
        ).simulateCommand("join", alphaGameUUID.toString(), COOL_NAMES[3]);

        // =============== end of "networking" tests, now start tests in alpha game ============== //
        syncClients();

        // ------ assemble ------ //

        // abort nondeterminism in starting data
        List<TileSkeleton> tiles = TilesFactory.createPileTiles();
        int i = 0;
        for (TileSkeleton tile : tiles) {
            tile.setTileId(i);
            i++;
        }
        alphaGame.getGameData().setCoveredTiles(tiles);
        alphaGame.getGameData().setDeck(Deck.deterministic(DeckFactory.createTutorialDeck(), null));
        // note: shipboards are already deterministic

        // alpha draws the first tile
        clients[0].awaitConditionOnUpdate(gcm -> {
                    BatteryComponentTile tile0;
                    try {
                        tile0 = (BatteryComponentTile) gcm.getMockThis().getLinkedState().getLastUpdate()
                                .getClientPlayer().getTileInHand();
                    } catch (ClassCastException e) {
                        return false;
                    }
                    return (tile0.getTileId() == 0)
                            && (tile0.getSide(Direction.EAST) == SideType.SINGLE)
                            && (tile0.getSide(Direction.NORTH) == SideType.UNIVERSAL)
                            && (tile0.getSide(Direction.WEST) == SideType.SMOOTH)
                            && (tile0.getSide(Direction.SOUTH) == SideType.DOUBLE)
                            && (tile0.getCapacity() == 2)
                            && (tile0.getTextureName().equals("GT-new_tiles_16_for web.jpg"));
                }, "draw tile 0 [B2:1302]")
                .simulateCommand("draw");
        syncClient(0);

        // beta draws the second tile
        clients[1].awaitConditionOnUpdate(gcm -> {
                    BatteryComponentTile tile0;
                    try {
                        tile0 = (BatteryComponentTile) gcm.getMockThis().getLinkedState().getLastUpdate()
                                .getClientPlayer().getTileInHand();
                    } catch (ClassCastException e) {
                        return false;
                    }
                    return (tile0.getTileId() == 1)
                            && (tile0.getSide(Direction.EAST) == SideType.DOUBLE)
                            && (tile0.getSide(Direction.NORTH) == SideType.UNIVERSAL)
                            && (tile0.getSide(Direction.WEST) == SideType.SMOOTH)
                            && (tile0.getSide(Direction.SOUTH) == SideType.SMOOTH)
                            && (tile0.getCapacity() == 2)
                            && (tile0.getTextureName().equals("GT-new_tiles_16_for web2.jpg"));
                }, "draw tile 1 [B2:2300]")
                .simulateCommand("draw");
        syncClient(1);

        // discard those tiles
        clients[0].assertRefresh().simulateCommand("discard");
        clients[1].assertRefresh().simulateCommand("discard");
        syncClients(0, 1);

        // alpha draws and discard all the other tiles, to have all uncovered
        int leftTiles = tiles.size() - 2;  // already discarded 2 tiles
        for (i = 0; i < leftTiles; i++) {
            clients[0].awaitConditionOnUpdate(gcm ->
                    gcm.getMockThis().getLinkedState().getLastUpdate().getError() == null,
                    "expected no error on draw tiles"
            ).simulateCommand("draw").joinAll();
            clients[0].assertRefresh().simulateCommand("discard");
            syncClient(0);
        }

        clients[1].awaitConditionOnUpdate(gcm -> {
                    String error = gcm.getMockThis().getLinkedState().getLastUpdate().getError();
                    if (error == null) return false;
                    return error.startsWith("There are no covered tiles available.");
                }, "expected error on no more tiles available to draw"
        ).simulateCommand("draw");
        syncClient(1);

        //Client beta picks tile 0
        clients[1].simulateCommand("discard").joinAll();
        clients[1].awaitConditionOnUpdate(gcm -> {
                    BatteryComponentTile tile0;
                    try {
                        tile0 = (BatteryComponentTile) gcm.getMockThis().getLinkedState().getLastUpdate()
                                .getClientPlayer().getTileInHand();
                    } catch (ClassCastException e) {
                        return false;
                    }
                    return (tile0.getTileId() == 0)
                            && (tile0.getSide(Direction.EAST) == SideType.SINGLE)
                            && (tile0.getSide(Direction.NORTH) == SideType.UNIVERSAL)
                            && (tile0.getSide(Direction.WEST) == SideType.SMOOTH)
                            && (tile0.getSide(Direction.SOUTH) == SideType.DOUBLE)
                            && (tile0.getCapacity() == 2)
                            && (tile0.getTextureName().equals("GT-new_tiles_16_for web.jpg"));
                }, "pick tile 0 [B2:1302]")
                .simulateCommand("pick", "0");
        syncClient(1);

        //Client Beta places tile under the main cabin
        clients[1].awaitConditionOnUpdate(gcm -> {
            BatteryComponentTile tile0;
            Coordinates coord = new Coordinates(8, 7);
            try{
                tile0 = (BatteryComponentTile) gcm.getMockThis().getLinkedState().getLastUpdate()
                        .getClientPlayer().getShipBoard().getTile(coord);
            } catch (ClassCastException | OutOfBuildingAreaException | NoTileFoundException e) {
                return false;
            }
            return (tile0.getTileId() == 0)
                    && (tile0.getSide(Direction.EAST) == SideType.SINGLE)
                    && (tile0.getSide(Direction.NORTH) == SideType.UNIVERSAL)
                    && (tile0.getSide(Direction.WEST) == SideType.SMOOTH)
                    && (tile0.getSide(Direction.SOUTH) == SideType.DOUBLE)
                    && (tile0.getCapacity() == 2)
                    && (tile0.getTextureName().equals("GT-new_tiles_16_for web.jpg"));

        }, "place tile 0 at coordinates 8 7").simulateCommand("place", "8", "7");
        syncClients( 1);

        //Client beta picks tile 1
        clients[1].awaitConditionOnUpdate(gcm -> {
                    BatteryComponentTile tile0;
                    try {
                        tile0 = (BatteryComponentTile) gcm.getMockThis().getLinkedState().getLastUpdate()
                                .getClientPlayer().getTileInHand();
                    } catch (ClassCastException e) {
                        return false;
                    }
                    return (tile0.getTileId() == 1)
                            && (tile0.getSide(Direction.EAST) == SideType.DOUBLE)
                            && (tile0.getSide(Direction.NORTH) == SideType.UNIVERSAL)
                            && (tile0.getSide(Direction.WEST) == SideType.SMOOTH)
                            && (tile0.getSide(Direction.SOUTH) == SideType.SMOOTH)
                            && (tile0.getCapacity() == 2)
                            && (tile0.getTextureName().equals("GT-new_tiles_16_for web2.jpg"));
                }, "pick tile 1 [B2:2300]")
                .simulateCommand("pick", "1");
        syncClient(1);

        //Client beta rotates tile 1
        clients[1].awaitConditionOnRefresh(gcm -> {
                    Rotation rot;
                    try {
                        rot = gcm.getMockThis().getLinkedState().getLastUpdate()
                                .getClientPlayer().getTileInHand().getAppliedRotation();
                    } catch (ClassCastException e) {
                        return false;
                    }
                    return (rot == Rotation.CLOCKWISE);
                }, "rotate tile 1 clockwise [B2:2300]")
                .simulateCommand("rotate", "R");
        syncClient(1);

        //Client beta rotates tile 1 again
        clients[1].awaitConditionOnRefresh(gcm -> {
                    BatteryComponentTile tile0;
                    try {
                        tile0 = (BatteryComponentTile) gcm.getMockThis().getLinkedState().getLastUpdate()
                                .getClientPlayer().getTileInHand();
                    } catch (ClassCastException e) {
                        return false;
                    }
                    return (tile0.getTileId() == 1)
                            && (tile0.getSide(Direction.EAST) == SideType.DOUBLE)
                            && (tile0.getSide(Direction.NORTH) == SideType.UNIVERSAL)
                            && (tile0.getSide(Direction.WEST) == SideType.SMOOTH)
                            && (tile0.getSide(Direction.SOUTH) == SideType.SMOOTH)
                            && (tile0.getCapacity() == 2)
                            && (tile0.getTextureName().equals("GT-new_tiles_16_for web2.jpg"));
                }, "rotate tile 1 counterclockwise [B2:2300]")
                .simulateCommand("rotate", "L");
        syncClient(1);

        //Client Beta places tile left of the main cabin
        clients[1].awaitConditionOnUpdate(gcm -> {
            BatteryComponentTile tile0;
            Coordinates coord = new Coordinates(7, 6);
            try{
                tile0 = (BatteryComponentTile) gcm.getMockThis().getLinkedState().getLastUpdate()
                        .getClientPlayer().getShipBoard().getTile(coord);
            } catch (ClassCastException | OutOfBuildingAreaException | NoTileFoundException e) {
                return false;
            }
            return (tile0.getTileId() == 1)
                    && (tile0.getSide(Direction.EAST) == SideType.DOUBLE)
                    && (tile0.getSide(Direction.NORTH) == SideType.UNIVERSAL)
                    && (tile0.getSide(Direction.WEST) == SideType.SMOOTH)
                    && (tile0.getSide(Direction.SOUTH) == SideType.SMOOTH)
                    && (tile0.getCapacity() == 2)
                    && (tile0.getTextureName().equals("GT-new_tiles_16_for web2.jpg"));

        }, "place tile 1 at coordinates 7 6").simulateCommand("place", "7", "6");
        syncClients( 1);

        //Giving player beta a debug shipboard
        clients[1].awaitConditionOnUpdate(gcm -> {
                    int count;

                    try {
                        count = (int) gcm.getMockThis().getLinkedState().getLastUpdate()
                                .getClientPlayer().getShipBoard().getBoard().size();
                    } catch (ClassCastException e) {
                        return false;
                    }
                    return (count == 18);
                }, "load debug shipboard for player beta")
                .simulateCommand("cheat", "shipboard");
        syncClient(1);


        //Giving player alpha a debug shipboard
        clients[0].awaitConditionOnUpdate(gcm -> {
                    int count;

                    try {
                        count = (int) gcm.getMockThis().getLinkedState().getLastUpdate()
                                .getClientPlayer().getShipBoard().getBoard().size();
                    } catch (ClassCastException e) {
                        return false;
                    }
                    return (count == 18);
                }, "load debug shipboard for player alpha")
                .simulateCommand("cheat", "shipboard");
        syncClient(0);

        clients[0].awaitConditionOnUpdate(gcm -> {
                    boolean end;

                    try {
                        end = (boolean) gcm.getMockThis().getLinkedState().getLastUpdate()
                                .getClientPlayer().getShipBoard().isEndedAssembly();
                    } catch (ClassCastException e) {
                        return false;
                    }
                    return (end);
                }, "alpha finishes assembling")
                .simulateCommand("finish");
        syncClient(0);

        clients[1].awaitConditionOnUpdate(gcm -> {
                    boolean end;

                    try {
                        end = (boolean) gcm.getMockThis().getLinkedState().getLastUpdate()
                                .getClientPlayer().getShipBoard().isEndedAssembly();
                    } catch (ClassCastException e) {
                        return false;
                    }
                    return (end);
                }, "beta finishes assembling")
                .simulateCommand("finish");
        syncClient(1);



    }
}
