package sample;

import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import sample.Graph.GraphGroup;

public class InputDialog extends TextInputDialog {
    private Alert alert = new Alert(Alert.AlertType.ERROR);

    public InputDialog() {
        alert.setTitle("ლ(ಠ益ಠლ)");
        alert.setHeaderText("Ошибка");
    }

    public Double getGraphWidth(double initWidth) {
        setTitle("¯\\_(ツ)_/¯");
        setHeaderText("Введите ширину поля");
        setContentText("Ширина:");
        getEditor().setText(Double.toString(initWidth));

        while (true) {
            showAndWait();
            String res = getResult();
            if (res == null)
                return null;

            try {
                double doubleRes = Double.parseDouble(res);
                if (doubleRes < GraphGroup.minWidth) {
                    alert.setContentText("Минимальная ширина: " + GraphGroup.minWidth);
                    alert.showAndWait();
                }
                else {
                    return doubleRes;
                }
            }
            catch (NumberFormatException e) {
                alert.setContentText("Неверный формат числа.");
                alert.showAndWait();
            }
        }
    }

    public Double getGraphHeight(double initHeight) {
        setTitle("༼ つ ◕_◕ ༽つ");
        setHeaderText("Введите высоту поля");
        setContentText("Высота:");
        getEditor().setText(Double.toString(initHeight));

        while (true) {
            showAndWait();
            String res = getResult();
            if (res == null)
                return null;

            try {
                double doubleRes = Double.parseDouble(res);
                if (doubleRes < GraphGroup.minHeight) {
                    alert.setContentText("Минимальная высота: " + GraphGroup.minHeight);
                    alert.showAndWait();
                }
                else {
                    return doubleRes;
                }
            }
            catch (NumberFormatException e) {
                alert.setContentText("Неверный формат числа.");
                alert.showAndWait();
            }
        }
    }
}
