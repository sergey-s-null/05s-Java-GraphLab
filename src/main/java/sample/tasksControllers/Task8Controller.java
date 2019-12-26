package sample.tasksControllers;

import Jama.Matrix;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import sample.Graph.Elements.Vertex;
import sample.Graph.GraphGroup;
import sample.GraphAlert;
import sample.GraphAlgorithms;
import sample.GraphTab;
import sample.dialogs.SelectGraphDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class Task8Controller extends TaskController {
    @FXML private HBox root;

    @Override
    public Parent getRoot() {
        return root;
    }

    @FXML private void onSelectOperation(ActionEvent event) {
        if (!startIfCan.apply(this)) {
            GraphAlert.showInfoAndWait("Невозможно начать.");
            return;
        }
        execute(event);
        end.run();
    }

    private void execute(ActionEvent event) {
        BiFunction<Boolean, Boolean, Boolean> op =
                getOperationById(((Button) event.getSource()).getId());
        if (op == null) return;

        GraphTab firstTab = currentGraphTab.get().orElse(null);
        if (firstTab == null) return;

        List<GraphTab> otherTabs = new ArrayList<>(allGraphTabs.get());
        otherTabs.remove(firstTab);
        if (otherTabs.size() == 0) {
            GraphAlert.showInfoAndWait("Нужно хотя бы 2 графа.");
            return;
        }


        GraphTab secondTab = selectGraph.get().orElse(null);
        if (secondTab == null) return;

        if (firstTab.getGraphGroup().getVertices().size() != secondTab.getGraphGroup().getVertices().size()) {
            GraphAlert.showErrorAndWait("Количество вершин графов не совпадает.");
            return;
        }

        GraphTab resultTab = createNewGraph.get();
        GraphGroup resultGraph = resultTab.getGraphGroup();

        for (Vertex vertex : firstTab.getGraphGroup().getVertices())
            resultGraph.addVertex(vertex.getCenterX(), vertex.getCenterY(), false);

        Matrix mtx1 = firstTab.getMatrixView().getMatrix();
        Matrix mtx2 = secondTab.getMatrixView().getMatrix();
        GraphAlgorithms.applyBinaryOperation(resultGraph, mtx1, mtx2, op);
    }

    private BiFunction<Boolean, Boolean, Boolean> getOperationById(String id) {
        switch (id) {
            case "0": // A or B
                return (a, b) -> a || b;
            case "1": // A and B
                return (a, b) -> a && b;
            case "2": // A \ B
            case "6": // !(A -> B)   A \ B
                return (a, b) -> a && !b;
            case "3": // B \ A
            case "7": // !(B -> A) break;   B \ A
                return (a, b) -> b && !a;
            case "4": // A -> B
                return (a, b) -> !a || b;
            case "5": // B -> A
                return (a, b) -> !b || a;
            case "8": // A ^ B break;
                return (a, b) -> a ^ b;
            case "9": // A == B break;
                return (a, b) -> a == b;
            case "10": // !(A and B) штрих шефера break;
                return (a, b) -> !(a && b);
            case "11": // !(A or B) break;
                return (a, b) -> !(a || b);
            default:
                return null;
        }
    }
}
