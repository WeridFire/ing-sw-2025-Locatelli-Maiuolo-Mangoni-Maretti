package it.polimi.ingsw.view.gui.components;

import it.polimi.ingsw.controller.states.PIRState;
import it.polimi.ingsw.model.player.Player;
import it.polimi.ingsw.model.shipboard.tiles.MainCabinTile;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class TurnContainer extends StackPane {

    private TextFlow label;
    private Text normalText = new Text("Player: ");
    private Text playerText = new Text("...");

    private static TurnContainer instance;
    public static TurnContainer getInstance() {
        if (instance == null) {
            instance = new TurnContainer();
        }
        instance.refresh();
        return instance;
    }

    private TurnContainer() {
        super();
    }

    public void refresh() {
        Player p = PIRState.getTurnPlayer();
        if (p != null){
            this.getChildren().clear();

            label = new TextFlow();

            label.setMaxHeight(30);
            label.setMinHeight(30);
            label.setMaxWidth(CardContainer.FIXED_WIDTH);
            label.setMinWidth(CardContainer.FIXED_WIDTH);

            playerText = new Text(p.getUsername());
            playerText.setFill(MainCabinTile.Color.toPaint(p.getColor()));

            label = new TextFlow(normalText, playerText);
            normalText.setStyle("-fx-font-weight: bold;");
            normalText.setFill(Paint.valueOf("white"));

            this.getChildren().add(label);
        }
    }
}
