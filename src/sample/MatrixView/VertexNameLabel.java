package sample.MatrixView;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.Label;
import sample.Graph.Elements.Vertex;

@Deprecated
public class VertexNameLabel extends Label {
    private Vertex vertex;
    private ChangeListener<String> listener;


    public VertexNameLabel(Vertex vertex) {
        this.vertex = vertex;
        listener = ((observable, oldValue, newValue) ->
                setText(newValue));
        vertex.nameProperty().addListener(listener);

        setText(vertex.getName());
    }

    public void removeListener() {
        vertex.nameProperty().removeListener(listener);
    }

}
