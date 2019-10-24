package sample.Graph.ContextMenus;

import javafx.event.Event;
import javafx.event.EventType;
import sample.Graph.Elements.Vertex;

public class VertexEvent extends Event {
    private Vertex vertex;
    private VertexContextMenu.Action action;

    VertexEvent(Vertex vertex, VertexContextMenu.Action action) {
        super(EventType.ROOT);
        this.vertex = vertex;
        this.action = action;
    }

    public Vertex getVertex() {
        return vertex;
    }

    public VertexContextMenu.Action getAction() {
        return action;
    }
}
