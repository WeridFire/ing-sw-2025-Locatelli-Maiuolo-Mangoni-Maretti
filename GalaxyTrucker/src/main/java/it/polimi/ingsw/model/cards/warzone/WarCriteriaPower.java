package it.polimi.ingsw.model.cards.warzone;

import it.polimi.ingsw.enums.PowerType;
import it.polimi.ingsw.model.game.GameData;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.playerInput.PIRUtils;
import it.polimi.ingsw.view.cli.ANSI;

import java.util.HashMap;
import java.util.Map;

public class WarCriteriaPower implements WarCriteria {

    private PowerType powerType;

    private Map<Player, Float> powerMap = new HashMap<Player, Float>();

    public WarCriteriaPower(PowerType powerType) {
        this.powerType = powerType;
    }

    @Override
    public String getName() {
        return "Weakest " + ANSI.BACKGROUND_BLACK + ANSI.RED + (powerType == PowerType.THRUST
                ? "Engines"
                : "Cannons") + ANSI.RESET;
    }

    @Override
    public Player computeCriteria(GameData game) {
        game.getPlayersInFlight().stream().filter(p -> !p.hasRequestedEndFlight()).forEach(p -> {
            float activatedPower = PIRUtils.runPlayerPowerTilesActivationInteraction(p, game, this.powerType);
            this.powerMap.put(p, activatedPower);
        });
		return game.getPlayersInFlight().stream()
                .max(this).orElse(null);
    }


    @Override
    public int compare(Player p1, Player p2) {
        // Use getTotalPower with 0 extraPower for fair comparison
        float power1 = powerMap.get(p1);
        float power2 = powerMap.get(p2);

        if (power1 == power2) {
            // Invert order to break ties: higher order loses to lower order
            return Integer.compare(p2.getOrder(), p1.getOrder());
        }

        return Float.compare(power2, power1); // Descending order: higher power first
    }
}
