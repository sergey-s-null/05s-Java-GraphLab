package sample.Graph;

import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import sample.Graph.Elements.Vertex;
import sample.GraphAlert;


public class GraphInputDialog extends TextInputDialog {
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
                GraphAlert.showAndWait("Неверное имя.");
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
                double weight = Double.parseDouble(res);
                if (weight == 0) {
                    GraphAlert.showAndWait("Вес не может быть равен 0.");
                }
                else {
                    return weight;
                }
            }
            catch (NumberFormatException e) {
                GraphAlert.showAndWait("Неверный формат числа.");
            }
        }
    }


}
