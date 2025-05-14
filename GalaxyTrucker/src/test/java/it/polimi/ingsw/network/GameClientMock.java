package it.polimi.ingsw.network;

import it.polimi.ingsw.controller.states.State;
import it.polimi.ingsw.network.messages.ClientUpdate;
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
    private final List<Runnable> refreshListeners = new ArrayList<>();
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
        mockView.registerPermanentOnRefreshListener(this::onRefresh);

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
    public ViewMock getMockView() {
        return mockView;
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
     * Internal listener that dispatches refreshes to registered consumers.
     */
    private void onRefresh() {
        for (Runnable runnable : refreshListeners) {
            runnable.run();
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
        getMockView().getCommandsProcessor().processCommand(command, args);
        return this;
    }

    /**
     * Waits until the specified condition is satisfied after a refresh.
     * Fails with the given error if the timeout expires before the condition becomes true.
     *
     * @param condition predicate tested on the mock client
     * @param conditionFriendlyName description for logging or error reporting
     * @param timeoutMillis timeout in milliseconds
     * @param onConditionMet error to raise on condition met (if did not want that condition).
     * {@code null} if no problem when the condition is met.
     * @param onTimeout error to raise on timeout. {@code null} if no problem when the time runs out.
     * @return this instance for chaining
     */
    public GameClientMock awaitConditionOnRefresh(Predicate<GameClientMock> condition, String conditionFriendlyName,
                                                 long timeoutMillis, Throwable onConditionMet, Throwable onTimeout) {
        assert condition != null;
        assert timeoutMillis > 0;

        final CountDownLatch latch = new CountDownLatch(1);

        Runnable checkCondition = () -> {
            if (condition.test(this)) {
                latch.countDown();
            }
        };
        refreshListeners.add(checkCondition);

        threadAddAndRunAssertDoesNotThrow(() -> {
            boolean success = latch.await(timeoutMillis, TimeUnit.MILLISECONDS);
            if (success) {
                if (onConditionMet != null) {
                    errorSetter.set(onConditionMet);
                }
            }
            else {
                if (onTimeout != null) {
                    errorSetter.set(onTimeout);
                }
            }
            System.out.println("Thread awaitConditionOnRefresh \"" + conditionFriendlyName
                    + "\" ended for " + mockName + " caused by " + (success ? "condition met" : "timeout"));
            refreshListeners.remove(checkCondition);
        });

        return this;
    }
    /**
     * Shorthand version of {@link #awaitConditionOnRefresh(Predicate, String, long, Throwable, Throwable)}
     * with default timeout and error.
     *
     * @param condition predicate tested on the mock client
     * @param conditionFriendlyName human-readable name for the condition
     * @return this instance for chaining
     */
    public GameClientMock awaitConditionOnRefresh(Predicate<GameClientMock> condition, String conditionFriendlyName) {
        return awaitConditionOnRefresh(condition, conditionFriendlyName, 500, null,
                new AssertionError("Expected condition \"" + conditionFriendlyName
                        + "\" for " + mockName + ". Not satisfied in the recent updates."));
    }

    /**
     * Awaits a view refresh event and fails if it does not occur within the timeout.
     *
     * @param timeoutMillis timeout duration in milliseconds
     * @param orElse error to be raised if refresh doesn't happen
     * @return this instance for chaining
     */
    public GameClientMock assertRefresh(long timeoutMillis, AssertionError orElse) {
        return awaitConditionOnRefresh(_ -> true, "assertRefresh",
                timeoutMillis, null, orElse);
    }
    /**
     * Shorthand version of {@link #assertRefresh(long, AssertionError)} with default timeout and error.
     *
     * @return this instance for chaining
     */
    public GameClientMock assertRefresh() {
        return assertRefresh(100, new AssertionError("Expected refresh for "
                + mockName + ". Did not refresh."));
    }

    /**
     * Awaits for a timeout period expecting no refresh to occur. Fails if a refresh happens.
     *
     * @param timeoutMillis timeout duration in milliseconds
     * @param orElse error to be raised if a refresh does happen
     * @return this instance for chaining
     */
    public GameClientMock assertNoRefresh(long timeoutMillis, AssertionError orElse) {
        return awaitConditionOnRefresh(_ -> true, "assertNoRefresh",
                timeoutMillis, orElse, null);
    }
    /**
     * Shorthand version of {@link #assertNoRefresh(long, AssertionError)} with default timeout and error.
     *
     * @return this instance for chaining
     */
    public GameClientMock assertNoRefresh() {
        return assertNoRefresh(200, new AssertionError("Expected no refresh for "
                + mockName + ". Did refresh."));
    }

    /**
     * Waits until the specified condition is satisfied after an update.
     * Fails with the given error if the timeout expires before the condition becomes true.
     *
     * @param condition predicate tested on the mock client
     * @param conditionFriendlyName description for logging or error reporting
     * @param timeoutMillis timeout in milliseconds
     * @param onConditionMet error to raise on condition met (if did not want that condition).
     * {@code null} if no problem when the condition is met.
     * @param onTimeout error to raise on timeout. {@code null} if no problem when the time runs out.
     * @return this instance for chaining
     */
    public GameClientMock awaitConditionOnUpdate(Predicate<GameClientMock> condition, String conditionFriendlyName,
                                                 long timeoutMillis, Throwable onConditionMet, Throwable onTimeout) {
        assert condition != null;
        assert timeoutMillis > 0;

        final CountDownLatch latch = new CountDownLatch(1);

        Consumer<ClientUpdate> checkCondition = _ -> {
            if (condition.test(this)) {
                latch.countDown();
            }
        };
        updateListeners.add(checkCondition);

        threadAddAndRunAssertDoesNotThrow(() -> {
            boolean success = latch.await(timeoutMillis, TimeUnit.MILLISECONDS);
            if (success) {
                if (onConditionMet != null) {
                    errorSetter.set(onConditionMet);
                }
            }
            else {
                if (onTimeout != null) {
                    errorSetter.set(onTimeout);
                }
            }
            System.out.println("Thread awaitConditionOnUpdate \"" + conditionFriendlyName
                    + "\" ended for " + mockName);
            updateListeners.remove(checkCondition);
        });

        return this;
    }
    /**
     * Shorthand version of {@link #awaitConditionOnUpdate(Predicate, String, long, Throwable, Throwable)}
     * with default timeout and error.
     *
     * @param condition predicate tested on the mock client
     * @param conditionFriendlyName human-readable name for the condition
     * @return this instance for chaining
     */
    public GameClientMock awaitConditionOnUpdate(Predicate<GameClientMock> condition, String conditionFriendlyName) {
        return awaitConditionOnUpdate(condition, conditionFriendlyName, 500, null,
                new AssertionError("Expected condition \"" + conditionFriendlyName
                        + "\" for " + mockName + ". Not satisfied in the recent updates."));
    }

    /**
     * Awaits a view update event and fails if it does not occur within the timeout.
     *
     * @param timeoutMillis timeout duration in milliseconds
     * @param orElse error to be raised if an update doesn't happen
     * @return this instance for chaining
     */
    public GameClientMock assertUpdate(long timeoutMillis, AssertionError orElse) {
        return awaitConditionOnUpdate(_ -> true, "assertUpdate",
                timeoutMillis, null, orElse);
    }
    /**
     * Shorthand version of {@link #assertUpdate(long, AssertionError)} with default timeout and error.
     *
     * @return this instance for chaining
     */
    public GameClientMock assertUpdate() {
        return assertUpdate(100, new AssertionError("Expected update for "
                + mockName + ". Did not update."));
    }

    /**
     * Awaits for a timeout period expecting no update to occur. Fails if an update happens.
     *
     * @param timeoutMillis timeout duration in milliseconds
     * @param orElse error to be raised if an update does happen
     * @return this instance for chaining
     */
    public GameClientMock assertNoUpdate(long timeoutMillis, AssertionError orElse) {
        return awaitConditionOnUpdate(_ -> true, "assertNoUpdate",
                timeoutMillis, orElse, null);
    }
    /**
     * Shorthand version of {@link #assertNoUpdate(long, AssertionError)} with default timeout and error.
     *
     * @return this instance for chaining
     */
    public GameClientMock assertNoUpdate() {
        return assertNoUpdate(200, new AssertionError("Expected no update for "
                + mockName + ". Did update."));
    }
}