package sample.Graph;

import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import sample.Graph.Elements.Vertex;


public class GraphInputDialog extends TextInputDialog {
    private Alert alert = new Alert(Alert.AlertType.ERROR);

    public GraphInputDialog() {
        alert.setTitle("ᕦ(ò_óˇ)ᕤ");
        alert.setHeaderText("Ошибка");
    }

    public String getVertexName(String defaulName) {
        setTitle("Что тут обычно пишется?");
        setHeaderText("Введите имя вершины (длина от 1 до 10, запрещенные символы: \"%()[]{},\" и пробельные)");
        setContentText("Имя:");
        getEditor().setText(defaulName);

        while (true) {
            showAndWait();
            String res = getResult();
            if (res == null)
                return null;

            if (!Vertex.isNameValid(res)) {
                alert.setContentText("Неверное имя.");
                alert.showAndWait();
                continue;
            }
            return res;
        }
    }

    public Double getEdgeWeight(double defaultWeight) {
        setTitle("Так и не понял, что тут писать.");
        setHeaderText("Введите вес ребра");
        setContentText("Вес:");
        getEditor().setText(Double.toString(defaultWeight));

        while (true) {
            showAndWait();
            String res = getResult();
            if (res == null)
                return null;

            try {
                return Double.parseDouble(res);
            }
            catch (NumberFormatException e) {
                alert.setContentText("Неверный формат числа.");
                alert.showAndWait();
            }
        }
    }


}
