package sample.Graph.EventsContainers;

import sample.Graph.Elements.Vertex;

import java.util.function.Consumer;

public class SelectVertexEvents extends MouseEvents {
    private Consumer<Vertex> onSelected = null;

    public SelectVertexEvents() {
        super();

        onLeftClickVertex = (event) -> {
            Vertex vertex = (Vertex) event.getSource();
            event.consume();
            if (onSelected != null) onSelected.accept(vertex);
        };
    }

    @Override
    public void deactivate() {
        super.deactivate();
        onSelected = null;
    }

    public void setOnSelected(Consumer<Vertex> consumer) {
        onSelected = consumer;
    }
}
