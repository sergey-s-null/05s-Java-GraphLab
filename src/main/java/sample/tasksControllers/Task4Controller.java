package sample.tasksControllers;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import sample.Graph.Elements.Vertex;
import sample.Graph.GraphGroup;
import sample.GraphAlert;
import sample.GraphAlgorithms;
import sample.MatrixView.MatrixView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Task4Controller extends TaskController {
    @FXML private VBox root;
    @FXML private TextField eccentricitiesField, radiusField, diameterField,
            verticesDegreesField;
    private boolean haveResult = false;

    @Override
    public Parent getRoot() {
        return root;
    }

    //------------|
    //   events   |
    //------------|
    @FXML private void onStart() {
        if (!startIfCan.apply(this)) {
            GraphAlert.showErrorAndWait("Невозможно начать.");
            return;
        }

        GraphGroup graphGroup = currentGraph.get().orElse(null);
        MatrixView matrixView = currentMatrixView.get().orElse(null);
        if (graphGroup == null || matrixView == null) {
            GraphAlert.showInfoAndWait("Граф не выбран.");
            end.run();
            return;
        }
        if (!validateGraph(graphGroup)) {
            end.run();
            return;
        }

        GraphAlgorithms.GraphCharacteristic characteristic = GraphAlgorithms.graphCharacteristic(
                matrixView.getMatrix());
        List<Integer> verticesDegrees = new ArrayList<>();
        for (Vertex vertex : graphGroup.getVertices())
            verticesDegrees.add(vertex.getDegree());

        setResult(characteristic, verticesDegrees);
        haveResult = true;

        end.run();
    }

    @FXML private void onSave() {
        if (!haveResult) {
            GraphAlert.showErrorAndWait("Нечего сохранять.");
            return;
        }
        File file = fileChooser.showSaveDialog(null);
        if (file == null)
            return;

        try (FileWriter writer = new FileWriter(file)) {
            writer.write("Эксцентриситеты вершин: ");
            writer.write(eccentricitiesField.getText());
            writer.write("\n");

            writer.write("Радиус: ");
            writer.write(radiusField.getText());
            writer.write("\n");

            writer.write("Диаметр: ");
            writer.write(diameterField.getText());
            writer.write("\n");

            writer.write("Вектор степеней вершин: ");
            writer.write(verticesDegreesField.getText());
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
        if (!graphGroup.isAllEdgesWeightsPositive()) {
            GraphAlert.showErrorAndWait("Все ребра должны быть положительны.");
            return false;
        }
        return true;
    }

    private void setResult(GraphAlgorithms.GraphCharacteristic characteristic,
                           List<Integer> verticesDegrees)
    {
        eccentricitiesField.setText(vectorToString(characteristic.eccentricities));
        radiusField.setText(Double.toString(characteristic.radius));
        diameterField.setText(Double.toString(characteristic.diameter));
        verticesDegreesField.setText(vectorToString(verticesDegrees));
    }

    private String vectorToString(List<? extends Number> eccentricities) {
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        boolean isFirst = true;
        for (Number eccentricity : eccentricities) {
            if (!isFirst)
                builder.append(", ");
            isFirst = false;
            builder.append(eccentricity);
        }
        builder.append(")");
        return builder.toString();
    }
}
