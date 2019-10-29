package sample.tasksControllers;

import Jama.Matrix;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
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
import java.util.ResourceBundle;

public class FloydAlgorithmController extends TaskController implements Initializable {
    @FXML private VBox root;
    @FXML private SpreadsheetView spreadsheetView;
    private boolean haveResult = false;
    private FileChooser fileChooser = new FileChooser();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        spreadsheetView.setGrid(new GridBase(0, 0));
        spreadsheetView.setEditable(false);

        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Текстовый файл", "*.txt"));
    }

    @Override
    public Parent getRoot() {
        return root;
    }

    @Override
    public boolean validateGraph(GraphGroup graphGroup) {
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

    @Override
    public void start(GraphGroup graphGroup, MatrixView matrixView) {
        super.start(graphGroup, matrixView);

        Matrix matrix = matrixView.getMatrix();
        Matrix result = GraphAlgorithms.floydAlgorithm(matrix);
        setResult(result, graphGroup);
        haveResult = true;

        end();
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
