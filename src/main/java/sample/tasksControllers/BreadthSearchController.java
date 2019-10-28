package sample.tasksControllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import sample.Graph.Elements.Vertex;
import sample.Graph.GraphGroup;
import sample.Graph.GraphPath;
import sample.GraphAlert;
import sample.GraphAlgorithms;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class BreadthSearchController extends TaskController implements Initializable {
    @FXML private VBox root;
    @FXML private TextField resultField, pathLengthField, pathField;
    private FileChooser fileChooser = new FileChooser();
    private boolean haveResult = false;
    private GraphGroup currentGraph;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Текстовый файл", "*.txt"));

        root.setFillWidth(true);
    }

    @Override
    public VBox getRoot() {
        return root;
    }

    @Override
    public boolean validateGraph(GraphGroup graphGroup) {
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

    @FXML private void onStart() {
        if (isBusy())
            return;
        for (Consumer<TaskController> startConsumer : startListeners)
            startConsumer.accept(this);
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

    @Override
    public void start(GraphGroup graphGroup) {
        super.start(graphGroup);
        graphGroup.setOnTwoVerticesSelected(this::onTwoVerticesSelected);
        graphGroup.setCurrentAction(GraphGroup.Action.SelectTwoVertices);
        currentGraph = graphGroup;
    }

    private void onTwoVerticesSelected(Vertex vertexFrom, Vertex vertexTo) {
        GraphPath path = GraphAlgorithms.breadthSearch(vertexFrom, vertexTo);
        if (path == null) {
            resultField.setText("Маршрут не найден.");
            pathLengthField.setText("-");
            pathField.setText("-");
        }
        else {
            currentGraph.setElementsPath(path);
            resultField.setText("Маршрут найден.");
            pathLengthField.setText(Integer.toString(path.getEdgeCount()));
            pathField.setText(path.toString());
        }
        haveResult = (path != null);

        if (currentGraph != null)
            currentGraph.setOnTwoVerticesSelected(null);
        end();
    }
}
