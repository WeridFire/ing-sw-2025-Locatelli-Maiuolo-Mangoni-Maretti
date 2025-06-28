package it.polimi.ingsw.view.gui.UIs;

import it.polimi.ingsw.network.messages.ClientUpdate;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class ScoreUI implements INodeRefreshableOnUpdateUI{

    private StackPane root = new StackPane();


    public ScoreUI() {
        Label scoreLabel = new Label("THANKS FOR PLAYING!");
        root.getChildren().add(scoreLabel);
    }

    @Override
    public Node getLayout() {
        return root;
    }

    @Override
    public void refreshOnUpdate(ClientUpdate update) {

    }
}
