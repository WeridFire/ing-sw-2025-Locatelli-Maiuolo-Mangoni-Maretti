package it.polimi.ingsw.view.gui.components;

import it.polimi.ingsw.controller.states.CommonState;
import it.polimi.ingsw.model.playerInput.PIRType;
import it.polimi.ingsw.model.playerInput.PIRs.*;
import it.polimi.ingsw.model.playerInput.exceptions.WrongPlayerTurnException;
import it.polimi.ingsw.model.shipboard.LoadableType;
import it.polimi.ingsw.view.gui.UIs.AdventureUI;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class PIRContainer extends StackPane {

    private PIR pir;
    private PIRType type;

    private Label label;
    private String labelText;
    private final List<Button> buttons = new ArrayList<>();

    private final VBox content;

    public PIRContainer() {
        // Initialize content in the constructor
        this.content = new VBox();
        this.content.setSpacing(10); // Add some spacing for better layout
        this.getChildren().add(content);
    }

    public void setPir(PIR pir) {
        this.pir = pir;
        this.type = pir.getPIRType();
        Platform.runLater(() -> {
            switch (this.type) {
                case PIRType.ACTIVATE_TILE -> handleActivateTilePir();
                case PIRType.ADD_CARGO -> handleAddCargoPir();
                case PIRType.REMOVE_CARGO -> handleRemoveCargoPir();
                case PIRType.CHOICE -> handleChoicePir();
                case PIRType.DELAY -> handleDelayPir();
            }
        });
    }

    public void handleActivateTilePir() {
        labelText = "Activate Tile";
        label = new Label(labelText);
        content.getChildren().clear();
        content.getChildren().add(label);
        PIRActivateTiles castedPir = (PIRActivateTiles) pir;
        // TODO: implementa logica specifica
    }

    public void handleAddCargoPir() {
        PIRAddLoadables castedPir = (PIRAddLoadables) pir;
        ShipGrid shipGrid = AdventureUI.getInstance().getShipGrid();
        shipGrid.setActiveCells(castedPir.getHighlightMask(), true, false);
        for (LoadableType loadable: castedPir.getFloatingLoadables()){
            AdventureUI.getInstance().getLoadableContainer().addLoadableObject(loadable);
        }
    }

    public void handleRemoveCargoPir() {
        labelText = "Remove Cargo";
        label = new Label(labelText);
        content.getChildren().clear();
        content.getChildren().add(label);
        PIRRemoveLoadables castedPir = (PIRRemoveLoadables) pir;
        // TODO: implementa logica specifica
    }

    public void handleChoicePir() {
        PIRMultipleChoice castedPir = (PIRMultipleChoice) pir;
        labelText = castedPir.getChoiceMessage();
        label = new Label(labelText);

        buttons.clear();

        List<String> options = List.of(castedPir.getPossibleOptions());
        for (int i = 0; i < options.size(); i++) {
            String optionText = options.get(i);
            final int choiceIndex = i;

            Button buff = new Button(optionText);
            buff.setOnMouseClicked(event -> {
                try {
                    castedPir.makeChoice(CommonState.getPlayer(), choiceIndex);
                    AdventureUI.getInstance().hidePirContainer();
                } catch (WrongPlayerTurnException e) {
                    System.err.println("Wrong player turn, choice not registered.");
                }
                this.setVisible(false);
                // If you want to remove it from the parent, do it like this:
                if (this.getParent() != null) {
                    ((Pane) this.getParent()).getChildren().remove(this);
                }
            });

            buttons.add(buff);
        }

        content.getChildren().clear();
        content.getChildren().add(label);
        content.getChildren().addAll(buttons);

        this.setVisible(true);
    }

    public void handleDelayPir() {
        PIRDelay castedPir = (PIRDelay) pir;
        this.labelText = castedPir.getMessage();
        label = new Label(labelText);

        content.getChildren().clear();
        content.getChildren().add(label);
    }
}