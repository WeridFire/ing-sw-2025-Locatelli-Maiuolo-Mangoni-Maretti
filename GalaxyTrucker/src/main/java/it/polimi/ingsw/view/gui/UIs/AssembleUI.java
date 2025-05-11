package it.polimi.ingsw.view.gui.UIs;

import it.polimi.ingsw.network.messages.ClientUpdate;
import it.polimi.ingsw.view.gui.helpers.Asset;
import it.polimi.ingsw.view.gui.helpers.AssetHandler;
import it.polimi.ingsw.view.gui.helpers.Path;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class AssembleUI implements INodeRefreshableOnUpdateUI {

    private GridPane grid;

    public AssembleUI() {
        // Create the grid pane
        grid = new GridPane();

        // Top row: merged cells (span 2 columns)
        Node topContent = show();
        grid.add(topContent, 0, 0, 2, 1);
        GridPane.setHgrow(topContent, Priority.ALWAYS);
        GridPane.setVgrow(topContent, Priority.ALWAYS);

        // Bottom left
        ImageView shipBg = AssetHandler.loadImage(Asset.SHIP.toString());
        shipBg.fitWidthProperty().bind(
                grid.widthProperty()
                        .subtract(grid.getHgap())
                        .divide(2)
        );
        grid.add(shipBg, 0, 1);
        GridPane.setHgrow(shipBg, Priority.ALWAYS);
        GridPane.setVgrow(shipBg, Priority.ALWAYS);

        // Bottom right
        ImageView imageView2 = AssetHandler.loadImage(Asset.SHIP.toString()); //test
        imageView2.fitWidthProperty().bind(
                grid.widthProperty()
                        .subtract(grid.getHgap())
                        .divide(2)
        );
        grid.add(imageView2, 1, 1);
        GridPane.setHgrow(imageView2, Priority.ALWAYS);
        GridPane.setVgrow(imageView2, Priority.ALWAYS);
    }

    public GridPane getLayout() {
        return grid;
    }

    @Override
    public void refreshOnUpdate(ClientUpdate update) {
        // TODO: update UI based on ClientUpdate
    }

    /**
     * Example show() method stub. Should return the Node to display in the top merged cell.
     */
    private Node show() {
        // Replace this stub with actual implementation
        GridPane pane = new GridPane();
        return pane;
    }
}
