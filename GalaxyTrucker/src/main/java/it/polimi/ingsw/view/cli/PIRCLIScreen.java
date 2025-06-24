package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.controller.commandsProcessors.PIRCommandsProcessor;
import it.polimi.ingsw.controller.states.PIRState;
import it.polimi.ingsw.enums.Direction;
import it.polimi.ingsw.model.playerInput.PIRs.PIR;
import it.polimi.ingsw.model.playerInput.PIRs.PIRAddLoadables;
import it.polimi.ingsw.model.playerInput.PIRs.PIRRemoveLoadables;
import it.polimi.ingsw.network.GameClient;
import it.polimi.ingsw.model.playerInput.PIRType;
import it.polimi.ingsw.model.shipboard.LoadableType;

import java.util.*;

public class PIRCLIScreen extends CLIScreen {

	public PIRCLIScreen(GameClient gameClient) {
		super("turn", false, 10, new PIRCommandsProcessor(gameClient));
	}

	@Override
	protected boolean switchConditions() {
		return PIRState.isPIRActive();
	}

	@Override
	public CLIFrame getCLIRepresentation() {
		PIR activePIR = PIRState.getActivePIR();

		if (activePIR == null) {
			return new CLIFrame();
		}

		CLIFrame baseFrame = activePIR.getCLIRepresentation();

	/*
	THE SECTION BELOW IS SPECIFIC WITH THE ADD / REMOVE CARGO SCREEN.
	It can't be put inside of the clirepresentation itself of the PIR, because
	it needs to compute the "locally" allocated or removed cargo vs the total cargo
	to be handled, and show it dynamically. This part is simply put on top of the add or remove cargo screen.
	*/
		if (activePIR.getPIRType() == PIRType.ADD_CARGO) {

			PIRAddLoadables pirAdd = (PIRAddLoadables) activePIR;
			List<LoadableType> floatingLoadables = new ArrayList<>(pirAdd.getFloatingLoadables());

			for (List<LoadableType> allocated : PIRState.getLocalCargo().values()) {
				for (LoadableType item : allocated) {
					floatingLoadables.remove(item); // removes only one occurrence at a time
				}
			}

			CLIFrame cargoHeader = createHeader(ANSI.BACKGROUND_BLUE + ANSI.WHITE + " CARGO TO ALLOCATE " + ANSI.RESET);

			if (floatingLoadables.isEmpty()) {
				cargoHeader = cargoHeader.merge(new CLIFrame(ANSI.GREEN + "All cargo allocated!" + ANSI.RESET), Direction.SOUTH, 1);
			} else {
				for (LoadableType loadable : floatingLoadables) {
					cargoHeader = cargoHeader.merge(new CLIFrame(loadable.getUnicodeColoredString()), Direction.SOUTH, 1);
				}
			}

			baseFrame = cargoHeader.merge(baseFrame, Direction.SOUTH, 2);

		} else if (activePIR.getPIRType() == PIRType.REMOVE_CARGO) {

			PIRRemoveLoadables pirRemove = (PIRRemoveLoadables) activePIR;

			int currentCargo = pirRemove.getCargoAmount();
			int target = currentCargo - pirRemove.getAmountToRemove();

			CLIFrame removeHeader = createHeader(ANSI.BACKGROUND_RED + ANSI.WHITE + " CARGO TO REMOVE " + ANSI.RESET);

			if (currentCargo <= target) {
				removeHeader = removeHeader.merge(new CLIFrame(ANSI.GREEN + "All required cargo removed!" + ANSI.RESET), Direction.SOUTH, 1);
			} else {
				removeHeader = removeHeader.merge(
						new CLIFrame(" Remaining to remove: " + ANSI.YELLOW + (pirRemove.getAmountToRemove()) + ANSI.RESET),
						Direction.SOUTH, 1
				);
			}

			baseFrame = removeHeader.merge(baseFrame, Direction.SOUTH, 2);
		}

		return baseFrame;
	}

	private CLIFrame createHeader(String title) {
		return new CLIFrame(title).merge(new CLIFrame(""), Direction.SOUTH);
	}


}