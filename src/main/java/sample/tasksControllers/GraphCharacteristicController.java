package sample.tasksControllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import sample.Graph.Elements.Vertex;
import sample.Graph.GraphGroup;
import sample.GraphAlert;
import sample.GraphAlgorithms;
import sample.MatrixView.MatrixView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class GraphCharacteristicController extends TaskController implements Initializable {
    @FXML private VBox root;
    @FXML private TextField eccentricitiesField, radiusField, diameterField,
            verticesDegreesField;
    private boolean haveResult = false;
    private FileChooser fileChooser = new FileChooser();//TODO move this to parent class


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Текстовый файл", "*.txt"));
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

    @Override
    public void start(GraphGroup graphGroup, MatrixView matrixView) {
        super.start(graphGroup, matrixView);

        GraphAlgorithms.GraphCharacteristic characteristic = GraphAlgorithms.graphCharacteristic(
                matrixView.getMatrix());
        List<Integer> verticesDegrees = new ArrayList<>();
        for (Vertex vertex : graphGroup.getVertices()) {
            verticesDegrees.add(vertex.getDegree());
        }

        eccentricitiesField.setText(vectorToString(characteristic.eccentricities));
        radiusField.setText(Double.toString(characteristic.radius));
        diameterField.setText(Double.toString(characteristic.diameter));
        verticesDegreesField.setText(vectorToString(verticesDegrees));
        haveResult = true;

        end();
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
