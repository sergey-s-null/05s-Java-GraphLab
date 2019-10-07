package sample;

import javafx.scene.control.TextInputDialog;
import sample.Graph.GraphGroup;

public class InputDialog extends TextInputDialog {

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
                    GraphAlert.showAndWait("Минимальная ширина: " +
                            GraphGroup.minWidth);
                }
                else if (doubleRes > GraphGroup.maxWidth) {
                    GraphAlert.showAndWait("Максимальная ширина: " +
                            GraphGroup.maxWidth);
                }
                else {
                    return doubleRes;
                }
            }
            catch (NumberFormatException e) {
                GraphAlert.showAndWait("Неверный формат числа.");
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
                    GraphAlert.showAndWait("Минимальная высота: " +
                            GraphGroup.minHeight);
                }
                else if (doubleRes > GraphGroup.maxHeight) {
                    GraphAlert.showAndWait("Максимальная высота: " +
                            GraphGroup.maxHeight);
                }
                else {
                    return doubleRes;
                }
            }
            catch (NumberFormatException e) {
                GraphAlert.showAndWait("Неверный формат числа.");
            }
        }
    }
}
