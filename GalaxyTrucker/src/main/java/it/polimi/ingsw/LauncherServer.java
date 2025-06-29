package it.polimi.ingsw;

import java.util.ArrayList;
import java.util.List;

public class LauncherServer {
    public static void main(String[] args) {
        List<String> newArgs = new ArrayList<>(List.of(args));
        newArgs.addFirst("--server");
        Launcher.main(newArgs.toArray(new String[0]));
    }
}