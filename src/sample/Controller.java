package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import sample.Graph.GraphActionsController;
import sample.Graph.GraphGroup;
import sample.MatrixView.MatrixView;
import sample.Parser.InputFileParser;
import sample.Parser.GraphData;
import sample.Parser.OutputFileSaver;

import java.io.File;


//rb_createVertex
//rb_createEdge
//rb_delete
//rb_move

public class Controller {
    private static final File filesDirectory = new File("C:\\Users\\Sergey\\Desktop\\debug_saves");

    @FXML private VBox leftPanel;
    private MatrixView matrixView;
    @FXML private AnchorPane anchorPane;
    private GraphGroup graphGroup = new GraphGroup();

    private FileChooser fileChooser = new FileChooser();
    private InputFileParser inputFileParser = new InputFileParser();
    private OutputFileSaver outputFileSaver = new OutputFileSaver();
    private InputDialog inputDialog = new InputDialog();

    void init() {
        anchorPane.getChildren().add(graphGroup);

        matrixView = new MatrixView(graphGroup.getVertices(), graphGroup.getEdges());
        leftPanel.getChildren().add(matrixView);

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Матрица смежности", "*.adj"),
                new FileChooser.ExtensionFilter("Матрица инцидентности", "*.inc"),
                new FileChooser.ExtensionFilter("Ребра", "*.ed")
        );



//        anchorPane.setVisible(false); // todo delete
//        AnchorPane.setLeftAnchor(graphGroup.getClip(), 0.0);
//        AnchorPane.setRightAnchor(graphGroup.getClip(), 0.0);
    }

    // TODO del
    @FXML private void onTestAction(ActionEvent event) {
        System.gc();
    }

    // File
    @FXML private void onOpenFile(ActionEvent event) {
        fileChooser.setInitialDirectory(filesDirectory);
        File file = fileChooser.showOpenDialog(null);
        // TODO debug
        //File file = new File("C:\\Users\\Sergey\\Desktop\\1.adj");
        //file = new File("C:\\Users\\Sergey\\Desktop\\1.inc");

        GraphData result = null;
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


//        System.out.println(result);

    }

    @FXML private void onSaveFile(ActionEvent event) {
        fileChooser.setInitialDirectory(filesDirectory);
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
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
            }
        }
    }

    // Edit
    @FXML private void onUndoAction(ActionEvent event) {
        GraphActionsController.undo();
    }

    @FXML private void onRedoAction(ActionEvent event) {
        GraphActionsController.redo();
    }

    @FXML private void onChangeWidth(ActionEvent event) {
        Double width = inputDialog.getGraphWidth(graphGroup.getWidth());
        if (width != null)
            graphGroup.setWidth(width, true);
    }

    @FXML private void onChangeHeight(ActionEvent event) {
        Double height = inputDialog.getGraphHeight(graphGroup.getHeight());
        if (height != null)
            graphGroup.setHeight(height, true);
    }

    // other
    @FXML private void onCurrentActionChanged(ActionEvent event) {
        switch (((RadioButton)event.getSource()).getId()) {
            case "rb_createVertex":
                graphGroup.setCurrentAction(GraphGroup.Action.CreateVertex);
                break;
            case "rb_createEdge":
                graphGroup.setCurrentAction(GraphGroup.Action.CreateEdge);
                break;
            case "rb_delete":
                graphGroup.setCurrentAction(GraphGroup.Action.Delete);
                break;
            case "rb_move":
                graphGroup.setCurrentAction(GraphGroup.Action.Move);
                break;
        }
    }


}
