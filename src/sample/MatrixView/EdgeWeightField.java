package sample.MatrixView;

import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import sample.Graph.Elements.Edge;

import java.util.HashSet;
import java.util.Set;

@Deprecated
public class EdgeWeightField extends TextField {
    private double weight = 0;
    private Set<Edge> edges = new HashSet<>();

    public EdgeWeightField() {
        textProperty().set("0.0");
        setEditable(false);

        textProperty().addListener((observable, oldValue, newValue) -> {
            textChanged();
        });
        setOnKeyPressed(this::onKeyPressed);

        setStyleNotEditable();
    }

    private void setStyleOk() {
        setStyle("-fx-background-color: white;");
    }

    private void setStyleChanged() {
        setStyle("-fx-background-color: red;");
    }

    private void setStyleNotEditable() {
        setStyle("-fx-background-color: gray;");
    }

    private Double getTextValue() {
        try {
            return Double.parseDouble(textProperty().get());
        }
        catch (NumberFormatException ignored) {
            return null;
        }
    }

    private void textChanged() {
        Double res = getTextValue();

        if (res == null || !res.equals(weight)) {
            // TODO maybe some flag    wasChanged
            setStyleChanged();
        }
        else {
            setStyleOk();
        }
    }

    private void onKeyPressed(KeyEvent event) {
        if (event.getCode().equals(KeyCode.ENTER)) {
            Double textValue = getTextValue();
            if (textValue == null || edges.size() != 1)
                return;
            if (textValue.equals(weight)) {
                setStyleOk();
                return;
            }

            weight = textValue;
            for (Edge edge : edges)
                edge.setWeight(weight);
            setStyleOk();
        }
    }

    public void addEdge(Edge edge) {
        // TODO change style
        // TODO add listener
        edges.add(edge);
        setEditable(edges.size() == 1);
    }

    public void removeEdge(Edge edge) {
        // TODO change style
        edges.remove(edge);
        setEditable(edges.size() == 1);
    }



}

