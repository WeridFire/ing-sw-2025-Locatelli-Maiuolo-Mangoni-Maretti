package it.polimi.ingsw.controller.cp;

import it.polimi.ingsw.controller.cp.exceptions.CommandNotAllowedException;
import it.polimi.ingsw.view.View;

public class ViewCommandsProcessor implements ICommandsProcessor {

    private final View view;

    public ViewCommandsProcessor(View view) {
        this.view = view;
    }

    @Override
    public void processCommand(String command, String[] args) {
        switch (command) {
            case "" -> view.onVoid();
            case "ping" -> view.onPing();
            case "screen" -> {
                if (args.length == 0) {
                    view.onScreen(null);
                }
                else if (args.length == 1) {
                    view.onScreen(args[0]);
                }
                else {  // args.length > 1
                    view.showWarning("Usage: screen | screen <name>");
                }
            }
            case "help" -> view.onHelp();
            case "debug" -> view.onDebug();
            case "cheat" -> {
                if (args.length != 1) {
                    view.showWarning("Usage: cheat <cheat name>");
                }
                else {
                    view.onCheat(args[0]);
                }
            }

            // if the command is not recognized as a global command, it lets the active screen process it
            default -> {
                try {
                    propagateProcessCommand(command, args);
                } catch (CommandNotAllowedException e) {
                    view.showError(e.getMessage());
                }
            }
        }
    }

    /**
     * The first active CommandsProcessor processes the specified command.
     * If it throws a CommandNotAllowedException, it tries with the next active CommandsProcessor and so on, until
     * no more CommandsProcessor are active, or
     * the command is processed without raising a CommandNotAllowedException.
     * @param command The command to process
     * @param args Arguments of the command to process
     * @throws CommandNotAllowedException if all the CommandsProcessor throw a CommandNotAllowedException,
     * (in which chase it's the first thrown exception (from the screen which is head of the queue)),
     * or if no CommandsProcessor is present (in which case a new standard exception is thrown).
     */
    public void propagateProcessCommand(String command, String[] args) throws CommandNotAllowedException {
        CommandNotAllowedException firstException = null;
        boolean processed = false;
        for (ICommandsProcessor processor : view.getCommandsProcessors()) {
            try {
                processor.processCommand(command, args);
                // if here: command has been processed correctly
                processed = true;
                break;
            } catch (CommandNotAllowedException e) {
                if (firstException == null) {
                    firstException = e;
                }
            }
        }
        if (!processed) {
            throw (firstException == null)
                    ? new CommandNotAllowedException(command, "it's not global and there are no other commands processors.")
                    : firstException;
        }
    }
}
