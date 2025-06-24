package it.polimi.ingsw.view.gui.components;

import it.polimi.ingsw.model.playerInput.PIRType;
import it.polimi.ingsw.model.playerInput.PIRs.*;
import javafx.scene.layout.StackPane;

public class PIRContainer extends StackPane {

    private PIR pir;
    private PIRType type;

    private String labelText;

    public PIRContainer() {}

    public void setPir(PIR pir) {
        this.pir = pir;
        this.type = pir.getPIRType();

        switch (this.type) {
            case PIRType.ACTIVATE_TILE -> handleActivateTilePir();
            case PIRType.ADD_CARGO -> handleAddCargoPir();
            case PIRType.REMOVE_CARGO -> handleRemoveCargoPir();
            case PIRType.CHOICE -> handleChoicePir();
            case PIRType.DELAY -> handleDelayPir();
        }
    }

    public void handleActivateTilePir() {
        labelText = "Activate Tile";
        PIRActivateTiles castedPir = (PIRActivateTiles) pir;
        // TODO: implementa logica specifica
    }

    public void handleAddCargoPir() {
        labelText = "Add Cargo";
        PIRAddLoadables castedPir = (PIRAddLoadables) pir;
        // TODO: implementa logica specifica
    }

    public void handleRemoveCargoPir() {
        labelText = "Remove Cargo";
        PIRRemoveLoadables castedPir = (PIRRemoveLoadables) pir;
        // TODO: implementa logica specifica
    }

    public void handleChoicePir() {
        labelText = "Choice";
        PIRMultipleChoice castedPir = (PIRMultipleChoice) pir;
        // TODO: implementa logica specifica
    }


    public void handleDelayPir() {
        labelText = "Delay";
        PIRDelay castedPir = (PIRDelay) pir;
        // TODO: implementa logica specifica
    }

}
