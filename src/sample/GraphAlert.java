package sample;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public class GraphAlert {
    public static void showAndWait(String contentString) {
        Alert alert = new Alert(Alert.AlertType.ERROR, contentString, ButtonType.OK);
        alert.setTitle("ლ(ಠ益ಠლ)");
        alert.setHeaderText("Ошибка");
        alert.showAndWait();
    }

    public static ButtonType confirmTabClose(String tabName) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Сохранить граф \"" + tabName + "\" перед закрытием?",
                ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
        alert.setTitle("(⊙ω⊙)");
        alert.setHeaderText(tabName);

        Optional<ButtonType> result = alert.showAndWait();
        return result.orElse(ButtonType.CANCEL);
    }
}
