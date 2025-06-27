package it.polimi.ingsw.view.gui.components;

import it.polimi.ingsw.model.playerInput.PIRType;
import it.polimi.ingsw.model.playerInput.PIRs.*;
import it.polimi.ingsw.model.shipboard.LoadableType;
import it.polimi.ingsw.view.gui.UIs.AdventureUI;
import it.polimi.ingsw.view.gui.managers.ClientManager;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;


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
        this.content.setAlignment(Pos.CENTER);

        // Add a more suitable background for dialog
        this.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 5;");

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
        content.getChildren().add(getLabel("Activate Tiles!"));
        content.getChildren().clear();
        PIRActivateTiles castedPir = (PIRActivateTiles) pir;

        //highlight the possible choices to be activated
        ShipGrid shipGrid = AdventureUI.getInstance().getShipGrid();
        shipGrid.setActiveCells(castedPir.getHighlightMask(), false, false);
        addCloseButton(false);

        Button butt = new Button("Confirm Activation");
        butt.setOnMouseClicked(e -> {
            Platform.runLater(() -> {
                if(!shipGrid.getCellsToActivate().isEmpty()){
                    ClientManager.getInstance().simulateCommand("activate", formatCoordinates(shipGrid.getCellsToActivate()));
                    shipGrid.getCellsToActivate().clear();
                    AdventureUI.getInstance().getCardPane().getChildren().remove(butt);
                }
            });
        });
        AdventureUI.getInstance().getCardPane().getChildren().add(butt);
    }

    public void handleAddCargoPir() {
        content.getChildren().add(getLabel("HANDLEADDCARGO"));
        PIRAddLoadables castedPir = (PIRAddLoadables) pir;
        ShipGrid shipGrid = AdventureUI.getInstance().getShipGrid();
        shipGrid.setActiveCells(castedPir.getHighlightMask(), true, false);
        for (LoadableType loadable: castedPir.getFloatingLoadables()){
            AdventureUI.getInstance().getLoadableContainer().addLoadableObject(loadable);
        }
    }

    public void handleRemoveCargoPir() {
        content.getChildren().add(getLabel("HANDLEREMOVECARGO"));
        content.getChildren().clear();
        PIRRemoveLoadables castedPir = (PIRRemoveLoadables) pir;

        ShipGrid shipGrid = AdventureUI.getInstance().getShipGrid();
        shipGrid.setActiveCells(castedPir.getHighlightMask(), false, true);
        addCloseButton(false);
    }

    public void handleChoicePir() {
        PIRMultipleChoice castedPir = (PIRMultipleChoice) pir;
        label = getLabel(castedPir.getChoiceMessage());

        buttons.clear();

        List<String> options = List.of(castedPir.getPossibleOptions());
        for (int i = 0; i < options.size(); i++) {
            String optionText = options.get(i);
            final int choiceIndex = i;

            Button buff = new Button(optionText);
            buff.setViewOrder(-1);
            // Add some styling to make buttons more visible
            buff.setStyle("-fx-padding: 10; -fx-min-width: 120;");
            buff.setOnMouseClicked(event -> {
                Platform.runLater(() -> {
                    ClientManager.getInstance().simulateCommand("choose", String.valueOf(choiceIndex));
                });

                AdventureUI.getInstance().hidePirContainer();
            });

            buttons.add(buff);
        }

        content.getChildren().clear();
        content.getChildren().add(label);
        content.getChildren().addAll(buttons);
    }

    public void handleDelayPir() {
        PIRDelay castedPir = (PIRDelay) pir;
        label = getLabel(castedPir.getMessage());

        content.getChildren().clear();
        content.getChildren().add(label);

        addCloseButton(false);
    }

    private Label getLabel(String labelText) {
        Label labelObj = new Label(labelText); // Using the parameter instead of the field
        labelObj.setWrapText(true);
        labelObj.setStyle("-fx-font-weight: bold; -fx-text-fill: black;"); // Changed to black text
        labelObj.setViewOrder(-1);
        return labelObj;
    }




    private void addCloseButton(boolean endsTurn) {
        Button close = new Button("Close");
        close.setOnMouseClicked(event -> {
            if (endsTurn) {
                Platform.runLater(() -> {
                    ClientManager.getInstance().simulateCommand("endTurn");
                });
            }
            AdventureUI.getInstance().hidePirContainer();
            AdventureUI.getInstance().getShipGrid().update();

        });
        content.getChildren().add(close);
    }

    public static String formatCoordinates(List<ShipCell> cells) {
        StringBuilder sb = new StringBuilder();
        for (ShipCell obj : cells) {
            String r = obj.getLogicalRow(); // Dummy getter
            String c = obj.getLogicalColumn(); // Dummy getter
            sb.append("(").append(r).append(",").append(c).append(") ");
        }
        // Remove trailing space if needed
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

}