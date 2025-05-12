package it.polimi.ingsw;

import it.polimi.ingsw.game.Game;
import it.polimi.ingsw.network.GameClientMock;
import it.polimi.ingsw.network.GameServer;
import it.polimi.ingsw.network.exceptions.AlreadyRunningServerException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class MegaTest {

    private final int N_CLIENTS = 3;
    private GameClientMock[] clients;

    private void joinAll() {
        for (GameClientMock client : clients) {
            client.joinAll();
        }
    }

    @BeforeEach
    public void setup() throws NotBoundException, IOException, InterruptedException {
        assertDoesNotThrow(GameServer::start);  // start the server
        assertThrows(AlreadyRunningServerException.class, GameServer::start);  // can't start two servers

        clients = new GameClientMock[N_CLIENTS];
        for (int i = 0; i < N_CLIENTS; i++) {
            clients[i] = new GameClientMock("client " + (i + 1)).expectRefresh();
        }
    }

    @AfterEach
    public void teardown() {
        joinAll();
    }

    private GameClientMock expectRefreshOnlyFor(int index) {
        joinAll();
        for (int i = 0; i < N_CLIENTS; i++) {
            if (i == index) {
                clients[i].expectRefresh();
            } else {
                clients[i].expectNoRefresh();
            }
        }
        return clients[index];
    }

    private GameClientMock expectRefreshOnlyForAndGet(int refreshAndGet, int... otherExpectedRefreshes) {
        joinAll();
        Set<Integer> otherExpectedRefreshSet = new HashSet<>(otherExpectedRefreshes.length + 1);
        otherExpectedRefreshSet.add(refreshAndGet);
        for (int i = 0; i < otherExpectedRefreshes.length; i++) {
            otherExpectedRefreshSet.add(otherExpectedRefreshes[i]);
        }
        for (int i = 0; i < N_CLIENTS; i++) {
            if (otherExpectedRefreshSet.contains(i)) {
                clients[i].expectRefresh();
            } else {
                clients[i].expectNoRefresh();
            }
        }
        return clients[refreshAndGet];
    }

    private GameClientMock expectRefreshForAllAndGet(int index) {
        joinAll();
        for (GameClientMock client : clients) {
            client.expectRefresh();
        }
        return clients[index];
    }

    @Test
    public void test() {
        expectRefreshOnlyFor(0).simulateCommand("ping");
        expectRefreshForAllAndGet(0).simulateCommand("create", "alfa");
        joinAll();

        UUID gameUUID = clients[0].getMockThis().getLinkedState().getLastUpdate().getCurrentGame().getGameId();
        ArrayList<Game> games = GamesHandler.getInstance().getGames();
        assertEquals(1, games.size());
        assertEquals(games.getFirst().getId(), gameUUID);

        expectRefreshOnlyForAndGet(1, 0)
                .simulateCommand("join", gameUUID.toString(), "beta");

        expectRefreshOnlyFor(2).simulateCommand("create", "gamma");
    }

}
