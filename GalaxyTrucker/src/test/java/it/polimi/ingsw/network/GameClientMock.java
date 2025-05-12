package it.polimi.ingsw.network;

import it.polimi.ingsw.controller.states.State;
import it.polimi.ingsw.network.messages.ClientUpdate;
import it.polimi.ingsw.view.View;
import it.polimi.ingsw.view.ViewMock;
import org.junit.jupiter.api.function.Executable;

import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A test utility class that simulates the behavior of a {@link GameClient} in a controlled environment.
 * <p>
 * This mock client is designed to assist with automated testing of client-side behavior in a
 * client-server architecture, particularly focused on state transitions, view updates, and command handling.
 * It wraps a real {@link GameClient} instance initialized with mock components:
 * {@link ClientMock}, {@link ViewMock}, and {@link State}.
 */
public class GameClientMock implements IClient {

    private final String mockName;
    private final GameClient mockThis;
    private final ViewMock mockView;
    private final AtomicReference<Throwable> errorSetter;

    private final List<Consumer<ClientUpdate>> updateListeners = new ArrayList<>();
    private final Set<Thread> threads = new HashSet<>();

    /**
     * Creates a new mock client instance with the given name and error tracking reference.
     * Starts the client loop asynchronously.
     *
     * @param mockName a name identifier for this mock instance
     * @param errorSetter a reference to collect assertion or execution errors
     */
    public GameClientMock(String mockName, AtomicReference<Throwable> errorSetter) {
        this.errorSetter = errorSetter;

        // create mock IClient
        ClientMock mockClient = null;
        try {
            mockClient = new ClientMock();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        // create mock view
        mockView = new ViewMock(mockName);
        // simulate mock state for this client as endpoint target even in multiple instances for a single process
        State mockState = new State();

        // mock client
        this.mockName = mockName;
        mockThis = new GameClient(mockClient, mockView, mockState);

        // init
        mockClient.init(mockThis);
        mockView.init(mockThis);
        mockView.registerOnUpdateListener(this::onUpdate);

        // now simulate the real start
        new Thread(() -> {
            try {
                GameClient.start(mockThis);
            } catch (RemoteException e) {
                errorSetter.set(e);
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

    /**
     * @return the label assigned to this mock instance.
     */
    public String getMockName() {
        return mockName;
    }

    /**
     * @return the wrapped {@link GameClient} instance.
     */
    public GameClient getMockThis() {
        return mockThis;
    }

    /**
     * @return the mock view associated with this client.
     */
    public View getView() {
        return getMockThis().getView();
    }

    /**
     * @return the UUID of the mock client
     */
    public UUID getClientUUID() {
        return mockView.getClientUUID();
    }

    /**
     * Internal listener that dispatches updates to registered consumers.
     *
     * @param clientUpdate the update to dispatch
     */
    private void onUpdate(ClientUpdate clientUpdate) {
        for (Consumer<ClientUpdate> consumer : updateListeners) {
            consumer.accept(clientUpdate);
        }
    }

    /**
     * Handles the logic for executing an assertion in a background thread,
     * managing error propagation and thread tracking.
     *
     * @param executable assertion or test operation to run
     */
    private void threadAddAndRunAssertDoesNotThrow(Executable executable) {
        synchronized (threads) {
            Thread thread = new Thread(() -> assertDoesNotThrow(() -> {
                try {
                    executable.execute();
                } catch (Throwable t) {
                    errorSetter.set(t);
                } finally {
                    synchronized (threads) {
                        threads.remove(Thread.currentThread());
                        threads.notifyAll();
                    }
                }
            }));
            threads.add(thread);
            thread.start();
        }
    }

    /**
     * Waits for all background threads launched by this mock to complete.
     *
     * @return this instance for chaining
     */
    public GameClientMock joinAll() {
        synchronized (threads) {
            while (!threads.isEmpty()) {
                try {
                    threads.wait();
                } catch (InterruptedException e) {
                    errorSetter.set(e);
                    return this;
                }
            }
        }
        return this;
    }

    /**
     * Simulates input of a textual command with optional arguments.
     *
     * @param command the command name
     * @param args command arguments
     * @return this instance for chaining
     */
    public GameClientMock simulateCommand(String command, String... args) {
        getView().getCommandsProcessor().processCommand(command, args);
        return this;
    }

    /**
     * Awaits a view refresh event and fails if it does not occur within the timeout.
     *
     * @param timeoutMillis timeout duration in milliseconds
     * @param orElse error to be raised if refresh doesn't happen
     * @return this instance for chaining
     */
    public GameClientMock expectRefresh(long timeoutMillis, Throwable orElse) {
        assert timeoutMillis > 0;
        assert orElse != null;

        final CountDownLatch latch = new CountDownLatch(1);
        getView().doOnceOnRefresh(latch::countDown);

        threadAddAndRunAssertDoesNotThrow(() -> {
            boolean refreshed = latch.await(timeoutMillis, TimeUnit.MILLISECONDS);
            if (!refreshed) errorSetter.set(orElse);
            else System.out.println("Thread expectRefresh ended from " + mockName);
        });

        return this;
    }
    /**
     * Shorthand version of {@link #expectRefresh(long, Throwable)} with default timeout and error.
     *
     * @return this instance for chaining
     */
    public GameClientMock expectRefresh() {
        return expectRefresh(100, new Exception("Expected refresh for " + mockName + ". Did not refresh."));
    }

    /**
     * Awaits for a timeout period expecting no refresh to occur. Fails if a refresh happens.
     *
     * @param timeoutMillis timeout duration in milliseconds
     * @param orElse error to be raised if a refresh does happen
     * @return this instance for chaining
     */
    public GameClientMock expectNoRefresh(long timeoutMillis, Throwable orElse) {
        assert timeoutMillis > 0;
        assert orElse != null;

        final CountDownLatch latch = new CountDownLatch(1);
        getView().doOnceOnRefresh(latch::countDown);

        threadAddAndRunAssertDoesNotThrow(() -> {
            boolean refreshed = latch.await(timeoutMillis, TimeUnit.MILLISECONDS);
            if (refreshed) errorSetter.set(orElse);
            else System.out.println("Thread expectNoRefresh ended from " + mockName);
        });

        return this;
    }
    /**
     * Shorthand version of {@link #expectNoRefresh(long, Throwable)} with default timeout and error.
     *
     * @return this instance for chaining
     */
    public GameClientMock expectNoRefresh() {
        return expectNoRefresh(100, new Exception("Expected no refresh for " + mockName + ". Did refresh."));
    }

    /**
     * Waits until the specified condition is satisfied after an update.
     * Fails with the given error if the timeout expires before the condition becomes true.
     *
     * @param condition predicate tested on the mock client
     * @param conditionFriendlyName description for logging or error reporting
     * @param timeoutMillis timeout in milliseconds
     * @param orElse error to raise on timeout
     * @return this instance for chaining
     */
    public GameClientMock awaitConditionOnUpdate(Predicate<GameClient> condition, String conditionFriendlyName,
                                                 long timeoutMillis, Throwable orElse) {
        assert condition != null;
        assert timeoutMillis > 0;
        assert orElse != null;

        final CountDownLatch latch = new CountDownLatch(1);

        Consumer<ClientUpdate> checkCondition = _ -> {
            if (condition.test(getMockThis())) {
                latch.countDown();
            }
        };
        updateListeners.add(checkCondition);

        threadAddAndRunAssertDoesNotThrow(() -> {
            boolean success = latch.await(timeoutMillis, TimeUnit.MILLISECONDS);
            if (!success) {
                errorSetter.set(orElse);
            } else {
                System.out.println("Thread awaitConditionOnUpdate \"" + conditionFriendlyName
                        + "\" ended from " + mockName);
            }
        });

        return this;
    }
    /**
     * Shorthand version of {@link #awaitConditionOnUpdate(Predicate, String, long, Throwable)}
     * with default timeout and error.
     *
     * @param condition predicate tested on the mock client
     * @param conditionFriendlyName human-readable name for the condition
     * @return this instance for chaining
     */
    public GameClientMock awaitConditionOnUpdate(Predicate<GameClient> condition, String conditionFriendlyName) {
        return awaitConditionOnUpdate(condition, conditionFriendlyName, 500,
                new Exception("Expected condition \"" + conditionFriendlyName
                        + "\" for " + mockName + ". Not satisfied in the recent updates."));
    }
}