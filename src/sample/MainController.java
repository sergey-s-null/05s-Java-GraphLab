package sample;

import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import sample.Graph.GraphGroup;
import sample.MatrixView.MatrixView;
import sample.Parser.GraphData;
import sample.Parser.InputFileParser;
import sample.Parser.OutputFileSaver;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class MainController {
    private static final File filesDirectory = new File("C:\\Users\\Sergey\\Desktop\\debug_saves");

    @FXML private ToggleButton moveButton, vertexButton, edgeButton, deleteButton;
    private GraphGroup.Action currentAction = GraphGroup.Action.Empty;
    @FXML private ToggleGroup toggleGroup;
    @FXML private TabPane tabPaneWithGraphs;
    private int tabsCounter = 0;

    private Map<Tab, GraphController> tabToController = new HashMap<>();

    private FileChooser fileChooser = new FileChooser();
    private InputFileParser inputFileParser = new InputFileParser();
    private OutputFileSaver outputFileSaver = new OutputFileSaver();
    private InputDialog inputDialog = new InputDialog();

    void init() {
        initGlyphButtons();

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Матрица смежности", "*.adj"),
                new FileChooser.ExtensionFilter("Матрица инцидентности", "*.inc"),
                new FileChooser.ExtensionFilter("Ребра", "*.ed")
        );

        createNewGraph();
        createNewGraph();



    }

    private void initGlyphButtons() {
        Glyph hand = new Glyph("FontAwesome", FontAwesome.Glyph.HAND_ALT_UP);
        Glyph circle = new Glyph("FontAwesome", FontAwesome.Glyph.CIRCLE_ALT);
        Glyph edge = new Glyph("FontAwesome", FontAwesome.Glyph.ARROWS_H);
        Glyph trash = new Glyph("FontAwesome", FontAwesome.Glyph.TRASH_ALT);

        moveButton.setGraphic(hand);
        vertexButton.setGraphic(circle);
        edgeButton.setGraphic(edge);
        deleteButton.setGraphic(trash);

        toggleGroup.selectedToggleProperty().addListener(this::onActionButtonSelected);
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
                newGlyph.setColor(Color.ORANGE); // TODO change color

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

                for (GraphController controller : tabToController.values())
                    controller.getGraphGroup().setCurrentAction(currentAction);
            }
        }
    }

    private void createNewGraph() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("graph.fxml"));
        Parent root;
        try {
            root = loader.load();
        }
        catch (IOException e) {
            System.out.println("Error: " + e);
            return;
        }

        Tab tab = new Tab("Новый таб " + tabsCounter++, root); // TODO name with counter
        tab.setContextMenu(createTabContextMenu(tab));
        tabPaneWithGraphs.getTabs().add(tab);

        GraphController graphController = loader.getController();
        graphController.init();
        graphController.getGraphGroup().setCurrentAction(currentAction);
        tabToController.put(tab, graphController);
    }

    private ContextMenu createTabContextMenu(Tab tab) {
        MenuItem rename = new MenuItem("Переименовать");
        MenuItem close = new MenuItem("Закрыть");
        rename.setOnAction(event -> onRenameTab(tab, event));
        close.setOnAction(event -> onCloseTab(tab, event));

        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getItems().addAll(
                rename,
                new SeparatorMenuItem(),
                close
        );
        return contextMenu;
    }

    private void onRenameTab(Tab tab, ActionEvent event) {
        String text = inputDialog.getTabText(tab.getText());
        if (text != null)
            tab.setText(text);
    }

    private void onCloseTab(Tab tab, ActionEvent event) {
        ButtonType buttonType = GraphAlert.confirmTabClose();
        if (buttonType == ButtonType.YES) {
            if (trySaveGraph(tabToController.get(tab)))
                removeTab(tab);
        }
        else if (buttonType == ButtonType.NO) {
            removeTab(tab);
        }
    }

    private boolean trySaveGraph(GraphController controller) {
        fileChooser.setInitialDirectory(filesDirectory); // TODO
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            GraphGroup graphGroup = controller.getGraphGroup();
            MatrixView matrixView = controller.getMatrixView();
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
            catch (Exception e) {
                GraphAlert.showAndWait("Ошибка сохранения файла: " + e.getMessage());
                return false;
            }

            return true;
        }
        else {
            return false;
        }
    }

    private void removeTab(Tab tab) {
        tabPaneWithGraphs.getTabs().remove(tab);
    }

    private GraphGroup getSelectedGraphGroup() {
        Tab selectedTab = tabPaneWithGraphs.getSelectionModel().selectedItemProperty().get();
        if (selectedTab != null)
            return tabToController.get(selectedTab).getGraphGroup();
        else
            return null;
    }

    //---------------------|
    //   MenuBar actions   |
    //---------------------|
    // TODO del
    @FXML private void onTestAction(ActionEvent event) {
        System.gc();
    }

    // File
    @FXML private void onNewGraph(ActionEvent event) {
        createNewGraph();
    }

    @FXML private void onOpenFile(ActionEvent event) {
        GraphGroup graphGroup = getSelectedGraphGroup();
        if (graphGroup == null)
            return;

        fileChooser.setInitialDirectory(filesDirectory); // todo remake to prevOpenDirectory
        File file = fileChooser.showOpenDialog(null);

        GraphData result;
        if (file != null) {
            try {
                if (file.getName().endsWith(".adj")) {
                    result = inputFileParser.parseAdjacencyFile(file.getAbsolutePath());
                }
                else if (file.getName().endsWith(".inc")) {
                    result = inputFileParser.parseIncidentFile(file.getAbsolutePath());
                }
                else if (file.getName().endsWith(".ed")) {
                    result = inputFileParser.parseEdgesFile(file.getAbsolutePath());
                }
                else {
                    return;
                }
            }
            catch (Exception e) {
                GraphAlert.showAndWait("Ошибка открытия файла: " + e.getMessage());
                return;
            }
            graphGroup.setGraph(result, true);
        }
    }

    @FXML private void onSaveFile(ActionEvent event) {
        Tab selectedTab = tabPaneWithGraphs.getSelectionModel().selectedItemProperty().get();
        if (selectedTab == null)
            return;
        GraphController controller = tabToController.get(selectedTab);

        GraphGroup graphGroup = getSelectedGraphGroup();
        if (graphGroup == null)
            return;

        trySaveGraph(controller);
    }

    // Edit
    @FXML private void onUndoAction(ActionEvent event) {
        Tab selectedTab = tabPaneWithGraphs.getSelectionModel().selectedItemProperty().get();
        if (selectedTab != null)
            tabToController.get(selectedTab).getGraphGroup().undo();
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

        Double width = inputDialog.getGraphWidth(graphGroup.getWidth());
        if (width != null)
            graphGroup.setWidth(width, true);
    }

    @FXML private void onChangeHeight(ActionEvent event) {
        GraphGroup graphGroup = getSelectedGraphGroup();
        if (graphGroup == null)
            return;

        Double height = inputDialog.getGraphHeight(graphGroup.getHeight());
        if (height != null)
            graphGroup.setHeight(height, true);
    }


}
