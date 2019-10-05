package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import sample.Graph.GraphActionsController;
import sample.Graph.GraphGroup;
import sample.MatrixView.MatrixView;
import sample.Parser.InputFileParser;
import sample.Parser.GraphData;

import java.io.File;


//rb_createVertex
//rb_createEdge
//rb_delete
//rb_move

public class Controller {
    @FXML private VBox leftPanel;
    @FXML private AnchorPane anchorPane;
    private GraphGroup graphGroup = new GraphGroup();

    private FileChooser fileChooser = new FileChooser();
    private InputFileParser inputFileParser = new InputFileParser();

    void init() {
        anchorPane.getChildren().add(graphGroup);

        leftPanel.getChildren().add(new MatrixView(graphGroup.getVertices(),
                graphGroup.getEdges()));

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Матрица смежности", "*.adj"),
                new FileChooser.ExtensionFilter("Матрица инцидентности", "*.inc"),
                new FileChooser.ExtensionFilter("Ребра", "*.ed")
        );
//        AnchorPane.setLeftAnchor(graphGroup.getClip(), 0.0);
//        AnchorPane.setRightAnchor(graphGroup.getClip(), 0.0);
    }

    // TODO del
    @FXML private void onTestAction(ActionEvent event) {

    }

    // File
    @FXML private void onOpenFile(ActionEvent event) {
        //File file = fileChooser.showOpenDialog(null);
        // TODO debug
        File file = new File("C:\\Users\\Sergey\\Desktop\\1.adj");
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
                    System.out.println("Unknown extension.");
                    // TODO
                }
            }
            catch (Exception e) {
                for (StackTraceElement elem : e.getStackTrace())
                    System.out.println(elem);
                System.out.println("\n" + e.getMessage());
                System.out.println(e.getClass());
            }
        }

        System.out.println(result);

    }

    @FXML private void onSaveFile(ActionEvent event) {

    }

    // Edit
    @FXML private void onUndoAction(ActionEvent event) {
        GraphActionsController.undo();
    }

    @FXML private void onRedoAction(ActionEvent event) {
        GraphActionsController.redo();
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
