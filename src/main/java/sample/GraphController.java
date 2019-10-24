package sample;

import javafx.fxml.FXML;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import sample.Graph.GraphGroup;
import sample.MatrixView.MatrixView;

public class GraphController {

    @FXML private SplitPane splitPane;
    @FXML private AnchorPane anchorPane;
    private GraphGroup graphGroup = new GraphGroup();
    private MatrixView matrixView = new MatrixView(graphGroup.getVertices(), graphGroup.getEdges());


    void init() {
        splitPane.getItems().add(matrixView);

        initAnchorPane();
    }

    private void initAnchorPane() {
        anchorPane.getChildren().add(graphGroup);
        anchorPane.setMaxSize(graphGroup.getWidth(), graphGroup.getHeight());
        anchorPane.setMinSize(graphGroup.getWidth(), graphGroup.getHeight());


        graphGroup.widthObservable().addListener((observable, oldValue, newValue) -> {
            anchorPane.setMinWidth((Double) newValue);
            anchorPane.setMaxWidth((Double) newValue);
        });
        graphGroup.heightObservable().addListener((observable, oldValue, newValue) -> {
            anchorPane.setMinHeight((Double) newValue);
            anchorPane.setMaxHeight((Double) newValue);
        });
    }

    public GraphGroup getGraphGroup() {
        return graphGroup;
    }

    public MatrixView getMatrixView() {
        return matrixView;
    }

}
