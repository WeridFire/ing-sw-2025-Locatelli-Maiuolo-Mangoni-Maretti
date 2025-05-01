package it.polimi.ingsw.playerInput.PIRs;

import it.polimi.ingsw.player.Player;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class PIRAtomicSequence implements Serializable {
    private final Player player;
    private final Set<PIR> pirs;

    public PIRAtomicSequence(Player player) {
        this.player = player;
        pirs = new HashSet<>();
    }

    public Player getPlayer() {
        return player;
    }

    public boolean addPlayerInputRequest(PIR pir) {
        if (player != pir.getCurrentPlayer()) return false;
        pirs.add(pir);
        return true;
    }

    public void clear() {
        pirs.clear();
    }

    public boolean isValid(PIR pir) {
        return pirs.contains(pir);
    }
}
