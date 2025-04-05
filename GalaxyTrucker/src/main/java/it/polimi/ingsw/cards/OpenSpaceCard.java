package it.polimi.ingsw.cards;

import it.polimi.ingsw.GamesHandler;
import it.polimi.ingsw.enums.PowerType;
import it.polimi.ingsw.game.Game;
import it.polimi.ingsw.game.GameData;
import it.polimi.ingsw.player.Player;
import it.polimi.ingsw.playerInput.PIRUtils;
import it.polimi.ingsw.view.cli.CLIFrame;

import java.util.UUID;

public class OpenSpaceCard extends Card{
	/**
	 * Instances a card.
	 *
	 * @param textureName The name of the texture of the card.
	 * @param level       The level of this card.
	 */
	public OpenSpaceCard(String textureName, int level) {
		super(textureName, level);
	}

	/**
	 * Iterates each player, requires how many thrust power they wanna use, and moves accordingly.
	 */
	@Override
	public void playEffect(GameData game) {
		for(Player p : game.getPlayers()){
			float steps = PIRUtils.runPlayerPowerTilesActivationInteraction(p, game, PowerType.THRUST);
			//Here we just round, but know for sure that the thrusters tiles won't return numbers with decimals.
			game.movePlayerForward(p, Math.round(steps));
		}
	}

	/**
	 * Generates a CLI representation of the implementing object.
	 *
	 * @return A {@link CLIFrame} containing the CLI representation.
	 */
	@Override
	public CLIFrame getCLIRepresentation() {
		// TODO
		return null;
	}
}
