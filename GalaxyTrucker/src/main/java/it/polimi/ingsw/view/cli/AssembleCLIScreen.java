package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.enums.GamePhaseType;

import java.rmi.RemoteException;

public class AssembleCLIScreen extends CLIScreen{
    public AssembleCLIScreen(String screenName, boolean forceActivate) {
        super(screenName, forceActivate);
    }

    public AssembleCLIScreen(String screenName) {
        super(screenName);
    }

    @Override
    protected boolean switchConditions() {
        return getLastUpdate().getCurrentGame() != null &&
                getLastUpdate().getCurrentGame().getCurrentGamePhaseType() == GamePhaseType.ASSEMBLE;
    }

    @Override
    protected void processCommand(String command, String[] args) throws RemoteException, IllegalArgumentException {
        command = command.toLowerCase();
        switch(command){
            case "timerflip":
                getServer().flipHourglass(getClient());
                setScreenMessage("Done");
                break;

            case "draw":
                getServer().drawTile(getClient());
                setScreenMessage("Done");
                break;

            case "discard":
                getServer().discardTile(getClient());
                setScreenMessage("Done");
                break;

            case "reserve":
                getServer().reserveTile(getClient());
                setScreenMessage("Done");
                break;

            case "pick":
                if (args.length == 1) {
                    int id = Integer.parseInt(args[0]);
                    getServer().pickTile(getClient(), id);
                }
                setScreenMessage("Done");
                break;

            default:
                setScreenMessage("Invalid command. Use help to view available commands.");
                break;
        }
    }

    @Override
    void printScreenSpecificCommands() {
        printCommands(screenName,
                "timerflip | Flips the hourglass of the game.",
                "draw | Draws a tile from the covered tiles",
                "discard | Discard the tile you have in hand",
                "pick <tileId> | Pick in hand the tile with Id <tileId>"
        );
    }

    @Override
    public CLIFrame getCLIRepresentation() {
        //TODO: think about the assemble game screen
        return null;
    }
}
