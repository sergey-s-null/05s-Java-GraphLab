package sample;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import org.controlsfx.glyphfont.Glyph;
import sample.Graph.GraphGroup;
import sample.MatrixView.MatrixView;
import sample.Parser.Exceptions.EqualsNamesException;
import sample.Parser.GraphData;
import sample.Parser.InputFileParser;
import sample.Parser.OutputFileSaver;

import javax.imageio.ImageIO;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class MainController {
//    private static final File filesDirectory = new File("C:\\Users\\Sergey\\Desktop\\debug_saves");

    private GraphGroup.Action currentAction = GraphGroup.Action.Empty;
    @FXML private ToggleGroup toggleGroup;
    @FXML private TabPane tabPaneWithGraphs;

    private FileChooser fileChooser = new FileChooser(),
                        imageFileChooser = new FileChooser();
    private File prevSaveDir = null, prevOpenDir = null;

    @FXML private BorderPane graphEditorPane;
    @FXML private VBox authorPane, helpPane;

    @FXML private WebView webView;


    void init() {
        toggleGroup.selectedToggleProperty().addListener(this::onActionButtonSelected);
        initFileChoosers();
        initAboutProgram();

        createEmptyGraphTab();
    }

    private void initFileChoosers() {
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Матрица смежности", "*.adj"),
                new FileChooser.ExtensionFilter("Матрица инцидентности", "*.inc"),
                new FileChooser.ExtensionFilter("Ребра", "*.ed")
        );

        imageFileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Изображение", "*.png")
        );
    }

    private void initAboutProgram() {
        try {
            URI path = getClass().getResource("/aboutProgram/main.html").toURI();
            List<String> htmlLines = Files.readAllLines(Paths.get(path));
            String html = htmlLines.stream().reduce((s1, s2) -> s1 + s2).orElse("");
            webView.getEngine().loadContent(html);
        }
        catch (IOException|URISyntaxException e) {
            GraphAlert.showAndWait("Error while loading about program.");
            System.exit(-1);
        }
    }

    private void onActionButtonSelected(ObservableValue<? extends Toggle> obs, Toggle oldValue,
                                        Toggle newValue)
    {
        if (oldValue != null && newValue == null) {
            oldValue.setSelected(true);
        }
        else {
            if (oldValue != null) {
                Glyph oldGlyph = (Glyph) ((ToggleButton) oldValue).getGraphic();
                oldGlyph.setColor(Color.BLACK);
            }
            if (newValue != null) {
                Glyph newGlyph = (Glyph) ((ToggleButton) newValue).getGraphic();
                newGlyph.setColor(Color.ORANGE);

                //moveButton, vertexButton, edgeButton, deleteButton
                switch (((ToggleButton) newValue).getId()) {
                    case "moveButton":
                        currentAction = GraphGroup.Action.Move;
                        break;
                    case "vertexButton":
                        currentAction = GraphGroup.Action.CreateVertex;
                        break;
                    case "edgeButton":
                        currentAction = GraphGroup.Action.CreateEdge;
                        break;
                    case "deleteButton":
                        currentAction = GraphGroup.Action.Delete;
                        break;
                }

                for (Tab tab : tabPaneWithGraphs.getTabs())
                    ((GraphTab) tab).getGraphGroup().setCurrentAction(currentAction);
            }
        }
    }


    private GraphGroup getSelectedGraphGroup() {
        GraphTab selectedTab = getSelectedGraphTab();
        return selectedTab == null ? null : selectedTab.getGraphGroup();
    }

    private GraphTab getSelectedGraphTab() {
        return (GraphTab) tabPaneWithGraphs.getSelectionModel().selectedItemProperty().get();
    }

    private void createEmptyGraphTab() {
        GraphTab tab = new GraphTab();
        tab.setOnCloseAction(event -> onTabClose(tab));
        tabPaneWithGraphs.getTabs().add(tab);
    }

    private void createGraphTabBy(String tabName, GraphData graphData) {
        GraphTab tab = new GraphTab(tabName, graphData);
        tab.setOnCloseAction(event -> onTabClose(tab));
        tabPaneWithGraphs.getTabs().add(tab);
    }

    private void onTabClose(GraphTab tab) {
        tryCloseTab(tab);
    }

    private boolean tryCloseAllTabs() {
        List<Tab> tabsCopy = new ArrayList<>(tabPaneWithGraphs.getTabs());
        for (Tab tab : tabsCopy) {
            tabPaneWithGraphs.getSelectionModel().select(tab);
            if (!tryCloseTab((GraphTab) tab))
                return false;
        }
        return true;
    }

    private boolean tryCloseTab(GraphTab tab) {
        if (tab.getGraphGroup().isNeedToSave()) {
            ButtonType buttonType = GraphAlert.confirmTabClose(tab.getText());
            if (buttonType == ButtonType.YES) {
                if (!trySaveGraph(tab))
                    return false;
            }
            else if (buttonType == ButtonType.CANCEL) {
                return false;
            }
        }

        tabPaneWithGraphs.getTabs().remove(tab);
        return true;
    }

    private boolean trySaveGraph(GraphTab tab) {
        if (prevSaveDir != null)
            fileChooser.setInitialDirectory(prevSaveDir);
        File file = fileChooser.showSaveDialog(null);
        if (file == null)
            return false;
        prevSaveDir = file.getParentFile();

        GraphGroup graphGroup = tab.getGraphGroup();
        MatrixView matrixView = tab.getMatrixView();
        OutputFileSaver outputFileSaver = new OutputFileSaver();
        try {
            if (file.getName().endsWith(".adj")) {
                outputFileSaver.saveAsAdjacency(file.getAbsolutePath(), matrixView.getMatrix(),
                        matrixView.getVerticesData(), graphGroup.getResolution());
            }
            else if (file.getName().endsWith(".inc")) {
                outputFileSaver.saveAsIncident(file.getAbsolutePath(), graphGroup.getGraph());
            }
            else if (file.getName().endsWith(".ed")) {
                outputFileSaver.saveAsEdges(file.getAbsolutePath(), graphGroup.getGraph());
            }
        }
        catch (IOException e) {
            GraphAlert.showAndWait("Ошибка сохранения файла \"" + file.getName() + "\".");
            return false;
        }
        catch (EqualsNamesException e) {
            GraphAlert.showAndWait("Найдены вершины с одинаковыми именами.");
            return false;
        }
        return true;
    }

    //---------------------|
    //   MenuBar actions   |
    //---------------------|
    @FXML private void onTestAction(ActionEvent event) {
        System.gc();
    }

    // File
    @FXML private void onNewGraph(ActionEvent event) {
        createEmptyGraphTab();
    }

    @FXML private void onOpenFile(ActionEvent event) {
        if (prevOpenDir != null)
            fileChooser.setInitialDirectory(prevOpenDir);
        File file = fileChooser.showOpenDialog(null);
        if (file == null)
            return;
        prevOpenDir = file.getParentFile();

        GraphData graphData;
        try {
            InputFileParser inputFileParser = new InputFileParser();
            if (file.getName().endsWith(".adj")) {
                graphData = inputFileParser.parseAdjacencyFile(file.getAbsolutePath());
            }
            else if (file.getName().endsWith(".inc")) {
                graphData = inputFileParser.parseIncidentFile(file.getAbsolutePath());
            }
            else if (file.getName().endsWith(".ed")) {
                graphData = inputFileParser.parseEdgesFile(file.getAbsolutePath());
            }
            else {
                return;
            }
        }
        catch (Exception e) {
            GraphAlert.showAndWait("Ошибка открытия файла: " + e.getMessage());
            return;
        }

        createGraphTabBy(GraphTab.makeValidTabText(file.getName()), graphData);
    }

    @FXML private void onSaveFile(ActionEvent event) {
        GraphTab selectedTab = getSelectedGraphTab();
        if (selectedTab == null)
            return;

        if (selectedTab.getGraphGroup().isEmpty()) {
            GraphAlert.showAndWait("Граф пуст.");
            return;
        }
        trySaveGraph(selectedTab);
    }

    @FXML private void onSaveAsImage(ActionEvent event) {
        GraphGroup graphGroup = getSelectedGraphGroup();
        if (graphGroup == null)
            return;

        if (prevSaveDir != null)
            imageFileChooser.setInitialDirectory(prevSaveDir);
        File file = imageFileChooser.showSaveDialog(null);
        if (file == null)
            return;
        prevSaveDir = file.getParentFile();

        WritableImage image = graphGroup.getGraphImage();
        assert image != null;
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
        }
        catch (IOException e) {
            GraphAlert.showAndWait("Ошибка сохранения файла: " + e.getMessage());
        }
    }

    @FXML private void onExit(ActionEvent event) {
        onExit();
    }

    public void onExit() {
        authorPane.setVisible(false);
        helpPane.setVisible(false);
        graphEditorPane.setVisible(true);

        if (tryCloseAllTabs())
            Platform.exit();
    }

    // Edit
    @FXML private void onUndoAction(ActionEvent event) {
        GraphTab selectedTab = getSelectedGraphTab();
        if (selectedTab != null)
            selectedTab.getGraphGroup().undo();
    }

    @FXML private void onRedoAction(ActionEvent event) {
        GraphGroup graphGroup = getSelectedGraphGroup();
        if (graphGroup != null)
            graphGroup.redo();
    }

    @FXML private void onChangeWidth(ActionEvent event) {
        GraphGroup graphGroup = getSelectedGraphGroup();
        if (graphGroup == null)
            return;

        Double width = InputDialogs.getGraphWidth(graphGroup.getWidth());
        if (width != null)
            graphGroup.setWidth(width, true);
    }

    @FXML private void onChangeHeight(ActionEvent event) {
        GraphGroup graphGroup = getSelectedGraphGroup();
        if (graphGroup == null)
            return;

        Double height = InputDialogs.getGraphHeight(graphGroup.getHeight());
        if (height != null)
            graphGroup.setHeight(height, true);
    }

    // ?
    @FXML private void onAboutProgram(ActionEvent event) {
        graphEditorPane.setVisible(false);
        authorPane.setVisible(false);
        helpPane.setVisible(true);
    }

    @FXML private void onAboutAuthor(ActionEvent event) {
        graphEditorPane.setVisible(false);
        helpPane.setVisible(false);
        authorPane.setVisible(true);
    }

    //----------------------|
    //   About... actions   |
    //----------------------|
    @FXML private void onBackFromAuthor(ActionEvent event) {
        authorPane.setVisible(false);
        graphEditorPane.setVisible(true);
    }

    @FXML private void onBackFromHelp(ActionEvent event) {
        helpPane.setVisible(false);
        graphEditorPane.setVisible(true);
    }

}
