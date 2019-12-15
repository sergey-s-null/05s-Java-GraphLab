package sample;

import Jama.Matrix;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import org.controlsfx.glyphfont.Glyph;
import sample.Graph.Elements.Style;
import sample.Graph.Elements.Vertex;
import sample.Graph.GraphGroup;
import sample.MatrixView.MatrixView;
import sample.Parser.Exceptions.EqualsNamesException;
import sample.Parser.GraphData;
import sample.Parser.InputFileParser;
import sample.Parser.OutputFileSaver;
import sample.dialogs.InputDialogs;
import sample.dialogs.SelectGraphDialog;
import sample.tasksControllers.TaskController;

import javax.imageio.ImageIO;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;


public class MainController implements Initializable {
//    private static final File filesDirectory = new File("C:\\Users\\Sergey\\Desktop\\debug_saves");

    private GraphGroup.Action currentAction = GraphGroup.Action.Empty;
    // menu items for block while task execute
    @FXML private MenuItem newGraph, openGraph;
    @FXML private Menu editMenu, taskMenu;

    @FXML private ToggleButton moveButton, vertexButton, edgeButton, deleteButton;
    @FXML private ToggleGroup toggleGroup;
    @FXML private TabPane tabPaneWithGraphs;

    private FileChooser fileChooser = new FileChooser(),
                        imageFileChooser = new FileChooser();
    private File prevSaveDir = null, prevOpenDir = null;

    @FXML private BorderPane graphEditorPane;
    @FXML private VBox authorPane, helpPane;

    @FXML private WebView webView;

    @FXML private VBox taskVBox;
    private TaskController currentTaskController;
    private Map<String, TaskController> taskControllers = new HashMap<>();
    private boolean isTaskStarted = false;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        toggleGroup.selectedToggleProperty().addListener(this::onActionButtonSelected);
        initFileChoosers();
        initAboutProgram();
        initTasks();

