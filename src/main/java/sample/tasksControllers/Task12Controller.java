package sample.tasksControllers;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;
import sample.Graph.GraphGroup;
import sample.GraphAlert;
import sample.GraphAlgorithms;
import sample.MatrixView.MatrixView;

public class Task12Controller extends TaskController {
    @FXML private HBox root;

    @FXML private void onPrimSelected() {
        if (!startIfCan.apply(this)) {
            GraphAlert.showInfoAndWait("Невозможно начать.");
            return;
        }

        GraphGroup graphGroup = currentGraph.get().orElse(null);
        MatrixView matrixView = currentMatrixView.get().orElse(null);
        if (graphGroup == null || matrixView == null) {
            end.run();
            return;
        }
        if (!validateGraph(graphGroup, matrixView)) {
            end.run();
            return;
        }

        graphGroup.setOnVertexSelected(root -> {
            GraphGroup resultGraph = createNewGraph.get().getGraphGroup();
            GraphAlgorithms.makeSpanningTreeByPrim(resultGraph, graphGroup, root);
            end.run();
        });
        graphGroup.setCurrentAction(GraphGroup.Action.SelectVertex);
    }

    @FXML private void onKraskalSelected() {
        if (!startIfCan.apply(this)) {
            GraphAlert.showInfoAndWait("Невозможно начать.");
            return;
        }
        GraphGroup graphGroup = currentGraph.get().orElse(null);
        MatrixView matrixView = currentMatrixView.get().orElse(null);
        if (graphGroup == null || matrixView == null) {
            end.run();
            return;
        }
        if (!validateGraph(graphGroup, matrixView)) {
            end.run();
            return;
        }

        GraphGroup resultGraph = createNewGraph.get().getGraphGroup();
        GraphAlgorithms.makeSpanningTreeByKraskal(resultGraph, graphGroup);
        end.run();
    }

    @FXML private void onBoruwkaSelected() {
        if (!startIfCan.apply(this)) {
            GraphAlert.showInfoAndWait("Невозможно начать.");
            return;
        }
        GraphGroup graphGroup = currentGraph.get().orElse(null);
        MatrixView matrixView = currentMatrixView.get().orElse(null);
        if (graphGroup == null || matrixView == null) {
            end.run();
            return;
        }
        if (!validateGraph(graphGroup, matrixView)) {
            end.run();
            return;
        }

        GraphGroup resultGraph = createNewGraph.get().getGraphGroup();
        GraphAlgorithms.makeSpanningTreeByBoruwka(resultGraph, graphGroup);
        end.run();
    }

    private boolean validateGraph(GraphGroup graphGroup, MatrixView matrixView) {
        if (graphGroup.getVerticesCount() < 2) {
            GraphAlert.showInfoAndWait("Вершин должно быть больше 1.");
            return false;
        }
        if (graphGroup.isGraphOriented()) {
            GraphAlert.showInfoAndWait("Граф должен быть неориентированным.");
            return false;
        }
        int componentsCount = GraphAlgorithms.findConnectivityComponents(matrixView.getMatrix()).size();
        if (componentsCount != 1) {
            GraphAlert.showInfoAndWait("Граф должен быть связным.");
            return false;
        }
        return true;
    }

    @Override
    public Parent getRoot() {
        return root;
    }
}
