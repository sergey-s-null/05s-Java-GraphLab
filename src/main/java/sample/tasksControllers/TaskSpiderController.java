package sample.tasksControllers;

import com.sun.xml.internal.ws.api.model.wsdl.WSDLOutput;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sample.Graph.Elements.Style;
import sample.Graph.Elements.Vertex;
import sample.Graph.GraphGroup;
import sample.Graph.SpiderMoveTask;
import sample.Graph.SpiderPath;
import sample.GraphAlert;
import sample.GraphAlgorithms;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;


public class TaskSpiderController extends TaskController implements Initializable {
    @FXML private VBox root;
    @FXML private ToggleGroup spiderToggle;
    @FXML private ToggleButton spiderButton, bugsButton;
//    @FXML private Button findButton, startButton, stopButton, openButton, saveButton; TODO remove from fxml
    @FXML private TextField maxPathLengthField;
    @FXML private TextField pathLengthField, pathField, bugsCountField;
    private SpiderPath lastFoundPath = null;
    private Timer spiderMoveTimer = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        spiderToggle.selectedToggleProperty().addListener(this::onToggleChanged);

        fileChooser.getExtensionFilters().clear();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Json файл", "*.json"));
    }

    @Override
    public Parent getRoot() {
        return root;
    }

    private void onToggleChanged(ObservableValue<? extends Toggle> obs, Toggle oldVal, Toggle newVal) {
        if (oldVal == null) {
            if (!startIfCan.apply(this))
                spiderToggle.selectToggle(null);
        }

        if (oldVal != null)
            ((ToggleButton) oldVal).setTextFill(Color.BLACK);
        if (newVal != null)
            ((ToggleButton) newVal).setTextFill(Style.glyphSelectColor);

        if (newVal == null)
            end.run();
        else {
            GraphGroup graphGroup = currentGraph.get().orElse(null);
            if (graphGroup == null) {
                spiderToggle.selectToggle(null);
                return;
            }

            if (newVal == spiderButton)
                graphGroup.setCurrentAction(GraphGroup.Action.SelectSpider);
            else if (newVal == bugsButton)
                graphGroup.setCurrentAction(GraphGroup.Action.SelectBug);
        }
    }

    // events
    @FXML private void onFindPath() {
        spiderToggle.selectToggle(null);

        GraphGroup graphGroup = currentGraph.get().orElse(null);
        if (graphGroup == null) return;

        if (!graphGroup.isAllEdgesWeightsPositive()) {
            GraphAlert.showInfoAndWait("Все ребра должны быть положительны.");
            return;
        }

        Vertex spiderVertex = graphGroup.getSpiderVertex().orElse(null);
        if (spiderVertex == null) {
            GraphAlert.showInfoAndWait("Положение паука не выбрано.");
            return;
        }

        int bugsCount = graphGroup.getBugsCount();
        if (bugsCount == 0) {
            GraphAlert.showInfoAndWait("Нет ни одной мухи.");
            return;
        }

        double maxLength;
        try {
            maxLength = Double.parseDouble(maxPathLengthField.getText());
        }
        catch (NumberFormatException e) {
            GraphAlert.showErrorAndWait("Неверная максимальная длина маршрута.");
            return;
        }


        SpiderPath result = GraphAlgorithms.niceSpiderPath(spiderVertex, graphGroup.getBugs(),
                graphGroup.getEdges(), maxLength).orElse(null);
        if (result != null) {
            setResult(graphGroup, result);
        }
        else {
            setEmptyResult(graphGroup);
        }
    }

    @FXML private void onStartSpider() {
        // TODO block dangerous buttons
        if (lastFoundPath == null) {
            GraphAlert.showInfoAndWait("Нет маршрута.");
            return;
        }

        if (spiderMoveTimer != null) {
            GraphAlert.showInfoAndWait("Паук уже запущен.");
            return;
        }
        spiderMoveTimer = new Timer(true);
        spiderMoveTimer.schedule(new SpiderMoveTask(spiderMoveTimer, lastFoundPath, this::onSpiderFinished),
                0, 1000);
    }

    @FXML private void onStopSpider() {
        onSpiderFinished();
    }

    @FXML private void onOpenConfig() {
        GraphGroup graphGroup = currentGraph.get().orElse(null);
        if (graphGroup == null) return;

        File file = fileChooser.showOpenDialog(null);
        if (file == null) return;

        String content;
        try {
            content = Files.readAllLines(Paths.get(file.toURI())).stream().reduce((s1, s2) -> s1 + s2).orElse("");
        }
        catch (IOException e) {
            System.out.println(e);
            GraphAlert.showErrorAndWait("Ошибка чтения файла.");
            return;
        }

        String spiderName;
        Set<String> bugsNames = new HashSet<>();
        try {
            JSONObject object = new JSONObject(content);
            spiderName = object.getString("spider");
            JSONArray bugs = object.getJSONArray("bugs");
            for (int i = 0; i < bugs.length(); ++i) {
                String bugName = (String) bugs.get(i);
                bugsNames.add(bugName);
            }
        }
        catch (JSONException|ClassCastException e) {
            System.out.println(e);
            GraphAlert.showErrorAndWait("Ошибка чтения файла: неверный формат.");
            return;
        }

        graphGroup.clearSpider();
        graphGroup.clearBugs();

        for (Vertex vertex : graphGroup.getVerticesByName(spiderName))
            graphGroup.setSpider(vertex);
        for (String bugName : bugsNames) {
            for (Vertex vertex : graphGroup.getVerticesByName(bugName))
                graphGroup.toggleBug(vertex);
        }
    }

    @FXML private void onSaveConfig() {
        GraphGroup graphGroup = currentGraph.get().orElse(null);
        if (graphGroup == null) return;

        Vertex spiderVertex = graphGroup.getSpiderVertex().orElse(null);
        if (spiderVertex == null) {
            GraphAlert.showInfoAndWait("Паук не выбран.");
            return;
        }
        String spiderName = spiderVertex.nameProperty().get();
        List<String> bugsNames = graphGroup.getBugs().stream().map(vertex ->
                vertex.nameProperty().get()).collect(Collectors.toList());

        File file = fileChooser.showSaveDialog(null);
        if (file == null) return;

        JSONObject root = new JSONObject();
        root.put("spider", spiderName);

        JSONArray bugsNamesArray = new JSONArray();
        for (String bugName : bugsNames)
            bugsNamesArray.put(bugName);
        root.put("bugs", bugsNamesArray);

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(file.toURI()))) {
            writer.write(root.toString());
        }
        catch (IOException e) {
            System.out.println(e);
            GraphAlert.showErrorAndWait("Ошибка записи в файл.");
        }
    }

    // methods


    private void setResult(GraphGroup graphGroup, SpiderPath path) {
        graphGroup.setElementsPath(path);
        pathLengthField.setText(Double.toString(path.getLength()));
        pathField.setText(path.toString());
        bugsCountField.setText(Integer.toString(path.getBugsCount()));
        lastFoundPath = path;
    }

    private void setEmptyResult(GraphGroup graphGroup) {
        graphGroup.clearCurrentPath();
        pathLengthField.setText("-");
        pathField.setText("-");
        bugsCountField.setText("-");
        lastFoundPath = null;
    }

    private void onSpiderFinished() {
        if (spiderMoveTimer != null) {
            spiderMoveTimer.cancel();
            spiderMoveTimer = null;
        }
        if (lastFoundPath != null)
            lastFoundPath.getSpider().connect(lastFoundPath.getFirstVertex());
    }



}