        createEmptyGraphTab();

    }

    // init
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
            InputStream stream = getClass().getResourceAsStream("/aboutProgram/main.html");
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
            StringBuilder html = new StringBuilder();
            while(reader.ready())
                html.append(reader.readLine());
            webView.getEngine().loadContent(html.toString());
        }
        catch (IOException e) {
            GraphAlert.showErrorAndWait("Error while loading about program.");
            System.exit(-1);
        }
    }

    private void initTasks() {
        TaskController.initOnStart(this::onTaskStart);
        TaskController.initOnEnd(this::onTaskEnd);
        TaskController.initGraphTabReceiver(this::getSelectedGraphTab);
        TaskController.initGraphReceiver(this::getSelectedGraphGroup);
        TaskController.initMatrixViewReceiver(() -> {
            GraphTab tab = getSelectedGraphTab().orElse(null);
            return tab == null ? Optional.empty() : Optional.ofNullable(tab.getMatrixView());
        });
        TaskController.initAllGraphTabsReceiver(() -> tabPaneWithGraphs.getTabs().stream().map(tab ->
                (GraphTab) tab).collect(Collectors.toList()));
        TaskController.initNewGraphCreator(this::createEmptyGraphTab);

        String[] taskResourcePaths = {
                "/tasks_fxml/TaskSpider.fxml",
                "/tasks_fxml/Task2PathSearch.fxml",
                "/tasks_fxml/Task3Dijkstra.fxml",
                "/tasks_fxml/Task4.fxml",
                "/tasks_fxml/Task6.fxml",
                "/tasks_fxml/Task8.fxml"
        };
        String[] taskIds = {
                "1",
                "2",
                "3",
                "4",
                "6",
                "8"
        };

        try {
            for (int i = 0; i < taskResourcePaths.length; ++i) {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource(taskResourcePaths[i]));
                loader.load();
                TaskController controller = loader.getController();
                taskControllers.put(taskIds[i], controller);
            }
        }
        catch (IOException e) {
            System.out.println("Error while loading task resource: " + e);
            System.exit(-1);
        }
    }

    // methods
    private Optional<GraphGroup> getSelectedGraphGroup() {
        GraphTab selectedTab = getSelectedGraphTab().orElse(null);
        return selectedTab == null ? Optional.empty() :
                Optional.ofNullable(selectedTab.getGraphGroup());
    }

    private Optional<MatrixView> getSelectedMatrixView() {
        GraphTab selectedTab = getSelectedGraphTab().orElse(null);
        return selectedTab == null ? Optional.empty() :
                Optional.ofNullable(selectedTab.getMatrixView());
    }

    private Optional<GraphTab> getSelectedGraphTab() {
        Tab tab = tabPaneWithGraphs.getSelectionModel().selectedItemProperty().get();
        return Optional.ofNullable((GraphTab) tab);
    }
    //TODO одинаковый код
    private GraphTab createEmptyGraphTab() {
        GraphTab tab = new GraphTab();
        tab.getGraphGroup().setCurrentAction(currentAction);
        tab.setOnCloseAction(event -> onTabClose(tab));
        tabPaneWithGraphs.getTabs().add(tab);
        return tab;
    }
    //TODO одинаковый код
    private void createGraphTabBy(String tabName, GraphData graphData) {
        GraphTab tab = new GraphTab(tabName, graphData);
        tab.getGraphGroup().setCurrentAction(currentAction);
        tab.setOnCloseAction(event -> onTabClose(tab));
        tabPaneWithGraphs.getTabs().add(tab);
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
            GraphAlert.showErrorAndWait("Ошибка сохранения файла \"" + file.getName() + "\".");
            return false;
        }
        catch (EqualsNamesException e) {
            GraphAlert.showErrorAndWait("Найдены вершины с одинаковыми именами.");
            return false;
        }
        return true;
    }

    private void setDisableControlsForTask(boolean disable) {
        setDisableActionButtons(disable);
        setDisableMenuItems(disable);

        GraphTab tab = getSelectedGraphTab().orElse(null);
        if (disable) {
            if (tab != null)
                disableOtherTab(tab);
        }
        else {
            enableAllTabs();
        }
    }

    private void setDisableActionButtons(boolean flag) {
        moveButton.setDisable(flag);
        vertexButton.setDisable(flag);
        edgeButton.setDisable(flag);
        deleteButton.setDisable(flag);
    }

    private void setDisableMenuItems(boolean flag) {
        newGraph.setDisable(flag);
        openGraph.setDisable(flag);
        editMenu.setDisable(flag);
        taskMenu.setDisable(flag);
    }

    private void disableOtherTab(Tab tab) {
        for (Tab iTab : tabPaneWithGraphs.getTabs())
            if (iTab != tab) iTab.setDisable(true);
    }

    private void enableAllTabs() {
        for (Tab tab : tabPaneWithGraphs.getTabs())
            tab.setDisable(false);
    }

    //------------------|
    //   Tasks events   |
    //------------------|
    private boolean onTaskStart(TaskController controller) {
        if (isTaskStarted)
            return false;

        setDisableControlsForTask(true);
        isTaskStarted = true;
        return true;
    }

    private void onTaskEnd() {
        setDisableControlsForTask(false);
        isTaskStarted = false;

        getSelectedGraphGroup().ifPresent(graphGroup ->
                graphGroup.setCurrentAction(currentAction));
    }

    // Tab event
    private void onTabClose(GraphTab tab) {
        tryCloseTab(tab);
    }

    //--------------------|
    //   Buttons events   |
    //--------------------|
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
                newGlyph.setColor(Style.glyphSelectColor);

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

    @FXML private void onClearCurrentPath() {
        getSelectedGraphGroup().ifPresent(GraphGroup::clearCurrentPath);
    }

    @FXML private void onHideShowTaskPane() {
        if (currentTaskController != null) {
            Parent parent = currentTaskController.getRoot();
            boolean prevState = parent.isVisible();
            parent.setVisible(!prevState);
            parent.setManaged(!prevState);
        }
    }

    //---------------------|
    //   MenuBar actions   |
    //---------------------|
    @FXML private void onTestAction() {
        SelectGraphDialog dialog = new SelectGraphDialog();
        List<GraphTab> tabs = tabPaneWithGraphs.getTabs().stream().map(tab ->
                (GraphTab) tab).collect(Collectors.toList());
        GraphTab tab = dialog.select(tabs).orElse(null);
        System.out.println(tab);
    }

    // File
    @FXML private void onNewGraph() {
        createEmptyGraphTab();
    }

    @FXML private void onOpenFile() {
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
            GraphAlert.showErrorAndWait("Ошибка открытия файла: " + e.getMessage());
            return;
        }

        createGraphTabBy(GraphTab.makeValidTabText(file.getName()), graphData);
    }

    @FXML private void onSaveFile() {
        GraphTab selectedTab = getSelectedGraphTab().orElse(null);
        if (selectedTab == null)
            return;

        if (selectedTab.getGraphGroup().isEmpty()) {
            GraphAlert.showErrorAndWait("Граф пуст.");
            return;
        }
        trySaveGraph(selectedTab);
    }

    @FXML private void onSaveAsImage() {
        GraphGroup graphGroup = getSelectedGraphGroup().orElse(null);
        if (graphGroup == null)
            return;

        if (prevSaveDir != null)
            imageFileChooser.setInitialDirectory(prevSaveDir);
        File file = imageFileChooser.showSaveDialog(null);
        if (file == null)
            return;
        prevSaveDir = file.getParentFile();

        WritableImage image = graphGroup.getGraphImage();
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
        }
        catch (IOException e) {
            GraphAlert.showErrorAndWait("Ошибка сохранения файла: " + e.getMessage());
        }
    }

    @FXML public void onExit() {
        authorPane.setVisible(false);
        helpPane.setVisible(false);
        graphEditorPane.setVisible(true);

        if (tryCloseAllTabs())
            Platform.exit();
    }

    // Edit
    @FXML private void onUndoAction() {
        if (isTaskStarted) return;
        getSelectedGraphGroup().ifPresent(GraphGroup::undo);
    }

    @FXML private void onRedoAction() {
        if (isTaskStarted) return;
        getSelectedGraphGroup().ifPresent(GraphGroup::redo);
    }

    @FXML private void onChangeWidth() {
        GraphGroup graphGroup = getSelectedGraphGroup().orElse(null);
        if (graphGroup == null)
            return;

        Double width = InputDialogs.getGraphWidth(graphGroup.getWidth());
        if (width != null)
            graphGroup.setWidth(width, true);
    }

    @FXML private void onChangeHeight() {
        GraphGroup graphGroup = getSelectedGraphGroup().orElse(null);
        if (graphGroup == null)
            return;

        Double height = InputDialogs.getGraphHeight(graphGroup.getHeight());
        if (height != null)
            graphGroup.setHeight(height, true);
    }

    // Graph tasks
    @FXML private void onTaskSelected(ActionEvent event) {
        TaskController nextController;
        String menuItemId = ((MenuItem) event.getSource()).getId();
        switch (menuItemId) {
            case "1":
            case "2":
            case "3":
            case "4":
            case "6":
            case "8":
                nextController = taskControllers.get(menuItemId);
                break;
            default:
                System.out.println("Unknown task selected!");
                return;
        }

        nextController.getRoot().setVisible(true);
        nextController.getRoot().setManaged(true);

        currentTaskController = nextController;
        if (taskVBox.getChildren().size() >= 2)
            taskVBox.getChildren().remove(1);
        taskVBox.getChildren().add(1, currentTaskController.getRoot());
    }

    @FXML private void on7TaskSelected() {
        GraphGroup graphGroup = getSelectedGraphGroup().orElse(null);
        MatrixView matrixView = getSelectedMatrixView().orElse(null);
        if (graphGroup == null || matrixView == null) return;

        Matrix adjMatrix = matrixView.getMatrix();
        if (GraphAlgorithms.isGraphFull(adjMatrix))
            GraphAlert.showInfoAndWait("Граф является полным.");

        GraphTab newTab = createEmptyGraphTab();
        GraphGroup newGraphGroup = newTab.getGraphGroup();

        for (Vertex vertex : graphGroup.getVertices())
            newGraphGroup.addVertex(vertex.getCenterX(), vertex.getCenterY(), false);

        GraphAlgorithms.makeGraphAddition(newGraphGroup, adjMatrix);
    }

    // TODO
    @FXML private void on12TaskSelected() {




    }

    // ?
    @FXML private void onAboutProgram() {
        graphEditorPane.setVisible(false);
        authorPane.setVisible(false);
        helpPane.setVisible(true);
    }

    @FXML private void onAboutAuthor() {
        graphEditorPane.setVisible(false);
        helpPane.setVisible(false);
        authorPane.setVisible(true);
    }

    //----------------------|
    //   About... actions   |
    //----------------------|
    @FXML private void onBackFromAuthor() {
        authorPane.setVisible(false);
        graphEditorPane.setVisible(true);
    }

    @FXML private void onBackFromHelp() {
        helpPane.setVisible(false);
        graphEditorPane.setVisible(true);
    }

}
