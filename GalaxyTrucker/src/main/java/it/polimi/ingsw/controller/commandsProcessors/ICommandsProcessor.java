package it.polimi.ingsw.controller.commandsProcessors;

import it.polimi.ingsw.controller.commandsProcessors.exceptions.CommandNotAllowedException;

public interface ICommandsProcessor {
    /**
     * This function allows the main logic of any IView to delegate the handling of a certain set of commands.
     * Passes the command and the args, and the processor tries to execute.
     * If it fails, the processor will display the error and the requirements for the command to execute.
     *
     * @param command The command to execute
     * @param args The args to pass
     * @throws CommandNotAllowedException If the specified command can not be processed in this screen
     *
     * @implSpec switchConditions() == true for the specific game phase screen/view
     * (the set of implemented commands must be allowed when this function gets called)
     */
    void processCommand(String command, String[] args) throws CommandNotAllowedException;
}
