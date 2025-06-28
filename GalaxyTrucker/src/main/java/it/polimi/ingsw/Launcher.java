package it.polimi.ingsw;

import it.polimi.ingsw.network.GameClient;
import it.polimi.ingsw.network.GameServer;
import it.polimi.ingsw.network.exceptions.AlreadyRunningServerException;
import it.polimi.ingsw.util.CommandOptionsParser;
import it.polimi.ingsw.util.Default;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class Launcher {

    // server
    private static final String OPT_SERVER = "server";
    private static final String OPT_SOCKET_PORT = "socket_port";
    private static final String OPT_RMI_PORT = "rmi_port";
    // client
    private static final String OPT_TUI = "tui";
    private static final String OPT_GUI = "gui";
    private static final String OPT_SOCKET = "socket_client";
    private static final String OPT_RMI = "rmi_client";
    private static final String OPT_HOST = "host";
    private static final String OPT_CLIENT_PORT = "client_port";

    public static void main(String[] args) {
        // build command from args
        StringBuilder command = new StringBuilder("launch");
        for (String arg : args) {
            command.append(' ').append(arg);
        }

        // try parsing the command
        HashMap<String, String> options;
        try {
            options = parseOptions(command.toString());
        } catch (CommandOptionsParser.IllegalFormatException e) {
            System.err.println(e.getMessage());
            return;
        } catch (Exception e) {
            System.err.println("This build is corrupted: " + e.getMessage());
            return;
        }

        // choice - server or client
        if (CommandOptionsParser.toBoolean(options, OPT_SERVER)) {
            launchServer(options);
        } else {
            launchClient(options);
        }
    }

    private static HashMap<String, String> parseOptions(String command)
            throws CommandOptionsParser.IllegalFormatException {

        // prepare content validation
        CommandOptionsParser.Validator validator = new CommandOptionsParser.Validator();
        validator.add(OPT_SOCKET_PORT, CommandOptionsParser.Validator
                .createIntegerInvalidator(OPT_SOCKET_PORT, 1, 65535));
        validator.add(OPT_RMI_PORT, CommandOptionsParser.Validator
                .createIntegerInvalidator(OPT_RMI_PORT, 1, 65535));
        validator.add(OPT_CLIENT_PORT, CommandOptionsParser.Validator
                .createIntegerInvalidator(OPT_CLIENT_PORT, 1, 65535));

        // parse the command
        HashMap<String, String> result = CommandOptionsParser.parse(command, List.of(
                // Server
                new CommandOptionsParser.OptionFinder("--server", OPT_SERVER, null),
                new CommandOptionsParser.OptionFinder(List.of("--socket-port", "-sp"),
                        OPT_SOCKET_PORT, String.valueOf(Default.PORT(false))),
                new CommandOptionsParser.OptionFinder(List.of("--rmi-port", "-rmip"),
                        OPT_RMI_PORT, String.valueOf(Default.PORT(true))),

                // Client
                new CommandOptionsParser.OptionFinder("--tui", OPT_TUI, null),
                new CommandOptionsParser.OptionFinder("--gui", OPT_GUI, null),
                new CommandOptionsParser.OptionFinder(List.of("--socket-client", "-sc"), OPT_SOCKET, null),
                new CommandOptionsParser.OptionFinder(List.of("--rmi-client", "-rmic"), OPT_RMI, null),
                new CommandOptionsParser.OptionFinder(List.of("--host", "-h"),
                        OPT_HOST, Default.HOST),
                new CommandOptionsParser.OptionFinder(List.of("--port", "-p"),
                        OPT_CLIENT_PORT, String.valueOf(Default.PORT(Default.USE_RMI)))

                )
        );

        // exclusivity between: server and client; in client between: tui and gui, socket and rmi
        CommandOptionsParser.validateMutuallyExclusiveBooleans(result, Set.of(OPT_SERVER, OPT_TUI, OPT_GUI));
        CommandOptionsParser.validateMutuallyExclusiveBooleans(result, Set.of(OPT_SERVER, OPT_SOCKET, OPT_RMI));
        // exclusivity between: gui, socket and rmi (if gui -> will decide on gui what to use)
        CommandOptionsParser.validateMutuallyExclusiveBooleans(result, Set.of(OPT_GUI, OPT_SOCKET, OPT_RMI));

        return result;
    }

    private static void launchServer(HashMap<String, String> options) {
        try {
            GameServer.start(Integer.parseInt(options.get(OPT_RMI_PORT)),
                    Integer.parseInt(options.get(OPT_SOCKET_PORT)));
        } catch (AlreadyRunningServerException e) {
            System.err.println(e.getMessage());
        }
    }

    private static void launchClient(HashMap<String, String> options) {
        if (CommandOptionsParser.toBoolean(options, OPT_GUI)) {
            MainApp.createGUI();
        } else {
            try {
                GameClient.create(
                        CommandOptionsParser.toBoolean(options, OPT_RMI),
                        options.get(OPT_HOST),
                        Integer.parseInt(options.get(OPT_CLIENT_PORT)),
                        false
                );
            } catch (IOException | NotBoundException e) {
                System.err.println(e.getMessage());
            }
        }
    }

}
