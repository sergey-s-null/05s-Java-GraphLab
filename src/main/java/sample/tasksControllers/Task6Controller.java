package sample.tasksControllers;

import Jama.Matrix;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import sample.Graph.Elements.Vertex;
import sample.Graph.GraphGroup;
import sample.GraphAlert;
import sample.GraphAlgorithms;
import sample.MatrixView.MatrixView;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Task6Controller extends TaskController {
    @FXML private VBox root;
    @FXML private TextField graphTypeField, connectivityField,
            strongComponentsField, weakComponentsField, bridgesField, hingesField;

    @Override
    public Parent getRoot() {
        return root;
    }

    @FXML private void onStart() {
        if (!startIfCan.apply(this)) {
            GraphAlert.showErrorAndWait("Невозможно начать.");
            return;
        }
        execute();
        end.run();
    }

    private void execute() {
        GraphGroup graphGroup = currentGraph.get().orElse(null);
        MatrixView matrixView = currentMatrixView.get().orElse(null);
        if (graphGroup == null || matrixView == null)
            return;

        if (!graphGroup.isAllEdgesWeightsPositive()) {
            GraphAlert.showInfoAndWait("Все ребра должны быть положительны.");
            return;
        }
        if (graphGroup.getVertices().size() < 2) {
            GraphAlert.showInfoAndWait("Кол-во вершин должно быть >= 2.");
            return;
        }
        if (graphGroup.isMultiGraph()) {
            GraphAlert.showInfoAndWait("Граф не должен быть мультиграфом.");
            return;
        }

        List<Vertex> vertices = graphGroup.getVertices();

        Matrix adjMatrix = matrixView.getMatrix();
        List<List<String> > strongComponents = indicesToNames(
                GraphAlgorithms.findConnectivityComponents(adjMatrix), vertices);
        int bridgesCount = findBridgesCount(adjMatrix, strongComponents.size());
        int hingesCount = findHingesCount(adjMatrix, strongComponents.size());

        strongComponentsField.setText(beautifyComponents(strongComponents));
        bridgesField.setText(Integer.toString(bridgesCount));
        hingesField.setText(Integer.toString(hingesCount));
        if (!graphGroup.isGraphOriented()) {
            graphTypeField.setText("Неориентированный");
            connectivityField.setText(strongComponents.size() == 1 ? "Связный" : "Несвязный");
            weakComponentsField.setText("-");
        }
        else {
            Matrix adjSymmetric = GraphAlgorithms.makeMtxSymmetric(adjMatrix, (a, b) -> a == 0 ? b : a);
            List<List<String> > weakComponents = indicesToNames(
                    GraphAlgorithms.findConnectivityComponents(adjSymmetric), vertices);
            graphTypeField.setText("Ориентированный");
            if (strongComponents.size() == 1)
                connectivityField.setText("Сильно-связный");
            else if (weakComponents.size() == 1)
                connectivityField.setText("Слабо-связный");
            else
                connectivityField.setText("Несвязный");
            weakComponentsField.setText(beautifyComponents(weakComponents));
        }
    }

    private int findBridgesCount(Matrix adjMatrix, int baseComponentsCount) {
        int bridgesCount = 0;
        for (int i = 0; i < adjMatrix.getRowDimension(); ++i) {
            for (int j = i + 1; j < adjMatrix.getColumnDimension(); ++j) {
                double val1 = adjMatrix.get(i, j), val2 = adjMatrix.get(j, i);
                if (val1 == 0 && val2 == 0)
                    continue;

                if (val1 == val2) {
                    adjMatrix.set(i, j, 0);
                    adjMatrix.set(j, i, 0);
                }
                else if (val1 != 0)
                    adjMatrix.set(i, j, 0);
                else if (val2 != 0)
                    adjMatrix.set(j, i, 0);

                int componentsCount = GraphAlgorithms.findConnectivityComponents(adjMatrix).size();
                if (componentsCount > baseComponentsCount) {
                    bridgesCount++;
//                    System.out.println("Row: " + i + "   Col: " + j);// TODO del
                }
                adjMatrix.set(i, j, val1);
                adjMatrix.set(j, i, val2);
            }
        }
        return bridgesCount;
    }

    private int findHingesCount(Matrix adjMatrix, int baseComponentsCount) {
        int hingesCount = 0;
        for (int i = 0; i < adjMatrix.getRowDimension(); ++i) {
            Matrix subMatrix = GraphAlgorithms.subMatrix(adjMatrix, i, i);
            int componentsCount = GraphAlgorithms.findConnectivityComponents(subMatrix).size();
            if (componentsCount > baseComponentsCount) hingesCount++;
        }
        return hingesCount;
    }

    private List<List<String> > indicesToNames(List<List<Integer> > components, List<Vertex> vertices) {
        List<List<String> > result = new ArrayList<>();
        for (List<Integer> component : components) {
            result.add(component.stream().map(i ->
                    vertices.get(i).nameProperty().get()).collect(Collectors.toList()));
        }
        return result;
    }

    private String beautifyComponents(List<List<String> > components) {
        StringBuilder builder = new StringBuilder();
        for (List<String> component : components) {
            if (builder.length() > 0) builder.append(", ");
            builder.append(component.toString());
        }
        return builder.toString();
    }
}
