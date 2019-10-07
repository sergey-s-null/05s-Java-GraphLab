package sample;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class GraphAlert {
    public static void showAndWait(String contentString) {
        Alert alert = new Alert(Alert.AlertType.ERROR, contentString, ButtonType.OK);
        alert.setTitle("ლ(ಠ益ಠლ)");
        alert.setHeaderText("Ошибка");
        alert.showAndWait();
    }
}
