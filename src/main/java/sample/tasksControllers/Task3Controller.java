package sample.tasksControllers;

import Jama.Matrix;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import org.controlsfx.control.spreadsheet.GridBase;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetCellType;
import org.controlsfx.control.spreadsheet.SpreadsheetView;
import sample.Graph.Elements.Vertex;
import sample.Graph.GraphGroup;
import sample.GraphAlert;
import sample.GraphAlgorithms;
import sample.MatrixView.MatrixView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class Task3Controller extends TaskController implements Initializable {
    @FXML private VBox root;
    @FXML private SpreadsheetView spreadsheetView;
    private boolean haveResult = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        spreadsheetView.setGrid(new GridBase(0, 0));
        spreadsheetView.setEditable(false);
    }

    @Override
    public Parent getRoot() {
        return root;
    }

    //------------|
    //   events   |
    //------------|
    @FXML private void onFloydWarshell() {
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
        if (!validateGraph(graphGroup, true, true)) {
            end.run();
            return;
        }

        Matrix matrix = matrixView.getMatrix();
        Matrix result = GraphAlgorithms.floydAlgorithm(matrix);
        setResult(result, graphGroup);
        haveResult = true;

        end.run();
    }

    @FXML private void onDijkstra() {
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
        if (!validateGraph(graphGroup, true, true)) {
            end.run();
            return;
        }

        List<Vertex> vertices = graphGroup.getVertices();
        Matrix result = new Matrix(vertices.size(), vertices.size(), Double.POSITIVE_INFINITY);
        for (int i = 0; i < vertices.size(); ++i) {
            Map<Vertex, Double> distances = GraphAlgorithms.dijkstraAlgorithm(vertices.get(i), vertices);
            for (int j = 0; j < vertices.size(); ++j) {
                result.set(i, j, distances.get(vertices.get(j)));
            }
        }
        setResult(result, graphGroup);

        end.run();
    }

    @FXML private void onBellmanFord() {
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
        if (!validateGraph(graphGroup, true, false)) {
            end.run();
            return;
        }

        List<Vertex> vertices = graphGroup.getVertices();
        Matrix result = new Matrix(vertices.size(), vertices.size(), Double.POSITIVE_INFINITY);
        boolean foundInfCycles = false;
        for (int i = 0; i < vertices.size(); ++i) {
            Map<Vertex, Double> distances = GraphAlgorithms.bellmanFordAlgorithm(vertices.get(i),
                    vertices, graphGroup.getEdges()).orElse(null);
            if (distances == null) {
                foundInfCycles = true;
                break;
            }
            for (int j = 0; j < vertices.size(); ++j) {
                result.set(i, j, distances.get(vertices.get(j)));
            }
        }

        if (foundInfCycles) {
            setResult(new Matrix(0, 0), graphGroup);
            GraphAlert.showInfoAndWait("Найден цикл с отрицательным весом.");
        }
        else {
            setResult(result, graphGroup);
        }

        end.run();
    }

    @FXML private void onJohnson() {
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
        if (!validateGraph(graphGroup, true, false)) {
            end.run();
            return;
        }

        List<Vertex> vertices = graphGroup.getVertices();
        Map<Vertex, Map<Vertex, Double> > distances = GraphAlgorithms.johnsonAlgorithm(
                graphGroup, vertices, graphGroup.getEdges()).orElse(null);
        if (distances == null) {
            setResult(new Matrix(0, 0), graphGroup);
            GraphAlert.showInfoAndWait("Найден цикл с отрицательным весом.");
        }
        else {
            Matrix result = new Matrix(vertices.size(), vertices.size(), Double.POSITIVE_INFINITY);
            for (int i = 0; i < vertices.size(); ++i) {
                for (int j = 0; j < vertices.size(); ++j) {
                    Vertex v1 = vertices.get(i), v2 = vertices.get(j);
                    result.set(i, j, distances.get(v1).get(v2));
                }
            }

            setResult(result, graphGroup);
        }

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
            writer.write("Матрица кратчайших расстояний:\n");
            writer.write("[\n");
            boolean isFirst = true;
            for (ObservableList<SpreadsheetCell> row : spreadsheetView.getGrid().getRows()) {
                if (!isFirst)
                    writer.write(",\n");
                isFirst = false;
                writer.write("\t");
                writer.write(rowToString(row));
            }
            writer.write("\n]");
        }
        catch (IOException e) {
            GraphAlert.showErrorAndWait("Ошибка сохранения файла.");
        }
    }

    // methods
    private boolean validateGraph(GraphGroup graphGroup, boolean more1Vertex, boolean positiveWeights) {
        if (more1Vertex && graphGroup.getVerticesCount() < 2) {
            GraphAlert.showErrorAndWait("Кол-во вершин меньше 2.");
            return false;
        }
        if (positiveWeights && !graphGroup.isAllEdgesWeightsPositive()) {
            GraphAlert.showErrorAndWait("Все ребра должны быть положительны.");
            return false;
        }
        return true;
    }

    private String rowToString(ObservableList<SpreadsheetCell> row) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        boolean isFirst = true;
        for (SpreadsheetCell cell : row) {
            if (!isFirst)
                builder.append(", ");
            isFirst = false;
            builder.append((Double) cell.getItem());
        }
        builder.append("]");
        return builder.toString();
    }

    private void setResult(Matrix matrix, GraphGroup graphGroup) {
        ObservableList<ObservableList<SpreadsheetCell> > rows = FXCollections.observableArrayList();
        for (int row = 0; row < matrix.getRowDimension(); ++row) {
            ObservableList<SpreadsheetCell> newRow = FXCollections.observableArrayList();
            for (int col = 0; col < matrix.getColumnDimension(); ++col) {
                newRow.add(SpreadsheetCellType.DOUBLE.createCell(
                        row, col, 1, 1, matrix.get(row, col)));
            }
            rows.add(newRow);
        }

        GridBase gridBase = new GridBase(matrix.getRowDimension(),
                matrix.getColumnDimension());
        gridBase.setRows(rows);
        gridBase.getRowHeaders().clear();
        gridBase.getColumnHeaders().clear();
        for (Vertex vertex : graphGroup.getVertices()) {
            gridBase.getRowHeaders().add(vertex.getName());
            gridBase.getColumnHeaders().add(vertex.getName());
        }
        spreadsheetView.setGrid(gridBase);
    }

}
