package it.polimi.ingsw;

import it.polimi.ingsw.network.GameClientMock;
import it.polimi.ingsw.network.GameServer;
import it.polimi.ingsw.network.exceptions.AlreadyRunningServerException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.rmi.NotBoundException;

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
    }

}
