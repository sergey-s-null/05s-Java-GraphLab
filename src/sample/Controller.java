package sample;

import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import org.controlsfx.control.SegmentedButton;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import sample.Graph.GraphActionsController;
import sample.Graph.GraphGroup;
import sample.MatrixView.MatrixView;
import sample.Parser.InputFileParser;
import sample.Parser.GraphData;
import sample.Parser.OutputFileSaver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Controller {
    private static final File filesDirectory = new File("C:\\Users\\Sergey\\Desktop\\debug_saves");

    @FXML private ToggleButton moveButton, vertexButton, edgeButton, deleteButton;
    @FXML private ToggleGroup toggleGroup;
    @FXML private TabPane tabWithGraphs;

    private List<GraphController> graphsControllers = new ArrayList<>();

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

//        FXMLLoader loader = new FXMLLoader(getClass().getResource("graph.fxml"));
//        try {
//            Parent root = loader.load();
//
//            GraphController graphController = loader.getController();
//            graphController.init();
//
//            testHBox.getChildren().add(root);
//        }
//        catch (IOException e) {
//            System.out.println(e);
//        }





//        Button testButton = new Button("", new Glyph("FontAwesome", FontAwesome.Glyph.BEER));
//        leftPanel.getChildren().add(testButton);




//        leftPanel.getChildren().addAll(b1, b2, b3, b4);
//        tg.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
//            if (newValue == null && oldValue != null) {
//                oldValue.setSelected(true);
//            }
//        });








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
                GraphGroup.Action newAction = GraphGroup.Action.Empty;
                switch (((ToggleButton) newValue).getId()) {
                    case "moveButton":
                        newAction = GraphGroup.Action.Move;
                        break;
                    case "vertexButton":
                        newAction = GraphGroup.Action.CreateVertex;
                        break;
                    case "edgeButton":
                        newAction = GraphGroup.Action.CreateEdge;
                        break;
                    case "deleteButton":
                        newAction = GraphGroup.Action.Delete;
                        break;
                }

                for (GraphController controller : graphsControllers)
                    controller.setGraphAction(newAction);
            }
        }
    }

    private void createNewGraph() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("graph.fxml"));
        Parent root;
        try {
            root = loader.load();

            GraphController graphController = loader.getController();
            graphController.init();

            graphsControllers.add(graphController);
        }
        catch (IOException e) {
            System.out.println("Error: " + e);
            return;
        }

        tabWithGraphs.getTabs().add(new Tab("Новый таб", root));
    }

    //---------------------|
    //   MenuBar actions   |
    //---------------------|
    // TODO del
    @FXML private void onTestAction(ActionEvent event) {
        System.gc();
    }

    // File TODO
    @FXML private void onOpenFile(ActionEvent event) {
//        fileChooser.setInitialDirectory(filesDirectory); // todo remove
//        File file = fileChooser.showOpenDialog(null);
//
//        GraphData result;
//        if (file != null) {
//            try {
//                if (file.getName().endsWith(".adj")) {
//                    result = inputFileParser.parseAdjacencyFile(file.getAbsolutePath());
//                }
//                else if (file.getName().endsWith(".inc")) {
//                    result = inputFileParser.parseIncidentFile(file.getAbsolutePath());
//                }
//                else if (file.getName().endsWith(".ed")) {
//                    result = inputFileParser.parseEdgesFile(file.getAbsolutePath());
//                }
//                else {
//                    return;
//                }
//            }
//            catch (Exception e) {
//                GraphAlert.showAndWait("Ошибка открытия файла: " + e.getMessage());
//                return;
//            }
//            graphGroup.setGraph(result, true);
//        }
    }
    // TODO
    @FXML private void onSaveFile(ActionEvent event) {
//        fileChooser.setInitialDirectory(filesDirectory);
//        File file = fileChooser.showSaveDialog(null);
//        if (file != null) {
//            try {
//                if (file.getName().endsWith(".adj")) {
//                    outputFileSaver.saveAsAdjacency(file.getAbsolutePath(), matrixView.getMatrix(),
//                            matrixView.getVerticesData(), graphGroup.getResolution());
//                }
//                else if (file.getName().endsWith(".inc")) {
//                    outputFileSaver.saveAsIncident(file.getAbsolutePath(), graphGroup.getGraph());
//                }
//                else if (file.getName().endsWith(".ed")) {
//                    outputFileSaver.saveAsEdges(file.getAbsolutePath(), graphGroup.getGraph());
//                }
//            }
//            catch (Exception e) {
//                GraphAlert.showAndWait("Ошибка сохранения файла: " + e.getMessage());
//            }
//        }
    }

    // Edit TODO
    @FXML private void onUndoAction(ActionEvent event) {
        GraphActionsController.undo();
    }
    // TODO
    @FXML private void onRedoAction(ActionEvent event) {
        GraphActionsController.redo();
    }
    // TODO
    @FXML private void onChangeWidth(ActionEvent event) {
//        Double width = inputDialog.getGraphWidth(graphGroup.getWidth());
//        if (width != null)
//            graphGroup.setWidth(width, true);
    }
    // TODO
    @FXML private void onChangeHeight(ActionEvent event) {
//        Double height = inputDialog.getGraphHeight(graphGroup.getHeight());
//        if (height != null)
//            graphGroup.setHeight(height, true);
    }


}
