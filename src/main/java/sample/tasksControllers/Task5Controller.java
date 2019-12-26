package sample.tasksControllers;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import sample.Graph.Elements.Vertex;
import sample.Graph.GraphGroup;
import sample.GraphAlert;
import sample.GraphAlgorithms;
import sample.GraphTab;

import java.util.Map;

public class Task5Controller extends TaskController {
    @FXML private VBox root;
    @FXML private TextField resultField, mappingField;

    @Override
    public Parent getRoot() {
        return root;
    }

    @FXML private void onStart() {
        if (!startIfCan.apply(this)) {
            GraphAlert.showInfoAndWait("Невозможно начать.");
            return;
        }
        execute();
        end.run();
    }

    private void execute() {
        GraphGroup first = currentGraph.get().orElse(null);
        if (first == null) return;
        GraphTab secondTab = selectGraph.get().orElse(null);
        if (secondTab == null) return;
        GraphGroup second = secondTab.getGraphGroup();
        if (first == second) return;

        if (first.isMultiGraph() || second.isMultiGraph()) {
            GraphAlert.showInfoAndWait("Один из графов является мультиграфом.");
            return;
        }
        if (first.getVerticesCount() == 0 || second.getVerticesCount() == 0) {
            GraphAlert.showInfoAndWait("Один из графов пуст.");
            return;
        }
        if (first.getVerticesCount() != second.getVerticesCount()) {
            GraphAlert.showInfoAndWait("Кол-во вершин в графах не равны.");
            return;
        }

        Map<Vertex, Vertex> isomorphism = GraphAlgorithms.checkIsomorphism(first, second).orElse(null);
        if (isomorphism == null)
            setEmptyResult();
        else
            setResult(isomorphism);
    }

    private void setResult(Map<Vertex, Vertex> isomorphism) {
        resultField.setText("Графы изоморфны");
        mappingField.setText(beautifyResult(isomorphism));
    }

    private String beautifyResult(Map<Vertex, Vertex> isomorphism) {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<Vertex, Vertex> pair : isomorphism.entrySet()) {
            builder.append(pair.getKey().getName());
            builder.append("<=>");
            builder.append(pair.getValue().getName());
            builder.append("; ");
        }
        return builder.toString();
    }

    private void setEmptyResult() {
        resultField.setText("Графы неизоморфны");
        mappingField.setText("-");
    }




}
