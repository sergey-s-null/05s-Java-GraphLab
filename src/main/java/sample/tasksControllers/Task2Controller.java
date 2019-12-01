package sample.tasksControllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import sample.Graph.GraphGroup;
import sample.Graph.GraphPath;
import sample.GraphAlert;
import sample.GraphAlgorithms;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Task2Controller extends TaskController {
    @FXML private VBox root;
    @FXML private TextField resultField, pathLengthField, pathField;
    private boolean haveResult = false;

    @Override
    public VBox getRoot() {
        return root;
    }

    //------------|
    //   events   |
    //------------|
    @FXML private void onBreadthSearch() {
        if (!startIfCan.apply(this)) {
            GraphAlert.showErrorAndWait("Невозможно начать.");
            return;
        }

        GraphGroup graphGroup = currentGraph.get().orElse(null);
        if (graphGroup == null) {
            GraphAlert.showInfoAndWait("Граф не выбран.");
            end.run();
            return;
        }
        if (!validateGraph(graphGroup)) {
            end.run();
            return;
        }

        GraphAlert.showInfoAndWait("Выберите две вершины.");
        graphGroup.setOnTwoVerticesSelected((vertexFrom, vertexTo) -> {
            GraphPath path = GraphAlgorithms.breadthSearch(vertexFrom, vertexTo);
            // TODO move to func saveResult (maybe after 2 another algs)
            if (path == null) {
                resultField.setText("Маршрут не найден.");
                pathLengthField.setText("-");
                pathField.setText("-");
            }
            else {
                graphGroup.setElementsPath(path);
                resultField.setText("Маршрут найден.");
                pathLengthField.setText(Integer.toString(path.getEdgeCount()));
                pathField.setText(path.toString());
            }
            haveResult = (path != null);
            graphGroup.setOnTwoVerticesSelected(null);
            end.run();
        });
        graphGroup.setCurrentAction(GraphGroup.Action.SelectTwoVertices);
    }

    @FXML private void onAStar() {
        // TODO
    }

    @FXML private void onIterativeDeepeningAStar() {
        // TODO
    }

    @FXML private void onSave() {
        if (!haveResult) {
            GraphAlert.showErrorAndWait("Нечего сохранить.");
            return;
        }
        File file = fileChooser.showSaveDialog(null);
        if (file == null)
            return;

        try (FileWriter writer = new FileWriter(file)) {
            writer.write("Результат работы алгоритма поиска в ширину (2).\n");
            writer.write("Длина маршрута: ");
            writer.write(pathLengthField.getText());
            writer.write("\n");
            writer.write("Маршрут: ");
            writer.write(pathField.getText());
            writer.write("\n");
        }
        catch (IOException e) {
            GraphAlert.showErrorAndWait("Ошибка сохранения файла.");
        }
    }

    // methods
    private boolean validateGraph(GraphGroup graphGroup) {
        if (graphGroup.getVerticesCount() < 2) {
            GraphAlert.showErrorAndWait("Кол-во вершин меньше 2.");
            return false;
        }
        if (!graphGroup.isAllEdgesWeightsUnit()) {
            GraphAlert.showErrorAndWait("Все веса должны быть равны 1.");
            return false;
        }
        return true;
    }

}
