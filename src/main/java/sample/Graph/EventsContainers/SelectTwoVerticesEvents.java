package sample.Graph.EventsContainers;

import sample.Graph.Elements.Vertex;

import java.util.function.BiConsumer;

public class SelectTwoVerticesEvents extends MouseEvents {
    private BiConsumer<Vertex, Vertex> onSelected;
    private Vertex firstVertex;

    public SelectTwoVerticesEvents() {
        super();
        onLeftClickVertex = (event) -> {
            Vertex vertex = (Vertex) event.getSource();
            if (firstVertex == null) {
                firstVertex = vertex;
                firstVertex.setSelected(true);
            }
            else if (firstVertex == vertex) {
                firstVertex.setSelected(false);
                firstVertex = null;
            }
            else {
                firstVertex.setSelected(false);
                if (onSelected != null)
                    onSelected.accept(firstVertex, vertex);
            }
            event.consume();
        };
    }

    @Override
    public void deactivate() {
        super.deactivate();
        if (firstVertex != null) {
            firstVertex.setSelected(false);
            firstVertex = null;
        }
        onSelected = null;
    }

    public void setOnSelected(BiConsumer<Vertex, Vertex> consumer) {
        onSelected = consumer;
    }


}
