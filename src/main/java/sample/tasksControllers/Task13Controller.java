package sample.tasksControllers;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import sample.Graph.Elements.Vertex;
import sample.Graph.GraphGroup;
import sample.Graph.GraphPath;
import sample.GraphAlert;
import sample.GraphAlgorithms;
import sample.MatrixView.MatrixView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

public class Task13Controller extends TaskController {
    @FXML private VBox root;
    @FXML private TextField hasCyclesField, centersField, preferField, minCycleField;
    private boolean hasPreferResult = false;

    @Override
    public Parent getRoot() {
        return root;
    }

    @FXML private void onStart() {
        if (!startIfCan.apply(this)) {
            GraphAlert.showInfoAndWait("Невозможно начать.");
            return;
        }
        execute();
        end.run();
    }

    private void execute() {
        GraphGroup graphGroup = currentGraph.get().orElse(null);
        MatrixView matrixView = currentMatrixView.get().orElse(null);
        if (graphGroup == null || matrixView == null) return;

        if (graphGroup.getVerticesCount() < 3) {
            GraphAlert.showInfoAndWait("Кол-во вершин должно быть больше или равно 3.");
            return;
        }
        if (graphGroup.isMultiGraph()) {
            GraphAlert.showInfoAndWait("Граф не должен быть мультиграфом.");
            return;
        }
        if (graphGroup.isGraphOriented()) {
            GraphAlert.showInfoAndWait("Граф должен быть неориентированным.");
            return;
        }
        if (graphGroup.hasUnaryEdges()) {
            GraphAlert.showInfoAndWait("Граф не должен содержать петель.");
            return;
        }
        if (GraphAlgorithms.findConnectivityComponents(matrixView.getMatrix()).size() != 1) {
            GraphAlert.showInfoAndWait("Граф должен быть связным.");
            return;
        }

        if (GraphAlgorithms.hasCycle(graphGroup)) {
            GraphPath minCycle = GraphAlgorithms.findMinCycle(graphGroup).orElse(null);
            if (minCycle == null) {
                setEmptyResult();
                return;
            }
            graphGroup.setElementsPath(minCycle);
            setResult(minCycle);
        }
        else {
            graphGroup.clearCurrentPath();
            List<Integer> preferCode = GraphAlgorithms.makeCodePrefer(graphGroup).orElse(null);
            if (preferCode == null) {
                GraphAlert.showErrorAndWait("Ошибка вычисления кода Прюфера.");
                setEmptyResult();
                return;
            }

            List<Integer> centersIndices = GraphAlgorithms.findTreeCenters(matrixView.getMatrix());
            List<Vertex> centers = centersIndices.stream().map(i -> graphGroup.getVertices().get(i))
                    .collect(Collectors.toList());

            setResult(centers, preferCode);
        }
    }

    private void setEmptyResult() {
        hasCyclesField.setText("");
        centersField.setText("");
        preferField.setText("");
        minCycleField.setText("");
        hasPreferResult = false;
    }

    private void setResult(GraphPath minCycle) {
        hasCyclesField.setText("Присутствуют");
        centersField.setText("-");
        preferField.setText("-");
        minCycleField.setText(minCycle.toString());
        hasPreferResult = false;
    }

    private void setResult(List<Vertex> centers, List<Integer> preferCode) {
        hasCyclesField.setText("Отсутствуют");
        centersField.setText(beautifyVertices(centers));
        preferField.setText(preferCode.toString());
        minCycleField.setText("-");
        hasPreferResult = true;
    }

    private String beautifyVertices(List<Vertex> vertices) {
        StringBuilder builder = new StringBuilder();
        for (Vertex vertex : vertices) {
            if (builder.length() > 0)
                builder.append(", ");
            builder.append(vertex.getName());
        }
        return builder.toString();
    }

    @FXML private void onSavePrefer() {
        if (!hasPreferResult) return;
        File file = fileChooser.showSaveDialog(null);
        if (file == null) return;

        try (BufferedWriter writer = Files.newBufferedWriter(file.toPath())) {
            writer.write("Prefer code: ");
            writer.write(preferField.getText());
        }
        catch (IOException e) {
            GraphAlert.showErrorAndWait("Ошибка сохранения.");
        }
    }
}
