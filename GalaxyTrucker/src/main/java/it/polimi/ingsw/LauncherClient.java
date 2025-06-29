package it.polimi.ingsw;

import it.polimi.ingsw.util.Default;

import java.util.ArrayList;
import java.util.List;

public class LauncherClient {
    public static void main(String[] args) {
        List<String> newArgs = new ArrayList<>(List.of(args));
        if (!newArgs.contains("--tui") && !newArgs.contains("--gui")) {
            newArgs.addFirst(Default.USE_GUI ? "--gui" : "--tui");
        }
        Launcher.main(newArgs.toArray(new String[0]));
    }
}
