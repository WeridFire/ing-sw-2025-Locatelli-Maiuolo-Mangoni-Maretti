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
                break;

            case "drawtile":
                getServer().drawTile(getClient());
        }
    }

    @Override
    void printScreenSpecificCommands() {
        printCommands(screenName,
                "timerflip | Flips the hourglass of the game.",
                "drawtile | Draws a tile from the covered tiles"
        );
    }

    @Override
    public CLIFrame getCLIRepresentation() {
        //TODO: think about the assemble game screen
        return null;
    }
}
