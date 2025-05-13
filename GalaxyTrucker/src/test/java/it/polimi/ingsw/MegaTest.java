package it.polimi.ingsw;

import it.polimi.ingsw.cards.Deck;
import it.polimi.ingsw.cards.DeckFactory;
import it.polimi.ingsw.controller.states.State;
import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.enums.GamePhaseType;
import it.polimi.ingsw.game.Game;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.network.GameClientMock;
import it.polimi.ingsw.network.GameServer;
import it.polimi.ingsw.network.exceptions.AlreadyRunningServerException;
import it.polimi.ingsw.shipboard.SideType;
import it.polimi.ingsw.shipboard.tiles.BatteryComponentTile;
import it.polimi.ingsw.shipboard.tiles.TileSkeleton;
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
        // ensure no game at the beginning
        ArrayList<Game> games = GamesHandler.getInstance().getGames();
        assertEquals(0, games.size());

        // client 0 pings -> expect refresh only for client 0
        expectRefreshOnlyFor(0).simulateCommand("ping");
        // client 0 creates game -> expect refresh for all
        expectRefreshForAllAndGet(0).simulateCommand("create", COOL_NAMES[0]);
        syncClients();

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

        // client 1 joins client 0's game
        // -> expect to enter in assemble phase for client 0 and 1 (those in the game)
        expectGamePhase(GamePhaseType.ASSEMBLE, 0, 1);
        clients[1].simulateCommand("join", alphaGameUUID.toString(), COOL_NAMES[1]);
        syncClients();

        // validate client 1 joined the correct game
        assertEquals(alphaGame, GamesHandler.getInstance().findGameByClientUUID(clients[0].getClientUUID()));
        assertEquals(alphaGame, GamesHandler.getInstance().findGameByClientUUID(clients[1].getClientUUID()));
        for (int i = 2; i < N_CLIENTS; i++) {
            assertNull(GamesHandler.getInstance().findGameByClientUUID(clients[i].getClientUUID()));
        }

        // client 2 independently creates another game -> all but those already in a game expect a refresh
        expectRefreshForAndGet(2, null, Set.of(0, 1))
                .simulateCommand("create", COOL_NAMES[2]);
        // client 2 wrong command ping as "pig" -> refresh only for it and ensure it's a wrong command
        syncClients();
        clients[2].awaitConditionOnRefresh(gcm -> {
            try {
                return gcm.getMockView().getErrors().getFirst().startsWith("Error_Rejected command: pig");
            } catch (NoSuchElementException e) {
                return false;
            }
            }, "expected error on wrong command 'pig'"
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

        // =============== end of "networking" tests, now start tests in alpha game ============== //

        // abort nondeterminism in starting data
        List<TileSkeleton> tiles = TilesFactory.createPileTiles();
        alphaGame.getGameData().setCoveredTiles(tiles);
        alphaGame.getGameData().setDeck(Deck.deterministic(DeckFactory.createTutorialDeck(), null));
        // note: shipboards are already deterministic

        State.overrideInstance(clients[0].getMockThis().getLinkedState());
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
                            && (tile0.getCapacity() == 2);
                }, "draw tile 0 [2B:1302]")
                .simulateCommand("draw")
                .joinAll();
    }
}
