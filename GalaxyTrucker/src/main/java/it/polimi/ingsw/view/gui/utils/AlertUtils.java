package it.polimi.ingsw.view.gui.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

public class AlertUtils {

    public static boolean showConfirmation(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        ButtonType yes = new ButtonType("Yes");
        ButtonType no = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(yes, no);

        return alert.showAndWait().filter(response -> response == yes).isPresent();
    }

    private static void alertShow(Alert alert, String title, String content) {
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void showError(String title, String content) {
        alertShow(new Alert(Alert.AlertType.ERROR), title, content);
    }
    public static void showWarning(String title, String content) {
        alertShow(new Alert(Alert.AlertType.WARNING), title, content);
    }
    public static void showInfo(String title, String content) {
        alertShow(new Alert(Alert.AlertType.INFORMATION), title, content);
    }

}
