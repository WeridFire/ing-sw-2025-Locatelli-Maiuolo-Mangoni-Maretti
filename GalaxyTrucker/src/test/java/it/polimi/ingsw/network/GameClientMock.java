package it.polimi.ingsw.network;

import it.polimi.ingsw.controller.states.State;
import it.polimi.ingsw.network.messages.ClientUpdate;
import it.polimi.ingsw.view.View;
import it.polimi.ingsw.view.ViewMock;
import org.junit.jupiter.api.function.Executable;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class GameClientMock implements IClient {
    private final String mockName;
    private final GameClient mockThis;

    public GameClientMock(String mockName) {
        // create mock IClient
        ClientMock mockClient = null;
        try {
            mockClient = new ClientMock();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        // create mock view
        ViewMock mockView = new ViewMock(mockName);
        // simulate mock state for this client as endpoint target even in multiple instances for a single process
        State mockState = new State();

        // mock client
        this.mockName = mockName;
        mockThis = new GameClient(mockClient, mockView, mockState);

        // init
        mockClient.init(mockThis);
        mockView.init(mockThis);

        // now simulate the real start
        new Thread(() -> {
            try {
                GameClient.start(mockThis);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    @Override
    public IServer getServer() throws RemoteException {
        return getMockThis().getServer();
    }

    @Override
    public void updateClient(ClientUpdate clientUpdate) throws RemoteException {
        getMockThis().updateClient(clientUpdate);
    }

    public String getMockName() {
        return mockName;
    }

    public GameClient getMockThis() {
        return mockThis;
    }

    public View getView() {
        return getMockThis().getView();
    }

    public GameClientMock simulateCommand(String command, String... args) {
        getView().getCommandsProcessor().processCommand(command, args);
        return this;
    }

    private final AtomicInteger expectations = new AtomicInteger(0);
    private final Map<Integer, Throwable> currentlyExpectingFailures = new HashMap<>();
    private final Set<Thread> threads = new HashSet<>();

    private void threadAddAndRunAssertDoesNotThrow(Executable executable) {
        synchronized (threads) {
            Thread thread = new Thread(() -> assertDoesNotThrow(() -> {
                Throwable rethrow = null;
                try {
                    executable.execute();
                } catch (Throwable t) {
                    rethrow = t;
                } finally {
                    synchronized (threads) {
                        threads.remove(Thread.currentThread());
                        threads.notifyAll();
                    }
                    if (rethrow != null) {
                        throw rethrow;
                    }
                }
            }));
            threads.add(thread);
            thread.start();
        }
    }

    public GameClientMock expectRefresh(long timeoutMillis, Throwable orElse) {
        assert timeoutMillis > 0;
        assert orElse != null;

        final CountDownLatch latch = new CountDownLatch(1);
        final int orElseIndex = expectations.getAndIncrement();
        currentlyExpectingFailures.put(orElseIndex, orElse);

        getView().doOnceOnRefresh(latch::countDown);

        threadAddAndRunAssertDoesNotThrow(() -> {
            boolean refreshed = latch.await(timeoutMillis, TimeUnit.MILLISECONDS);
            if (!refreshed) {
                throw currentlyExpectingFailures.remove(orElseIndex);
            } // else
            currentlyExpectingFailures.remove(orElseIndex);
            System.out.println("Thread expectRefresh (" + orElseIndex + ") ended from " + mockName);
        });

        return this;
    }
    public GameClientMock expectRefresh() {
        return expectRefresh(1000, new Exception("Expected refresh. Did not refresh."));
    }

    public GameClientMock expectNoRefresh(long timeoutMillis, Throwable orElse) {
        assert timeoutMillis > 0;
        assert orElse != null;

        final CountDownLatch latch = new CountDownLatch(1);
        final int orElseIndex = expectations.getAndIncrement();
        currentlyExpectingFailures.put(orElseIndex, orElse);

        // refresh done -> test failure
        getView().doOnceOnRefresh(latch::countDown);

        threadAddAndRunAssertDoesNotThrow(() -> {
            boolean refreshed = latch.await(timeoutMillis, TimeUnit.MILLISECONDS);
            if (refreshed) {
                throw currentlyExpectingFailures.remove(orElseIndex);
            } // else
            currentlyExpectingFailures.remove(orElseIndex);
            System.out.println("Thread expectNoRefresh (" + orElseIndex + ") ended from " + mockName);
        });

        return this;
    }
    public GameClientMock expectNoRefresh() {
        return expectNoRefresh(1000, new Exception("Expected no refresh. Did refresh."));
    }

    public GameClientMock joinAll() {
        synchronized (threads) {
            while (!threads.isEmpty()) {
                try {
                    threads.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return this;
    }
}
