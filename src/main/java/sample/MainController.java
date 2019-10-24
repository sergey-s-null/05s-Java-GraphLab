package sample;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import sample.Graph.GraphGroup;
import sample.MatrixView.MatrixView;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainController {
//    private static final File filesDirectory = new File("C:\\Users\\Sergey\\Desktop\\debug_saves");

    @FXML private ToggleButton moveButton, vertexButton, edgeButton, deleteButton;
    private GraphGroup.Action currentAction = GraphGroup.Action.Empty;
    @FXML private ToggleGroup toggleGroup;
    @FXML private TabPane tabPaneWithGraphs;
    private int tabsCounter = 0;

    private Map<Tab, GraphController> tabToController = new HashMap<>();

    private FileChooser fileChooser = new FileChooser(),
                        imageFileChooser = new FileChooser();
    private File prevSaveDir = null, prevOpenDir = null;
    private InputFileParser inputFileParser = new InputFileParser();
    private OutputFileSaver outputFileSaver = new OutputFileSaver();
    private InputDialog inputDialog = new InputDialog();

    @FXML private BorderPane graphEditorPane;
    @FXML private VBox authorPane, helpPane;
    @FXML private Button backFromAuthor, backFromHelp;

    @FXML private WebView webView;


    void init() {
        initGlyphButtons();
        initFileChoosers();
        initAbouts();

        createNewTab();

        try {
            URI path = getClass().getResource("/aboutProgram/main.html").toURI();
            List<String> htmlLines = Files.readAllLines(Paths.get(path));
            String html = htmlLines.stream().reduce((s1, s2) -> s1 + s2).orElse("");
            webView.getEngine().loadContent(html);
        }
        catch (IOException|URISyntaxException e) {
            System.out.println(e.getMessage());
        }
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

    private void initAbouts() {
        Glyph glyphAuthor = new Glyph("FontAwesome", FontAwesome.Glyph.ARROW_CIRCLE_LEFT);
        glyphAuthor.setFontSize(28);
        backFromAuthor.setGraphic(glyphAuthor);

        Glyph glyphHelp = new Glyph("FontAwesome", FontAwesome.Glyph.ARROW_CIRCLE_LEFT);
        glyphHelp.setFontSize(28);
        backFromHelp.setGraphic(glyphHelp);
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

                for (GraphController controller : tabToController.values())
                    controller.getGraphGroup().setCurrentAction(currentAction);
            }
        }
    }

    private Tab createNewTab(String tabText, GraphData data) {
        Tab tab = createNewTab();
        if (tab == null)
            return null;

        tab.setText(tabText);
        GraphController controller = tabToController.get(tab);
        controller.getGraphGroup().setGraph(data, false);

        return tab;
    }

    private Tab createNewTab() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/graph.fxml"));
        Parent root;
        try {
            root = loader.load();
        }
        catch (IOException e) {
            return null;
        }

        Tab tab = new Tab("Новый таб " + tabsCounter++, root);
        tab.setContextMenu(createTabContextMenu(tab));
        tabPaneWithGraphs.getTabs().add(tab);

        GraphController graphController = loader.getController();
        graphController.init();
        graphController.getGraphGroup().setCurrentAction(currentAction);
        tabToController.put(tab, graphController);

        return tab;
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

    private boolean trySaveGraph(GraphController controller) {
        if (prevSaveDir != null)
            fileChooser.setInitialDirectory(prevSaveDir);
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            prevSaveDir = file.getParentFile();

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

    private boolean tryCloseAllTabs() {
        List<Tab> tabsCopy = new ArrayList<>(tabPaneWithGraphs.getTabs());
        for (Tab tab : tabsCopy) {
            tabPaneWithGraphs.getSelectionModel().select(tab);
            GraphController controller = tabToController.get(tab);
            if (controller.getGraphGroup().isNeedToSave()) {
                ButtonType buttonType = GraphAlert.confirmTabClose(tab.getText());
                if (buttonType == ButtonType.YES) {
                    if (!trySaveGraph(controller))
                        return false;
                }
                else if (buttonType == ButtonType.CANCEL) {
                    return false;
                }
            }
            tabPaneWithGraphs.getTabs().remove(tab);
        }

//        tabPaneWithGraphs.getTabs().removeAll(savedTabs);
        return true;
    }

    //------------------------------|
    //   tab context menu actions   |
    //------------------------------|
    private void onRenameTab(Tab tab, ActionEvent event) {
        String text = inputDialog.getTabText(tab.getText());
        if (text != null)
            tab.setText(text);
    }

    private void onCloseTab(Tab tab, ActionEvent event) {
        GraphController controller = tabToController.get(tab);
        if (controller.getGraphGroup().isNeedToSave()) {
            ButtonType buttonType = GraphAlert.confirmTabClose(tab.getText());
            if (buttonType == ButtonType.YES) {
                if (!trySaveGraph(controller))
                    return;
            }
            else if (buttonType == ButtonType.CANCEL)
                return;
        }
        removeTab(tab);
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
        createNewTab();
    }

    @FXML private void onOpenFile(ActionEvent event) {
        if (prevOpenDir != null)
            fileChooser.setInitialDirectory(prevOpenDir);
        File file = fileChooser.showOpenDialog(null);
        if (file == null)
            return;
        prevOpenDir = file.getParentFile();

        GraphData result;
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

        Tab tab = createNewTab(Main.makeValidTabText(file.getName()), result);
        tabPaneWithGraphs.getSelectionModel().select(tab);
    }

    @FXML private void onSaveFile(ActionEvent event) {
        Tab selectedTab = tabPaneWithGraphs.getSelectionModel().selectedItemProperty().get();
        if (selectedTab == null)
            return;

        GraphController controller = tabToController.get(selectedTab);
        if (controller.getGraphGroup().isEmpty()) {
            GraphAlert.showAndWait("Граф пуст.");
            return;
        }
        trySaveGraph(controller);
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
