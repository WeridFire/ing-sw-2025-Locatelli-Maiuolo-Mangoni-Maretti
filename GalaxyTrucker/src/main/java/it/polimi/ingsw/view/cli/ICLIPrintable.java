package it.polimi.ingsw.view.cli;

/**
 * Interface representing an object that can be printed in a CLI format.
 */
public interface ICLIPrintable {
    /**
     * Generates a CLI representation of the implementing object.
     *
     * @return A {@link CLIFrame} containing the CLI representation.
     */
    CLIFrame getCLIRepresentation();
}

