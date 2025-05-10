package it.polimi.ingsw.controller.cp;

import it.polimi.ingsw.controller.cp.exceptions.CommandNotAllowedException;
import it.polimi.ingsw.network.GameClient;
import it.polimi.ingsw.network.IClient;
import it.polimi.ingsw.network.IServer;
import it.polimi.ingsw.view.View;

import java.rmi.RemoteException;
import java.util.List;

public abstract class PhaseCommandsProcessor implements ICommandsProcessor {

    protected IClient client;
    protected IServer server;
    protected View view;

    public PhaseCommandsProcessor(IClient client, IServer server, View view) {
        this.client = client;
        this.server = server;
        this.view = view;
    }

    public PhaseCommandsProcessor(GameClient gameClient) {
        this(gameClient.getClient(), gameClient.getServer(), gameClient.getView());
    }

    @Override
    public final void processCommand(String command, String[] args) throws CommandNotAllowedException {
        if (validateCommand(command, args)) {
            try {
                performCommand(command, args);
            } catch (RemoteException e) {
                view.showError(e.getMessage());
            }
        }
    }

    /**
     * @return the commands available in the specific commands processor, each formatted as "{command}|{description}".
     * @implNote Can be immutable.
     */
    public abstract List<String> getAvailableCommands();

    /**
     * This function allows to perform client-side checks and commands before trying to send the command
     * to the processor, which would send it to the server. Manage here all client-side checks and commands.
     * Already updates the view to show any problems in the attempted command.
     *
     * @return {@code true} if the command is valid from client-side,
     * {@code false} if not valid or already processed client-side.
     * @throws CommandNotAllowedException if the command is not among this processor commands.
     */
    protected abstract boolean validateCommand(String command, String[] args) throws CommandNotAllowedException;

    /**
     * This function actually send the command to the server parsing correctly the arguments.
     * Note: client side checks done before -> command and args must represent a valid command for the server.
     */
    protected abstract void performCommand(String command, String[] args) throws RemoteException;


    // util functions below

    protected Integer validateInteger(String integerToParse, String integerName) {
        int id;
        try {
            id = Integer.parseInt(integerToParse);
            return id;
        } catch (NumberFormatException e) {
            view.showError("Invalid " + integerName + ". Please enter valid " + integerName + ": integer number only.");
            return null;
        }
    }
}
